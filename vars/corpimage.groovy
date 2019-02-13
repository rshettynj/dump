def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
	  	try {
                if ("${config.awsregion}" == "null") {
                awsregion = 'us-east-1'
                }
		else {
		awsregion = 'us-west-2'
                }
		def corpimageOUT  = getcorpimage("${config.awsroleArn}","${config.createdate}","${config.filterpattern}","${awsregion}")
		  if ("${corpimageOUT}" == "null" || "${corpimageOUT}" == '') {
                    currentBuild.result = 'FAILED'
                    echo "No image found, expand your search filter to search older images perhaps..."
                }
		  echo "Image ID is ${corpimageOUT}"
		} catch (err) {
		    currentBuild.result = 'FAILED'
		    throw err
		}
}

def getcorpimage(awsroleArn,createdate,filterpattern,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: arn"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def imageid = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws ec2 --region ${awsregion} describe-images --filter \"Name=is-public,Values=false\" --query \"Images[].[Name,ImageId,CreationDate>=\'$createdate\']\" --output=text |grep -i \"$filterpattern\"  | grep True | cut -f 2  | tail -1")
		writeFile file: "output/corp-ami.txt", text: "${imageid}"
		return imageid
                }
}
return this;
