def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
	node ('unixaws'){
	try {
                stage 'create cloudsearch index'
                def bt = "${env.BUILD_TAG}"
		def outobj = createindex("${bt}","${config.awsroleArn}","${config.indexname}")
                def indexid = deserialize("${outobj}")
		echo "endpoint is ${indexid}"
		} catch (err) {
		currentBuild.result = 'FAILED'
		throw err
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

def createindex(bt,awsroleArn,name) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()

                def resource = getresources("${bt}")
                sh "chmod +x /tmp/$bt/install-cs.sh"
                sh("/tmp/$bt/install-cs.sh ${name}")
}
}

def getresources(directory)
{
                def resource = libraryResource "aws/cloudsearch/install-cs.sh"
                writeFile file: "/tmp/${directory}/install-cs.sh", text: "${resource}"
                def f = readFile file: "/tmp/${directory}/install-cs.sh"
                return f
}
return this;
