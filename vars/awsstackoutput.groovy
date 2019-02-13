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
        if ("${config}".contains( 'stackname') == true) {
	   resourcelist = stackoutput("${config.stackname}","${config.awsroleArn}","${awsregion}")
           int count = 0
           def fulldata = ''
           resourcelist.each() {
             count++
             sleep(10)
             nestedresourceout$count = nestedstackoutput("${it}","${config.awsroleArn}","${awsregion}")
             fulldata = fulldata + nestedresourceout$count
             }
             writeFile file: "output/stackresources.txt", text: "${fulldata}"
	     def envout = "${config.environment}".split("\\.")
             apArtifactory.uploadartifactory("APArtifactory","output/stackresources.txt","dne-infrastructure-build/${envout[0]}/${envout[1]}/${config.stackname}/stackresources.out")

            if ("${config}".contains('readstackresources') == true) {
            readstackresources("${config.stackname}","${config.environment}","${awsregion}")
            }
         }
	else {
         def bt = "${env.BUILD_TAG}"
         def resource = awsCfops.getresources("${bt}","${config.resourcePath}")
         def resourcelist = buildoutput("${config.environment}","${config.awsroleArn}",resource,"${awsregion}")
         int count = 0
         def fulldata = ''
         resourcelist.each() {
         count++
         sleep(15)
         nestedresourceout$count = nestedstackoutput("${it}","${config.awsroleArn}","${awsregion}")
         fulldata = fulldata + nestedresourceout$count
         }
         writeFile file: "output/stackresources.txt", text: "${fulldata}"
         envout = "${config.environment}".split("\\.")
         def stck = getstackname("${config.environment}",resource)
         apArtifactory.uploadartifactory("APArtifactory","output/stackresources.txt","dne-infrastructure-build/${envout[0]}/${envout[1]}/$stck/stackresources.out")
         if ("${config}".contains('readstackresources') == true) {
         readstackresources("${config.stackname}","${config.environment}")
         }
       }
}

def stackoutput(stackname,awsroleArn,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def status = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation describe-stack-resources --stack-name $stackname | jq \'.StackResources[] | {ResourceType,PhysicalResourceId}\'  |  jq  \'. | select(.ResourceType == \"AWS::CloudFormation::Stack\")\' | jq \'.PhysicalResourceId\' | cut -f 6 -d \':\' | cut -f 2 -d \'/\'")
                def out = "$status".trim().tokenize()
                return out
                }
}

def nestedstackoutput(stackname,awsroleArn,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def status = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation describe-stack-resources --stack-name $stackname | jq '.StackResources[] | {LogicalResourceId,PhysicalResourceId}'")
                return status
                }
}

def buildoutput(environment,awsroleArn,resource,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def out1 = deserialize(resource)
                def sprefix
                switch ("$environment") {
			case 'dne.dev':
				sprefix = "${out1.dne.dev.stacknameprefix}".toString().trim()
                        break
                        case 'dne.qa':
				sprefix = "${out1.dne.qa.stacknameprefix}".toString().trim()
                        break
                        case 'dne.prod':
				sprefix = "${out1.dne.prod.stacknameprefix}".toString().trim()
                        break
                        case 'dne.prodoregon':
				sprefix = "${out1.dne.prod.stacknameprefix}".toString().trim()
                        break
			default:
                                echo "Sorry wrong choice"
			break
                }
                echo "sprefix is ${sprefix}"
                def status = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} cloudformation describe-stack-resources --stack-name \"${sprefix}\"\"${BUILD_NUMBER}\" | jq \'.StackResources[] | {ResourceType,PhysicalResourceId}\'  |  jq  \'. | select(.ResourceType == \"AWS::CloudFormation::Stack\")\' | jq \'.PhysicalResourceId\' | cut -f 6 -d \':\' | cut -f 2 -d \'/\'")
                def out = "$status".trim().tokenize()
                return out
                }
}

def getstackname(environment,resource) {
                def out = deserialize(resource)
                def sprefix
                switch ("$environment") {
                        case 'dne.dev':
                                sprefix = "${out.dne.dev.stacknameprefix}".toString().trim()
                        break
                        case 'dne.qa':
                                sprefix = "${out.dne.qa.stacknameprefix}".toString().trim()
                        break
                        case 'dne.prod':
                                sprefix = "${out.dne.prod.stacknameprefix}".toString().trim()
                        break
                        case 'dne.prodoregon':
                                sprefix = "${out.dne.prod.stacknameprefix}".toString().trim()
                        break
                        default:
                                echo "Sorry wrong choice"
                        break
                }

                def stackname = "${sprefix}" + "${BUILD_NUMBER}"
		return stackname
}

def readstackresources(stackname,environment) {
  apArtifactory.downloadartifactory("APArtifactory","dne-infrastructure-build/${environment}/${stackname}/stackresources.out","/tmp/")
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

return this;
