def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		try {
                keysList = getKeys("${config.awsroleArn}")
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		sleep(5)
		keysList = getKeys("${config.awsroleArn}")
                AWS_ACCESS_KEY_ID = keysList[0].trim()
                AWS_SECURITY_TOKEN = keysList[1].trim()
                AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		} catch (err) {
                currentBuild.result = 'FAILED'
                throw err
                }
}

def getKeys(role) {
                def roleName = sh(returnStdout: true, script: "echo ${role} | cut -f 5,6 -d ':'")
                echo "Role name is: ${roleName}"
                roleNameout = roleName.replaceAll("\\P{Alnum}", "");
		def dt1 = new Date().getTime()/1000
		def dt2 = sh(returnStdout: true, script: "echo \"${dt1}\" | cut -f 1 -d '.'")
		def dt = "${dt2}".toInteger()
		def timediff
		dir ('/tmp/awscred/') { writeFile file:'dummyfile', text:'' }
		def exists = fileExists "/tmp/awscred/${roleNameout}/DATE"
		if (exists) {
		def rd1 = readFile file: "/tmp/awscred/${roleNameout}/DATE"
		def rd = "${rd1}".trim().toInteger()
		timediff = dt - rd
		} else {
		timediff = dt
		}
		if ( "${timediff}".toInteger() > "900".toInteger() ) 
		{
		echo "CREATING NEW AWS KEYS..."
                sh("/usr/bin/aws sts assume-role --role-arn ${role} --role-session-name rolename | tee /tmp/awscred.txt")
                sh 'cat /tmp/awscred.txt |  grep -w \'AccessKeyId\\|SecretAccessKey\\|SessionToken\'  | awk  \'{print $2}\' | sed  \'s/\\"//g;s/\\,//\' | tee /tmp/awscred1.txt'
                def AWS_ACCESS_KEY_ID = sh(returnStdout: true, script: "cat /tmp/awscred1.txt |sed -n \'3p\'")
                def AWS_SECURITY_TOKEN = sh(returnStdout: true, script: "cat /tmp/awscred1.txt |sed -n \'2p\'")
                def AWS_SECRET_ACCESS_KEY = sh(returnStdout: true, script: "cat /tmp/awscred1.txt |sed -n \'1p\'")
                if ("${AWS_ACCESS_KEY_ID.length()}" != "0") {
		writeFile file: "/tmp/awscred/${roleNameout}/AWS_ACCESS_KEY_ID", text: "${AWS_ACCESS_KEY_ID}"
		writeFile file: "/tmp/awscred/${roleNameout}/AWS_SECURITY_TOKEN", text: "${AWS_SECURITY_TOKEN}"
		writeFile file: "/tmp/awscred/${roleNameout}/AWS_SECRET_ACCESS_KEY", text: "${AWS_SECRET_ACCESS_KEY}"
		writeFile file: "/tmp/awscred/${roleNameout}/DATE", text: "${dt}"
                return [ AWS_ACCESS_KEY_ID,AWS_SECURITY_TOKEN,AWS_SECRET_ACCESS_KEY ]
		}
		}
		else
		{
                def AWS_ACCESS_KEY_ID = readFile file: "/tmp/awscred/${roleNameout}/AWS_ACCESS_KEY_ID"
                def AWS_SECURITY_TOKEN = readFile file: "/tmp/awscred/${roleNameout}/AWS_SECURITY_TOKEN"
                def AWS_SECRET_ACCESS_KEY = readFile file: "/tmp/awscred/${roleNameout}/AWS_SECRET_ACCESS_KEY"
		return [ AWS_ACCESS_KEY_ID,AWS_SECURITY_TOKEN,AWS_SECRET_ACCESS_KEY ]
		}
		
}
return this;
