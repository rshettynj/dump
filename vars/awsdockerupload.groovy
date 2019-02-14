 def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		stage 'create ecr repository'
		uploadFile("${config.awsroleArn}","${config.imagename}") 
 }

def uploadFile(awsroleArn,imagename) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                sh("AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 ecr get-login --no-include-email")
                }
                else
                {
                sh("/usr/bin/aws --region us-east-1 ecr get-login --no-include-email")
                }
                sh("docker tag ${imagename} 559994907943.dkr.ecr.us-east-1.amazonaws.com/rshetty-testing-002:latest")
                sh("docker push 559994907943.dkr.ecr.us-east-1.amazonaws.com/rshetty-testing-002:latest")
}

return this;
