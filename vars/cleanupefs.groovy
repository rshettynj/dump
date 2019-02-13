def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		def bt = "${env.BUILD_TAG}"
                def directory = sh(returnStdout: true, script: "mkdir -p /tmp/\"${bt}\"")
			//def resource = getresources("${bt}","${config.resourcePath}")
			efscleanup("${bt}","${config.awsroleArn}","${config.envr}")
 }

def efscleanup(bt,awsroleArn,envirn) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		def efsout = apArtifactory.downloadartifactory("APArtifactory","dne-infrastructure-build/keys/dne-${envirn}-key.pem","/tmp/dne-${envirn}-key.pem")
                sh "chmod 600 /tmp/keys/dne-${envirn}-key.pem"
 		def resource = getresources("${bt}")
                sh "chmod +x /tmp/$bt/cleanup.sh"
                sh "/tmp/$bt/cleanup.sh ${envirn}"
                sh "rm -rf /tmp/keys/dne-${envirn}-key.pem"
                }
}

def getresources(directory)
	{
                def resource = libraryResource "aws/efs/cleanup.sh" 
                writeFile file: "/tmp/${directory}/cleanup.sh", text: "${resource}"
		def f = readFile file: "/tmp/${directory}/cleanup.sh"
		return f
	}
return this;
