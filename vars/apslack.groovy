def call(body) {

        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()

		try {
		  def downloadscmOUT = apslack("${config.slack_channel}")
		  return downloadscmOUT
		} catch (err) {
		currentBuild.result = 'FAILED'
		throw err
		}
}

def apslack(slack_channel) {
                stage 'notify via slack'
                slackSend channel: slack_channel, message: 'Build Started - ' + env.JOB_NAME + " " + env.BUILD_NUMBER + " " + env.BUILD_URL

}
return this;
