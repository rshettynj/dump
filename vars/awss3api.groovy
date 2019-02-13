def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		def bt = "${env.BUILD_TAG}"
                def vs = WORKSPACE
                def directory = sh(returnStdout: true, script: "mkdir /tmp/\"${bt}\"")

                if ("${config}".contains( 'deleteAllNotification') == true) {
                def result = deleteallbucketnotificationconfiguration("${config.bucketName}","${config.awsroleArn}")
		}

                if ("${config}".contains( 'deleteNotification') == true) {
		def out = getbucketnotificationconfiguration("${config.bucketName}","${config.awsroleArn}")
                writeFile file: "output/notification.json", text: "${out}"
		def idtodelete = "${config.deleteNotification}"
		echo "Idtodlelete is ${idtodelete}"
		def checkqueue = sh(returnStdout: true, script: "cat output/notification.json |  jq \'.QueueConfigurations[] | select(.Id != \"${idtodelete}\")\'").trim()
		def checklambda = sh(returnStdout: true, script: "cat output/notification.json |  jq \'.LambdaFunctionConfigurations[] | select(.Id != \"${idtodelete}\")\'").trim()
		def checktopic = sh(returnStdout: true, script: "cat output/notification.json |  jq \'.TopicConfigurations[] | select(.Id != \"${idtodelete}\")\'").trim()
		echo "checkqueue is ${checkqueue}"
		echo "checklambda is ${checklambda}"
		echo "checktopic is ${checktopic}"
		if ("${checkqueue}") {
                writeFile file: "output/tempqueue.json", text: "${checkqueue}"
		def existing = sh(returnStdout: true, script: "cat output/tempqueue.json | sed '1d; \$d'").trim()
		def part2 = "{".concat("\"QueueConfigurations\" : [ { ${existing} } ] }")
		writeFile file: "output/newqueue.json", text: "${part2}"
		def awsout = putbucketnotificationconfiguration("${config.bucketName}","${config.awsroleArn}","newqueue.json")
		}
		if ("${checklambda}") {
                writeFile file: "output/templambda.json", text: "${checklambda}"
		def existing = sh(returnStdout: true, script: "cat output/templambda.json | sed '1d; \$d'").trim()
		def part2 = "{".concat("\"LambdaFunctionConfigurations\" : [ { ${existing} } ] }")
		writeFile file: "output/newlambda.json", text: "${part2}"
		def awsout = putbucketnotificationconfiguration("${config.bucketName}","${config.awsroleArn}","newlambda.json")
		}
		if ("${checktopic}") {
                writeFile file: "output/temptopic.json", text: "${checktopic}"
		def existing = sh(returnStdout: true, script: "cat output/temptopic.json | sed '1d; \$d'").trim()
		def part2 = "{".concat("\"TopicConfigurations\" : [ { ${existing} } ] }")
		writeFile file: "output/newtopic.json", text: "${part2}"
		def awsout = putbucketnotificationconfiguration("${config.bucketName}","${config.awsroleArn}","newtopic.json")
		}
		}

		if ("${config}".contains( 'notificationJson') == true) {
		def out = getbucketnotificationconfiguration("${config.bucketName}","${config.awsroleArn}")
                writeFile file: "output/notification.json", text: "${out}"
		File file = new File("${vs}/output/notification.json")
		//If not empty
		echo "File length is ${file.length()}"
		if ("${file.length()}" != "0") {
		def existing = sh(returnStdout: true, script: "cat output/notification.json | sed '1d; \$d'").trim()
//		def new = sh(returnStdout: true, script: "echo \"${jsonp}\" | awk '{print substr(\$0, 2, length(\$0) - 2)}'")
		def temp1 = "${existing}".concat(',')
                def temp2 = "${temp1}" + "${config.notificationJson}"
		def temp3 = "{".concat("${temp2}").concat("}")
		writeFile file: "output/newnotification.json", text: "${temp3}"
		}
		else
		{
		def temp1 = '{'.concat("${config.notificationJson}")
		def temp2 = "${temp1}".concat('}')
		writeFile file: "output/newnotification.json", text: "${temp2}"
		}
		def awsout = putbucketnotificationconfiguration("${config.bucketName}","${config.awsroleArn}","newnotification.json")
		if ("${awsout}" == "null") {
		echo "AWS call response is OK"
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

def getbucketnotificationconfiguration(bucketName,awsroleArn) {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def bn = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3api get-bucket-notification-configuration --bucket ${bucketName}")
		return bn
}

def putbucketnotificationconfiguration(bucketName,awsroleArn,newnotification) {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def bn = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3api put-bucket-notification-configuration --bucket ${bucketName} --notification-configuration file://output/${newnotification}")
                return bn
}

def deleteallbucketnotificationconfiguration(bucketName,awsroleArn) {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def bn = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3api put-bucket-notification-configuration --bucket ${bucketName} --notification-configuration {}")
                return bn
}

def deletebucket(bucketName,awsroleArn) {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		bucketName = "$bucketName".trim().toString()
		echo "checking $bucketName if exists..."
                def checkbucket = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3api head-bucket --bucket ${bucketName}")
		echo "Bucket $bucketName found"
                def out1 = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3 rb s3://${bucketName} --force")
                return out1
}

def emptybucket(bucketName,awsroleArn) {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		bucketName = "$bucketName".trim().toString()
                def checkbucket = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3api head-bucket ${bucketName}")
		echo "Bucket found"
                def out2 = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3 rm s3://${bucketName} --recursive")
                return out2
}
return this;
