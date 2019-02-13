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
	if ("${config}".contains('deleteStack') == true) {
	   stage "Delete stack ${config.stackname}"
           deletestack("${config.environment}","${config.awsroleArn}","${config.stackname}","${awsregion}")
	}
	if ("${config}".contains('deleteami') == true) {
	   stage "Delete Web AMI for ${config.version}"
	   def out = image.getamiid("${config.awsroleArn}","${config.version}","${awsregion}")
              writeFile file: "output/getamiid.txt", text: "${out}"
	      def out1 = sh(returnStdout: true, script: "cat output/getamiid.txt | jq '.Images[].ImageId' | sed \'s/\"//g\'")
	   echo out1
           if ("${out1.length()}" != "0") {
	      def out2 = "$out1".toString().trim()
              image.deleteimage("${config.awsroleArn}","${out2}","${awsregion}")
	   }
	   else {
	      echo "No AMI found.."
	   }
	}
        if ("${config}".contains('deletebucket') == true) {
	   stage "Delete s3 logging bucket for ${config.sname}"
           def bt = "${env.BUILD_TAG}"
           envout = "${config.environment}".split("\\.")
           apArtifactory.downloadartifactory("APArtifactory","dne-infrastructure-build/${envout[0]}/${envout[1]}/${config.sname}/stackresources.out","/tmp/${bt}/")
           def loggingbucket = sh(returnStdout: true, script: "cat /tmp/${bt}/${envout[0]}/${envout[1]}/${config.sname}/stackresources.out | jq \'.| select(.LogicalResourceId == \"loggingbucket\")\' | jq \'.PhysicalResourceId\' | sed \'s/\"//g\'")
           echo "deleting bucket ${loggingbucket}"
           awss3api.deletebucket("${loggingbucket}","${config.awsroleArn}")
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
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region $awsregion cloudformation delete-stack --stack-name \"${stackname}\"")
			break
                        case 'dne.qa':
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region $awsregion cloudformation delete-stack --stack-name \"${stackname}\"")
			break
                        case 'dne.prod':
				sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region $awsregion cloudformation delete-stack --stack-name \"${stackname}\"")
			break
			default:
				echo "Please make a correct environment choice (dev/qa/prod)"
			break
		}
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

return this

