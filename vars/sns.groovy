def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		def bt = "${env.BUILD_TAG}"
                def directory = sh(returnStdout: true, script: "mkdir /tmp/\"${bt}\"")
		def resource = getresources("${bt}")
	        def out = deserialize(resource)
		switch ("${config.environment}") {
			case 'dne.qa':
                                int count=0
                                def result
                                hostedzonename = "aptechlab.com"
                                accountid = "720322524327"
                                if ("${out.dne.qa.topics}" != "null") {
				stage "Add raw option for topic subscriptions"
                                while ("${out.dne.qa.topics[count]}" != "null") {
                                echo "Adding raw options for subscription:  ${out.dne.qa.topics[count]}"
                                sublist=getsubscriptionarn("${bt}","${config.awsroleArn}","${out.dne.qa.topics[count]}","${accountid}")
				sublist.each() {
                                result=addrawsubscription("${bt}","${config.awsroleArn}","${it}")
                                count++
                                }
                                count--
				}
				}
			break
			case 'dne.prod':
                                int count=0
                                def result
                                hostedzonename = "associatedpress.com"
                                accountid = "198401342403"
                                if ("${out.dne.prod.topics}" != "null") {
                                stage "Add raw option for topic subscriptions"
                                while ("${out.dne.prod.topics[count]}" != "null") {
                                echo "Adding raw options for subscription:  ${out.dne.prod.topics[count]}"
                                sublist=getsubscriptionarn("${bt}","${config.awsroleArn}","${out.dne.prod.topics[count]}","${accountid}")
                                sublist.each() {
                                result=addrawsubscription("${bt}","${config.awsroleArn}","${it}")
                                count++
                                }
                                count--
                                }
                                }
			break
			}
	 }
def addrawsubscription(bt,awsroleArn,subscriptionarn) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 sns set-subscription-attributes --subscription-arn $subscriptionarn --attribute-name RawMessageDelivery --attribute-value true")
		return currOUT
                }
}

def getsubscriptionarn(bt,awsroleArn,topicid,accountid) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 sns list-subscriptions-by-topic --topic-arn arn:aws:sns:us-east-1:${accountid}:${topicid} | jq '.Subscriptions[].SubscriptionArn' | sed \'s/\"//g\'").trim().tokenize()
		return currOUT
                }
}

def getresources(directory)
{
                def resource = libraryResource "aws/sns/resources.json"
                writeFile file: "/tmp/${directory}/resources.json", text: "${resource}"
		def f = readFile file: "/tmp/${directory}/resources.json"
		return f
}

def deserialize(object) {
                def slurper = new groovy.json.JsonSlurperClassic()
                def result = slurper.parseText(object)
                return result
}

return this;
