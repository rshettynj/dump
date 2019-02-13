def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
                if ("${config.awsregion}" == "null") {
                awsregion = 'us-east-1'
                }
                else {
                awsregion = 'us-west-2'
                }
		def bt = "${env.BUILD_TAG}"
                def directory = sh(returnStdout: true, script: "mkdir -p /tmp/\"${bt}\"")
		if ("${config}".contains( 'createdashboard') == true) { 
			def result = createdashboard("${bt}","${config.awsroleArn}","${awsregion}")
			echo "Dashboard is created ${result}"
                }
		if ("${config}".contains( 'createapigateway') == true) {
                        def templatefile = getresources("${bt}","${config.resourcePath}")
			def result = createapigateway("${bt}","${config.awsroleArn}",templatefile,"${config.apititle}","${config.systemname}","${config.servicename}","${config.domainname}",,"${awsregion}")
			echo "API Gateway is created ${result}"
                }
		if ("${config}".contains( 'createStack') == true) { 
			def resource = getresources("${bt}","${config.resourcePath}")
			def result = createstack("${config.environment}","${config.awsroleArn}",resource,"${awsregion}")
			def value = deserialize("${result}")
			echo "STACK IS ${value.StackId}"

			def finalstatus = "CREATE_IN_PROGRESS"
			int counter = "${config.retrycount}".toInteger()
			int counternow1 = 0

			while("${finalstatus}" == "CREATE_IN_PROGRESS" && counternow1 < counter)
			{
			counternow1++
			sleep("${config.sleeptime}".toInteger())
			int timeelapsed1 = counternow1.multiply("${config.sleeptime}".toInteger())
			echo "elaspsed time is : ${timeelapsed1}"
                        def status = checkstackstatus("${config.environment}","${config.awsroleArn}","${value.StackId}","${awsregion}")
                        def sstatus = deserialize("${status}")
                        finalstatus = "${sstatus.Stacks.StackStatus}".toString().replaceAll("^\\[|\\]\$", "")
			}
			if ( "${finalstatus}" != "CREATE_COMPLETE" ) { currentBuild.result = 'FAILED' }
			def sname = createservicename("${config.environment}","${awsregion}")
			echo "returned service name is: ${sname}"

			int counternow2 = 0
			retry("${config.retrycount}".toInteger()) {
			counternow2++
			int timeelapsed2 = counternow2.multiply("${config.sleeptime}".toInteger())
			echo "elaspsed time is : ${timeelapsed2}"
               		sleep("${config.sleeptime}".toInteger())
	                awshealthcheck.servicehealthcheck("${bt}","${config.awsroleArn}",sname,"${awsregion}")
			}

	                def instanceid1 = readFile file: "/tmp/${bt}/instanceid.txt"
			def instanceid = "${instanceid1}".toString().trim()
			objout = image.createimage("${config.awsroleArn}","${instanceid}","${awsregion}")
	                def imageid = deserialize("${objout}")
       		        echo "Image ID is ${imageid}"

               	 	int counternow3 = 0
                	def finalstatus3 = "Pending"
			
	                while("${finalstatus3}" != "available" && counternow3 < 30)
       		        {
               		        counternow3++
				keysList3 = image.checkimagestatus("${config.awsroleArn}","${imageid.ImageId}","${awsregion}")
	                        def out1 = deserialize("${keysList3}")
       		                finalstatus3 = "${out1.Images.State}".toString().replaceAll("^\\[|\\]\$", "")
                        	echo "Image status is ${finalstatus3}"
                        	sleep(30)
                	}
                	if ( "${finalstatus3}" != "available" ) { currentBuild.result = 'FAILED' }
			keysList4 = image.createtag("${config.awsroleArn}","${imageid.ImageId}","${awsregion}")
			keysList5 = image.addpermissions("${config.awsroleArn}","${imageid.ImageId}","${awsregion}")
                	writeFile file: "output/ami.txt", text: "${imageid.ImageId}"
                	echo "tags created ${keysList4}"
                	echo "cross account permissions in aws is set ${keysList5}"
			}
		if ("${config}".contains( 'deleteStack') == true) { 
			deletestack("${config.environment}","${config.awsroleArn}","${config.stackname}","${awsregion}")
			}
		if ("${config}".contains( 'createEcache') == true) { 
                        def resource = getresources("${bt}","${config.resourcePath}")
			createecache("${config.environment}","${config.awsroleArn}",resource)
			}
 }

def createservicename(environment,awsregion) {
			switch (environment) {
				case 'dne.dev':
					def servicename = "dne.dev.web.v${BUILD_NUMBER}.${awsregion}.aptechdevlab.com"
					return servicename
				break
				case 'dne.qa':
					def servicename = "dne.qa.web.v${BUILD_NUMBER}.${awsregion}.aptechlab.com"
					return servicename
				break
				case 'dne.qaoregon':
					def servicename = "dne.qa.web.v${BUILD_NUMBER}.${awsregion}.aptechlab.com"
					return servicename
				break
				case 'dne.prod':
					def servicename = "dne.prod.web.v${BUILD_NUMBER}.${awsregion}.associatedpress.com"
					return servicename
				break
				case 'dne.prodoregon':
					def servicename = "dne.prod.web.v${BUILD_NUMBER}.${awsregion}.associatedpress.com"
					return servicename
				break
				default:
					echo "Please make a correct environment choice (dne.dev/dne.qa/dne.qaoregon/dne.prod/dne.prodoregon)"
				break
			}
}

def deserialize(object) {
                def slurper = new groovy.json.JsonSlurperClassic()
                def result = slurper.parseText(object)
		return result
	}

def serialize(object) {
                def output = new groovy.json.JsonOutput()
                def result = output.toJson(object)
		return result
	}

def checkstackstatus(environment,awsroleArn,stackname,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()

		def stacknamestatus = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation describe-stacks --stack-name $stackname")
		return stacknamestatus
}
}

def createstack(environment,awsroleArn,resource,awsregion) {
	stage 'Creating CF Stack'
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		def out = deserialize(resource)
		switch (environment) {
			case 'dne.dev':
	                	def amiid1 = readFile file: "output/corp-ami.txt"
				def amiid = "${amiid1}".toString().trim()
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} sns subscribe --topic-arn \"${out.dne.dev.topicarn}\" --protocol lambda --notification-endpoint \"${out.dne.dev.notificationarn}\"")
			        def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation create-stack --stack-name \"${out.dne.dev.stacknameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.dev.templateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=InstanceImageID,ParameterValue=\"${amiid}\" ParameterKey=EfsInteractiveVolumeName,ParameterValue=\"${out.dne.dev.efsinteractivesid}\" ParameterKey=EfsVolumeName,ParameterValue=\"${out.dne.dev.efsdneid}\" --tags Key=\"Name\",Value=\"${out.dne.dev.environment}\" Key=\"Services\",Value=\"${out.dne.dev.services}\" Key=\"Support Team\",Value=\"${out.dne.dev.supportteam}\" Key=\"Environment\",Value=\"${out.dne.dev.environment}\" --capabilities CAPABILITY_IAM --notification-arns \"${out.dne.dev.topicarn}\"")
				return "${stackid}"
			break
			case 'dne.devd8':
				def amiid1 = readFile file: "output/corp-ami.txt"
				def amiid = "${amiid1}".toString().trim()
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} sns subscribe --topic-arn \"${out.dne.devd8.topicarn}\" --protocol lambda --notification-endpoint \"${out.dne.devd8.notificationarn}\"")
			        def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation create-stack --stack-name \"${out.dne.devd8.stacknameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.devd8.templateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=InstanceImageID,ParameterValue=\"${amiid}\" ParameterKey=EfsInteractiveVolumeName,ParameterValue=\"${out.dne.devd8.efsinteractivesid}\" ParameterKey=EfsVolumeName,ParameterValue=\"${out.dne.devd8.efsdneid}\" --tags Key=\"Name\",Value=\"${out.dne.devd8.environment}\" Key=\"Services\",Value=\"${out.dne.devd8.services}\" Key=\"Support Team\",Value=\"${out.dne.devd8.supportteam}\" Key=\"Environment\",Value=\"${out.dne.devd8.environment}\" --capabilities CAPABILITY_IAM --notification-arns \"${out.dne.devd8.topicarn}\"")
				return "${stackid}"
			break
			case 'dne.qa':
	                	def amiid1 = readFile file: "output/ami.txt"
				def amiid = "${amiid1}".toString().trim()
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} sns subscribe --topic-arn \"${out.dne.qa.topicarn}\" --protocol lambda --notification-endpoint \"${out.dne.qa.notificationarn}\"")
		                def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation create-stack --stack-name \"${out.dne.qa.stacknameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.qa.templateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=InstanceImageID,ParameterValue=\"${amiid}\" ParameterKey=EfsInteractiveVolumeName,ParameterValue=\"${out.dne.qa.efsinteractivesid}\" ParameterKey=EfsVolumeName,ParameterValue=\"${out.dne.qa.efsdneid}\" --tags Key=\"Name\",Value=\"${out.dne.qa.environment}\" Key=\"Services\",Value=\"${out.dne.qa.services}\" Key=\"Support Team\",Value=\"${out.dne.qa.supportteam}\" Key=\"Environment\",Value=\"${out.dne.qa.environment}\" --capabilities CAPABILITY_IAM --notification-arns \"${out.dne.qa.topicarn}\"")
			return "${stackid}"
			break
			case 'dne.qaoregon':
			def amiid1 = readFile file: "output/corp-ami.txt"
			def amiid = "${amiid1}".toString().trim()
			sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} sns subscribe --topic-arn \"${out.dne.qaoregon.topicarn}\" --protocol lambda --notification-endpoint \"${out.dne.qaoregon.notificationarn}\"")
		               def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation create-stack --stack-name \"${out.dne.qaoregon.stacknameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.qaoregon.templateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=InstanceImageID,ParameterValue=\"${amiid}\" ParameterKey=EfsInteractiveVolumeName,ParameterValue=\"${out.dne.qaoregon.efsinteractivesid}\" ParameterKey=EfsVolumeName,ParameterValue=\"${out.dne.qaoregon.efsdneid}\" --tags Key=\"Name\",Value=\"${out.dne.qaoregon.environment}\" Key=\"Services\",Value=\"${out.dne.qaoregon.services}\" Key=\"Support Team\",Value=\"${out.dne.qaoregon.supportteam}\" Key=\"Environment\",Value=\"${out.dne.qaoregon.environment}\" --capabilities CAPABILITY_IAM --notification-arns \"${out.dne.qaoregon.topicarn}\"")
			return "${stackid}"
			break
			case 'dne.prod':
				def amiid1 = readFile file: "output/ami.txt"
				def amiid = "${amiid1}".toString().trim()
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} sns subscribe --topic-arn \"${out.dne.prod.topicarn}\" --protocol lambda --notification-endpoint \"${out.dne.prod.notificationarn}\"")
		                def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation create-stack --stack-name \"${out.dne.prod.stacknameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.prod.templateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=InstanceImageID,ParameterValue=\"${amiid}\" ParameterKey=EfsInteractiveVolumeName,ParameterValue=\"${out.dne.prod.efsinteractivesid}\" ParameterKey=EfsVolumeName,ParameterValue=\"${out.dne.prod.efsdneid}\" --tags Key=\"Name\",Value=\"${out.dne.prod.environment}\" Key=\"Services\",Value=\"${out.dne.prod.services}\" Key=\"Support Team\",Value=\"${out.dne.prod.supportteam}\" Key=\"Environment\",Value=\"${out.dne.prod.environment}\" --capabilities CAPABILITY_IAM --notification-arns \"${out.dne.prod.topicarn}\"")
			return "${stackid}"
			break
			case 'dne.prodoregon':
				def amiid1 = readFile file: "output/corp-ami.txt"
				def amiid = "${amiid1}".toString().trim()
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} sns subscribe --topic-arn \"${out.dne.prodoregon.topicarn}\" --protocol lambda --notification-endpoint \"${out.dne.prodoregon.notificationarn}\"")
		                def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation create-stack --stack-name \"${out.dne.prodoregon.stacknameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.prodoregon.templateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=InstanceImageID,ParameterValue=\"${amiid}\" ParameterKey=EfsInteractiveVolumeName,ParameterValue=\"${out.dne.prodoregon.efsinteractivesid}\" ParameterKey=EfsVolumeName,ParameterValue=\"${out.dne.prodoregon.efsdneid}\" --tags Key=\"Name\",Value=\"${out.dne.prodoregon.environment}\" Key=\"Services\",Value=\"${out.dne.prodoregon.services}\" Key=\"Support Team\",Value=\"${out.dne.prodoregon.supportteam}\" Key=\"Environment\",Value=\"${out.dne.prodoregon.environment}\" --capabilities CAPABILITY_IAM --notification-arns \"${out.dne.prodoregon.topicarn}\"")
			return "${stackid}"
			break
			default:
				echo "Please make a correct environment choice (dne.dev/dne.devd8/dne.qa/dne.prod/dne.prodoregon)"
			break
		}
                }
}

def createecache(environment,awsroleArn,resource) {
        stage 'Creating CF Stack for elasticache'
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def out = deserialize(resource)
                switch (environment) {
                        case 'dne.dev':
                                def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 cloudformation create-stack --stack-name \"${out.dne.dev.cachenameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.dev.cachetemplateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=WebSecurityGroup,ParameterValue=\"${out.dne.dev.websecuritygroup}\" ParameterKey=CacheNodeType,ParameterValue=\"${out.dne.dev.cacheboostsize}\" --tags Key=\"Name\",Value=\"${out.dne.dev.environment}\" Key=\"Services\",Value=\"${out.dne.dev.services}\" Key=\"Support Team\",Value=\"${out.dne.dev.supportteam}\" Key=\"Environment\",Value=\"${out.dne.dev.environment}\" --capabilities CAPABILITY_IAM")
                                return "${stackid}"
                        break
                        case 'dne.qa':
                                def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 cloudformation create-stack --stack-name \"${out.dne.qa.cachenameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.qa.cachetemplateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=WebSecurityGroup,ParameterValue=\"${out.dne.qa.websecuritygroup}\" ParameterKey=CacheNodeType,ParameterValue=\"${out.dne.qa.cacheboostsize}\" --tags Key=\"Name\",Value=\"${out.dne.qa.environment}\" Key=\"Services\",Value=\"${out.dne.qa.services}\" Key=\"Support Team\",Value=\"${out.dne.qa.supportteam}\" Key=\"Environment\",Value=\"${out.dne.qa.environment}\" --capabilities CAPABILITY_IAM")
                                return "${stackid}"
                        break
                        case 'dne.prod':
                                def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 cloudformation create-stack --stack-name \"${out.dne.prod.cachenameprefix}\"\"${BUILD_NUMBER}\" --template-url \"${out.dne.prod.cachetemplateurl}\" --parameters ParameterKey=EnvironmentVersion,ParameterValue=\"${BUILD_NUMBER}\" ParameterKey=WebSecurityGroup,ParameterValue=\"${out.dne.prod.websecuritygroup}\" ParameterKey=CacheNodeType,ParameterValue=\"${out.dne.prod.cacheboostsize}\" --tags Key=\"Name\",Value=\"${out.dne.prod.environment}\" Key=\"Services\",Value=\"${out.dne.prod.services}\" Key=\"Support Team\",Value=\"${out.dne.prod.supportteam}\" Key=\"Environment\",Value=\"${out.dne.prod.environment}\" --capabilities CAPABILITY_IAM")
                                return "${stackid}"
                        break
			default:
				echo "Please make a correct environment choice (dne.dev/dne.qa/dne.prod)"
			break
                }
          }
}


def deletestack(environment,awsroleArn,stackname,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()

		switch (environment) {
                        case 'dne.dev':
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation delete-stack --stack-name \"${stackname}\"")
			break
			default:
				echo "Please make a correct environment choice (dev/qa/prod)"
			break
		}
		}
}

def mapParse(def map, String keys) {
    def m = map
    keys.split(/\./).each {
        if(m.getAt(it)) {
            m = m.getAt(it)
        }
    }
    return m
}

def createdashboard(directory,awsroleArn,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()

                def webalb = sh(returnStdout: true, script: "cat output/stackresources.txt | jq \'.| select(.LogicalResourceId == \"WebALB\")\' | jq \'.PhysicalResourceId\' | cut -f 6 -d : | cut -f 2- -d / | sed \'s/\"//g\'")
                def httptargetgroup = sh(returnStdout: true, script: "cat output/stackresources.txt | jq \'.| select(.LogicalResourceId == \"ALBhttpTargetGroup\")\' | jq \'.PhysicalResourceId\' | cut -f 6 -d : | sed \'s/\"//g\'")
                def httpstargetgroup = sh(returnStdout: true, script: "cat output/stackresources.txt | jq \'.| select(.LogicalResourceId == \"ALBhttpsTargetGroup\")\' | jq \'.PhysicalResourceId\' | cut -f 6 -d : | sed \'s/\"//g\'")
                def asgname = sh(returnStdout: true, script: "cat output/stackresources.txt | jq \'.| select(.LogicalResourceId == \"WebASPup\")\' | jq \'.PhysicalResourceId\' | cut -f 8 -d : | sed \'s/\"//g\'")
                def cachenames = sh(returnStdout: true, script: "cat output/stackresources.txt | jq \'. | select(.LogicalResourceId == \"CacheCluster\")\' | jq \'.PhysicalResourceId\' | sed \'s/\"//g\'").trim().tokenize()

		def hoscachename
		def dnecachename
		def elecachename

		cachenames.each() {
		switch ("${it}") {
		case ~/^dne.*hos.*/:
		   hoscachename = "${it}".toString()
		   echo hoscachename 
                break
		case ~/^dne.*ele.*/:
		   elecachename = "${it}".toString()
		   echo elecachename
                break
	        case ~/^dne.*all.*/:	
		   dnecachename = "${it}".toString()
		   echo dnecachename
                break
                }
                }

                webalb = "$webalb".trim().toString()
                httptargetgroup = "$httptargetgroup".trim().toString()
                httpstargetgroup = "$httpstargetgroup".trim().toString()
                asgname = "$asgname".trim().toString()
                hoscachename = "$hoscachename".trim().toString()
                elecachename = "$elecachename".trim().toString()
                dnecachename = "$dnecachename".trim().toString()
 
                echo "ALB name is $webalb"
                echo "http Target Group name is $httptargetgroup"
                echo "https Target Group name is $httpstargetgroup"
                echo "ASG name is $asgname"
                echo "hosted cache name is $hoscachename"
                echo "elections cache name is $elecachename"
                echo "dne cache name is $dnecachename"

		def out = getdashboardtemplate(directory,"aws/dashboard/create-dne-dashboard.sh")

                sh("chmod +x /tmp/$directory/create-dne-dashboard.sh")
                def dashout = sh(returnStdout: true, script:"/tmp/$directory/create-dne-dashboard.sh ${webalb} ${httptargetgroup} $httpstargetgroup $dnecachename $hoscachename $elecachename $asgname $AWS_ACCESS_KEY_ID $AWS_SECRET_ACCESS_KEY $AWS_SECURITY_TOKEN $awsregion")
                return dashout

}
}

def createapigateway(directory,awsroleArn,templatefile,apititle,system,servicename,domainname,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		def stackid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation create-stack --stack-name \"${system}-${servicename}-v${BUILD_NUMBER}\" --template-body file:///tmp/${directory}/resources.json --parameters ParameterKey=apititle,ParameterValue=$apititle ParameterKey=domainname,ParameterValue=$domainname --capabilities CAPABILITY_IAM")
                return stackid
		}
}


def getresources(directory,resourcepath)
        {
                def resource = libraryResource "${resourcepath}"
                writeFile file: "/tmp/${directory}/resources.json", text: "${resource}"
                def jsonText = readFile file: "/tmp/${directory}/resources.json"
                return jsonText
        }

def getdashboardtemplate(directory,resourcepath)
{
  def resource = libraryResource "${resourcepath}"
  writeFile file: "/tmp/${directory}/create-dne-dashboard.sh", text: "${resource}"
  def out = readFile file: "/tmp/${directory}/create-dne-dashboard.sh"
  return out
}

return this;
