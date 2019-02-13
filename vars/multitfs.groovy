def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		try {
		deleteDir()
		def pname = getprojectname("${config.projectPath1}")
                stage 'reading current tfs changesets'
                   def lastsets = readtfschangesets(pname)
		stage 'starting scm on tfs'
		def tfs1 = downloadTfs {
		   serverUrl   = "${config.serverUrl1}"
		   projectPath = "${config.projectPath1}"
		}
		def tfs2 = downloadTfs {
   		   serverUrl   = "${config.serverUrl2}"
		   projectPath = "${config.projectPath2}"
		}
		stage 'compare changesets with previous run'
		def result = writetfschangesets(pname,[tfs1.TFS_CHANGESET,tfs2.TFS_CHANGESET])
		return result
		} catch (err) {
		currentBuild.result = 'FAILED'
		throw err
		}
}

def readtfschangesets(pname) {
                def file = "/tmp/tfs/${pname}/tfs_changesetR"
                def exists = fileExists "/tmp/tfs/${pname}/tfs_changesetR"
                if (exists) {
		//delete from local so we get the copy from artifactory
                sh(returnStdout: true, script: "rm ${file}")
                }
		//download the previous changeset from artifactory
                def dn = apArtifactory.downloadartifactory("APArtifactory","dne-infrastructure-build/tfs/${pname}/tfs_changeset","/tmp/tfs_changesetR")
                return dn
}

def writetfschangesets(pname,changeset) {
		//delete the previous copy if any
                def file = "/tmp/tfs/${pname}/tfs_changesetW"
                def exists = fileExists "/tmp/tfs/${pname}/tfs_changesetW"
                if (exists) {
                sh(returnStdout: true, script: "rm ${file}")
                echo "file ${file} is deleted locally"
                }
		//Write the changeset to the local file 
                writeFile file: "/tmp/tfs/${pname}/tfs_changesetW", text: "${changeset}"

		//reading file downloadd from artifactory into variable
                def filer
                exists = fileExists "/tmp/tfs/${pname}/tfs_changesetR"
                if (exists) {
                def filer1 = readFile file: "/tmp/tfs/${pname}/tfs_changesetR"
                filer =  "${filer1}".toString().trim()
                }
                else {
                filer = "not found"
                }
                echo "Filer is ${filer}"

                def filew1 = readFile file: "/tmp/tfs/${pname}/tfs_changesetW"
                def filew = "${filew1}".toString().trim()

		//compare files
                def out = comparefiles(filer,filew)
                if ("${out}" == "true") { echo "No build changes detected..."
		return 'nochange' }
                else { echo "Build changes detected..."
                def fn = apArtifactory.uploadartifactory("APArtifactory","/tmp/tfs/${pname}/tfs_changesetW","dne-infrastructure-build/tfs/${pname}/tfs_changeset")
                sh(returnStdout: true, script: "rm ${file}")
                return 'change'
                }
}

def randomfolder() {
	String randomString = org.apache.commons.lang.RandomStringUtils.random(9, true, true)
        return randomString
}

def resetchangeset(projectpath) {
	def pname = getprojectname(projectpath)
        writeFile file: "/tmp/tfs/${pname}/tfs_changesetW", text: "CLEARED"
	def fn = apArtifactory.uploadartifactory("APArtifactory","/tmp/tfs/${pname}/tfs_changesetW","dne-infrastructure-build/tfs/${pname}/tfs_changeset")
}

def getprojectname(projectname) {
   def out = projectname.split('/').minus('$')
   return out[0].toString().trim() + "/" + out[1].toString().trim()
}

def comparefiles(file1,file2) {
                if (file1 == file2) {
                   echo "Files match"
                   return true
                }
                else {
                   echo "Files do not match"
                   return false
               }
}

return this;
