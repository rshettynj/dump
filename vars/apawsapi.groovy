def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
                  stage 'New AWS LC with AMI'
                  def bn
                  if ("${config.versionnumber}" == "null") {
                  bn          = BUILD_NUMBER
                  }
                  else {
                  bn          = "${config.versionnumber}"
                  }

                  def currlc      = "${config.currentlc}" + '-' + "v${bn}"
                  def asg         = "${config.currentasg}" + '-' + "v${bn}"
                  def nwlc        = "${config.newlc}" + '-' + "v${bn}"

                  def imageid1
                  def imageid
                  imageid1     = readFile file: "output/ami.txt"
                  imageid      = "${imageid1}".toString().trim()
                  if ( "${imageid}" == "null" ) { imageid = "ami-da6fc3a0" }

		  def lcparseOUT  = lcparse("${config.awsroleArn}","${currlc}")
		  def lcnewOUT    = lcnew("${config.awsroleArn}","${nwlc}","${imageid}")
		  def asgparseOUT = asgparse("${config.awsroleArn}","${asg}")
		  def attachlcOUT = attachlc("${config.awsroleArn}","${nwlc}")
}

def lcparse(awsroleArn,currentlc) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 autoscaling describe-launch-configurations --query 'LaunchConfigurations[?starts_with(LaunchConfigurationName, `${currentlc}`) == `true`]' --output=text")
		writeFile file: "output/launchconfig.txt", text: "${currOUT}"
		return currOUT
}
}

def asgparse(awsroleArn,currentasg) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 autoscaling describe-auto-scaling-groups --query 'AutoScalingGroups[?starts_with(AutoScalingGroupName, `${currentasg}`) == `true`]' --output=text")
                writeFile file: "output/asgconfig.txt", text: "${currOUT}"
                return currOUT
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

def lcnew(awsroleArn,newlc,imageid) {
  def instancetype = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 5").trim()
  def instancekey = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 7").trim()
  def iaminstanceprofile = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 3").trim()
  def userdata = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 11 | base64 -d").trim()
  echo "Instance Type: ${instancetype}"
  echo "iaminstanceprofile : ${iaminstanceprofile}"
  echo "userdata: ${userdata}"
  def securitygroup = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep SECURITYGROUPS | sed \"s/SECURITYGROUPS//g\" | sed \"s/ //g\" ").trim()
  def instancemonitoring = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep INSTANCEMONITORING | sed \"s/INSTANCEMONITORING//g\" | sed \"s/ //g\"| tr '[:upper:]' '[:lower:]'").trim()
  def ebs = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep -w EBS | sed \"s/EBS//g\" | sed \"s/ //g\"").trim()
  def ebssize = sh(returnStdout: true, script: "echo $ebs | cut -f 1 -d ' '").trim()
  def ebstype = sh(returnStdout: true, script: "echo $ebs | cut -f 2 -d ' '").trim()
  def blockdevicemappings = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep BLOCKDEVICEMAPPINGS  | sed \"s/BLOCKDEVICEMAPPINGS//g\" | sed \"s/ //g\"").trim()
  echo "instancekey: ${instancekey}"
  echo "securitygroup: ${securitygroup}"
  echo "instancemonitoring: ${instancemonitoring}"
  echo "blockdevicemappings: ${blockdevicemappings}"
  echo "ebssize: ${ebssize}"
  echo "ebstype: ${ebstype}"
  if ("${awsroleArn}" != "null") {
    arn = "${awsroleArn}"
    echo "role name is: ${arn}"
    def object1
    def keysList = []
    keysList = assumeawsRole.getKeys(arn)
    def AWS_ACCESS_KEY_ID = keysList[0].trim()
    def AWS_SECURITY_TOKEN = keysList[1].trim()
    def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
    def ser = "[{\"DeviceName\":\"$blockdevicemappings\",\"Ebs\":{\"VolumeSize\":$ebssize,\"VolumeType\":\"$ebstype\"}}]"
    echo "String is $ser"
    def out1 = serialize("${ser}")
    echo "serialized $out1"
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 autoscaling create-launch-configuration --launch-configuration-name ${newlc} --image-id $imageid --key-name $instancekey --security-groups $securitygroup --instance-type $instancetype --iam-instance-profile $iaminstanceprofile --user-data \"$userdata\" --block-device-mappings $out1 --no-ebs-optimized")
  }
}

def attachlc(awsroleArn,lc) {
  def fo = readFile file: "output/asgconfig.txt"
  def asgname = sh(returnStdout: true, script: "cat output/asgconfig.txt | head -1 | cut -f 2").trim()
  echo "ASG Name is $asgname"
  if ("${awsroleArn}" != "null") {
    arn = "${awsroleArn}"
    echo "role name is: ${arn}"
    def object1
    def keysList = []
    keysList = assumeawsRole.getKeys(arn)
    def AWS_ACCESS_KEY_ID = keysList[0].trim()
    def AWS_SECURITY_TOKEN = keysList[1].trim()
    def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 autoscaling update-auto-scaling-group --auto-scaling-group-name $asgname --launch-configuration-name $lc")
    }
}

def taggateway(awsroleArn,apiid,stagename,tags) {
  echo "tagging $stagename for $apiid"
  if ("${awsroleArn}" != "null") {
    arn = "${awsroleArn}"
    echo "role name is: ${arn}"
    def object1
    def keysList = []
    keysList = assumeawsRole.getKeys(arn)
    def AWS_ACCESS_KEY_ID = keysList[0].trim()
    def AWS_SECURITY_TOKEN = keysList[1].trim()
    def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 apigateway tag-resource --resource-arn arn:aws:apigateway:us-east-1::/restapis/${apiid}/stages/${stagename} --tags $tags")
    }
}

def disableasg(awsroleArn,asgname,region) {
  if ("${awsroleArn}" != "null") {
    arn = "${awsroleArn}"
    echo "role name is: ${arn}"
    def object1
    def keysList = []
    keysList = assumeawsRole.getKeys(arn)
    def AWS_ACCESS_KEY_ID = keysList[0].trim()
    def AWS_SECURITY_TOKEN = keysList[1].trim()
    def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region $awsregion autoscaling suspend-processes --auto-scaling-group-name $asgname
}

def enableasg(awsroleArn,asgname,region) {
  if ("${awsroleArn}" != "null") {
    arn = "${awsroleArn}"
    echo "role name is: ${arn}"
    def object1
    def keysList = []
    keysList = assumeawsRole.getKeys(arn)
    def AWS_ACCESS_KEY_ID = keysList[0].trim()
    def AWS_SECURITY_TOKEN = keysList[1].trim()
    def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region $awsregion autoscaling resume-processes --auto-scaling-group-name $asgname
}

return this

