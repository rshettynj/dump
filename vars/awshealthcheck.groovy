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
                def directory = sh(returnStdout: true, script: "mkdir /tmp/\"${bt}\"")
		if ("${config}".contains( 'servicename') == true) { 
				servicehealthcheck("${bt}","${config.awsroleArn}","${config.servicename}","${awsregion}")
			}
                if ("${config}".contains( 'urlname') == true) {
                                retry("${config.retrycount}".toInteger()) {
                                sleep("${config.sleeptime}".toInteger())
                                def response = urlhealthcheck("${config.urlname}","${awsregion}")
                                }
                        }
 }

def servicehealthcheck(bt,awsroleArn,servicename,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
	
 		def resource = getresources("${bt}")
		echo "AWS region is: ${awsregion}"

                sh "chmod +x /tmp/$bt/healthcheck.sh"
                sh("/tmp/$bt/healthcheck.sh ${servicename} ${bt} ${awsregion}")
                }
}

def urlhealthcheck(urlname) {
		def response = httpRequest "${urlname}"
		return response
}

def getresources(directory)
	{
                def resource = libraryResource "aws/elb/healthcheck.sh" 
                writeFile file: "/tmp/${directory}/healthcheck.sh", text: "${resource}"
		def f = readFile file: "/tmp/${directory}/healthcheck.sh"
                //File f = new File("/tmp/${directory}/healthcheck.sh")
                //def data = f.getText()
		return f
	}
return this;
