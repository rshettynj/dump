 def call(body) {

        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		try {
		deleteDir()
                stage 'download-from-scm'
	        def out = checkout changelog: false, poll: false, scm: [$class: 'TeamFoundationServerScm', credentialsConfigurer: [$class: 'AutomaticCredentialsConfigurer'], projectPath: "${config.projectPath}", serverUrl: "${config.serverUrl}", useOverwrite: true, useUpdate: true, workspaceName: 'Hudson-${JOB_NAME}-${NODE_NAME}']
		return out
		} catch (err) {
		currentBuild.result = 'FAILED'
		throw err
		}
}
