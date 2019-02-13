def call(body) {
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()
  if ("${config}".contains('uploadsourcelist') == true) {
    echo "Uploading file or files..."
    int count=0
    while ("${config.uploadsourcelist[count]}" != "null") {
      def uploadArtifactoryOUT = uploadartifactory("${config.artifactoryName}","${config.uploadsourcelist[count]}","${config.destinationlist[count]}")
      count++
    }
  }
  if ("${config}".contains('downloadsourcelist') == true) {
    echo "Downloading file or files..."
    int count=0
    while ("${config.downloadsourcelist[count]}" != "null") {
      def downloaddArtifactoryOUT = downloadartifactory("${config.artifactoryName}","${config.downloadsourcelist[count]}","${config.destinationlist[count]}")
      count++
    }
  }
}

def uploadartifactory(name,source,destination) {
  stage 'upload-to-artifactory'
  def server = Artifactory.server "${name}"
  def build_info = Artifactory.newBuildInfo()
  build_info.env.capture = true
  def sval = "${source}"
  def dval = "${destination}"
  echo "$sval $dval"
  def uploadSpec = """{
    "files": [
      {
        "pattern": "${sval}",
        "target": "${dval}"
      }
    ]
  }"""
  server.upload spec: uploadSpec, buildInfo: build_info
  server.publishBuildInfo build_info
}

def downloadartifactory(name,source,destination) {
  stage 'download-from-artifactory'
  def server = Artifactory.server "${name}"
  def sval = "${source}"
  def dval = "${destination}"
  echo "$sval $dval"
  def downloadSpec = """{
    "files": [
      {
        "pattern": "${sval}",
        "target": "${dval}"
      }
    ]
  }"""
  server.download spec: downloadSpec
}

return this
