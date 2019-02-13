def call(body) {

        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
			def downloadscmOUT = downloadscm("${config.branchName}","${config.repositoryName}")
			return downloadscmOUT
}

def downloadscm(branch,repository) {
		deleteDir()
                stage 'download-from-scm'
                def out = checkout([$class: 'GitSCM', branches: [[name: "${branch}"]], doGenerateSubmoduleConfigurations: false, extensions: [], gitTool: 'Default', submoduleCfg: [], userRemoteConfigs: [[url: "${repository}"]]])
		return out

}
return this;
