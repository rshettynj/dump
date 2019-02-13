 def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
	node ('unixaws'){
	  	try {
                stage 'create AMI'
                if ("${config.awsregion}" == "null") {
                awsregion = 'us-east-1'
                }
                else {
                awsregion = 'us-west-2'
                }
		def outobj = createimage("${config.awsroleArn}","${config.instanceid}","${awsregion}")
                def imageid = deserialize("${outobj}")
		echo "Image ID is ${imageid}"

                int counternow1 = 0
		def finalstatus = "Pending"
                while("${finalstatus}" != "available" && counternow1 < 30)
                {
                        counternow1++
			def out = checkimagestatus("${config.awsroleArn}","${imageid.ImageId}","${awsregion}")
       		        def out1 = deserialize("${out}")
                	finalstatus = "${out1.Images.State}".toString().replaceAll("^\\[|\\]\$", "")
			echo "Image status is ${finalstatus}"
                        sleep(30)
		}
		if ( "${finalstatus}" != "available" ) { currentBuild.result = 'FAILED' }
		def out2 = createtag("${config.awsroleArn}","${imageid.ImageId}","${awsregion}")
		writeFile file: "output/ami.txt", text: "${imageid.ImageId}"
		echo "tags created ${out2}"
		def permout = addpermissions("${config.awsroleArn}","${imageid.ImageId}","${awsregion}")
		echo "permissions added for cross account usage of the image"
		} catch (err) {
		currentBuild.result = 'FAILED'
		throw err
		} finally {
		archiveArtifacts artifacts: 'output/*.txt'
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

def createimage(awsroleArn,instanceid,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def imageid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 create-image --instance-id $instanceid --name \"${BUILD_NUMBER} ${AWS_ACCESS_KEY_ID} ${instanceid}-ami-DNE\" --description \"${BUILD_NUMBER} DNE web node ami\" --no-reboot")

		return imageid
}
}

def deleteimage(awsroleArn,imageid,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def result = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 deregister-image --image-id ${imageid}")
                return result
}
}

def getamiid(awsroleArn,version,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def imageid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 describe-images --filters Name=description,Values=\"${version} DNE web node ami\"")
                return imageid
}
}

def checkimagestatus(awsroleArn,imageid,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		def outobj = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 describe-images --image-ids $imageid")
		return outobj
		}
}

def createtag(awsroleArn,imageid,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def out = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 create-tags --resources $imageid --tags Key=\"Name\",Value=\"DNE\" Key=\"Services\",Value=\"DNE\" Key=\"Support Team\",Value=\"sysop@ap.org\" Key=\"Environment\",Value=\"dev\"")
                return out
}
}

def addpermissions(awsroleArn,imageid,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		def out = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 modify-image-attribute --image-id $imageid --attribute launchPermission --user-ids 959162376654 198401342403 720322524327 222259241209 --operation-type add")
		return out
}
}
return this;
