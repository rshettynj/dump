 def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		stage 's3 Opeations'
                int count = 0
		if ("${config}".contains( 'uploadfilelist') == true) { 
                while("${config.uploadfilelist[count]}" != "null") {
                def sval = "${config.uploadfilelist[count]}"
                def dval = "${config.destinationlist[count]}"
                echo "$sval $dval"
		uploadFile("${config.bucketName}","${sval}","${dval}","${config.awsroleArn}") 
		count++
		}
		}

		if ("${config}".contains( 'downloadfilelist') == true) { 
                while("${config.downloadfilelist[count]}" != "null") {
                def sval = "${config.downloadfilelist[count]}"
                def dval = "${config.destinationlist[count]}"
                echo "$sval $dval"
		downloadFile("${config.bucketName}","${sval}","${dval}","${config.awsroleArn}")
		count++
		}
		} 

		if ("${config}".contains( 'uploaddirectorylist') == true) { 
                while("${config.uploaddirectorylist[count]}" != "null") {
                def sval = "${config.uploaddirectorylist[count]}"
                def dval = "${config.destinationlist[count]}"
                echo "$sval $dval"
		uploadDirectory("${config.bucketName}","${sval}","${dval}","${config.awsroleArn}")
		count++
		}
		} 

		if ("${config}".contains( 'downloaddirectorylist') == true) { 
                while("${config.downloaddirectorylist[count]}" != "null") {
                def sval = "${config.downloaddirectorylist[count]}"
                def dval = "${config.destinationlist[count]}"
                echo "$sval $dval"
		downloadDirectory("${config.bucketName}","${sval}","${dval}","${config.awsroleArn}")
		count++
		}
		}

 }

def uploadFile(bucketName,source,destination,awsroleArn) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3 cp ${source} s3://${bucketName}/${destination}")
                }
                else
                {
                sh("/usr/bin/aws --region us-east-1 s3 cp ${source} s3://${bucketName}/${destination}")
                }
}

def downloadFile(bucketName,downloadsourceFile,targetFile,awsroleArn) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3 cp s3://${bucketName}/${downloadsourceFile} ${targetFile}")
                }
                else
                {
                sh("/usr/bin/aws --region us-east-1 s3 cp s3://${bucketName}/${downloadsourceFile} ${targetFile}")
                }
}

def uploadDirectory(bucketName,uploadsourceDirectory,targetDirectory,awsroleArn) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3 sync ${uploadsourceDirectory} s3://${bucketName}/${targetDirectory}")
                }
                else
                {
                sh("/usr/bin/aws --region us-east-1 s3 sync ${uploadsourceDirectory} s3://${bucketName}/${targetDirectory}")
                }
}

def downloadDirectory(bucketName,downloadsourceDirectory,targetDirectory,awsroleArn) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 s3 sync s3://${bucketName}/${downloadsourceDirectory} ${targetDirectory}")
                }
                else
                {
                sh("/usr/bin/aws --region us-east-1 s3 sync s3://${bucketName}/${downloadsourceDirectory} ${targetDirectory}")
                }
}
return this;
