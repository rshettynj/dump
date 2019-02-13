def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
                  stage 'New AWS LT with AMI'
                  if ("${config.awsregion}" == "null") {
                    awsregion = 'us-east-1'
                  }
                  else {
                    awsregion = 'us-west-2'
                  }
                  def bn
                  def imageid1
                  def imageid
                  if ("${config.versionnumber}" == "null") {
                  bn           = BUILD_NUMBER
                  imageid1     = readFile file: "output/ami.txt"
                  imageid      = "${imageid1}".toString().trim()
                  if ( "${imageid}" == "null" ) { imageid = "ami-da6fc3a0" }
                  }
                  else {
                  bn          = "${config.versionnumber}"
		  imageid     = "${config.amiid}"
                  }
		  def instancesize
                  if ("${config.instancesize}" != "null") {
		  instancesize   = "${config.instancesize}"
                  }
		else {
		  instancesize = "m5.large"
		  }

                  def currlc      = "${config.currentlc}" + '-' + "v${bn}"
                  def asg         = "${config.currentasg}" + '-' + "v${bn}"
                  def nwlt        = "${config.newlt}" + '-' + "v${bn}"

		  def lcparseOUT  = lcparse("${config.awsroleArn}","${currlc}","${awsregion}")
		  def lcnewOUT    = ltnewversion("${config.awsroleArn}","${nwlt}","${imageid}","${awsregion}","${instancesize}")
		  def asgparseOUT = asgparse("${config.awsroleArn}","${asg}","${awsregion}")
		  def attachlcOUT = attachlt("${config.awsroleArn}","${nwlt}","${config.ltversion}","${awsregion}")
		  def tagltOUT    = taglt("${config.awsroleArn}","${nwlt}","${awsregion}")
}

def lcparse(awsroleArn,currentlc,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} autoscaling describe-launch-configurations --query 'LaunchConfigurations[?starts_with(LaunchConfigurationName, `${currentlc}`) == `true`]' --output=text")
		writeFile file: "output/launchconfig.txt", text: "${currOUT}"
		return currOUT
}
}

def asgparse(awsroleArn,currentasg,awsregion) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                echo "role name is: ${arn}"
                def object1
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                def AWS_ACCESS_KEY_ID = keysList[0].trim()
                def AWS_SECURITY_TOKEN = keysList[1].trim()
                def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} autoscaling describe-auto-scaling-groups --query 'AutoScalingGroups[?starts_with(AutoScalingGroupName, `${currentasg}`) == `true`]' --output=text")
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

def ltnew(awsroleArn,newlt,imageid,awsregion) {
  def instancetype = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 5").trim()
  def instancekey = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 7").trim()
  def iaminstanceprofile = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 3").trim()
  def userdata = sh(returnStdout: true, script: "cat output/launchconfig.txt | head -1 | cut -f 11").trim()
  echo "Instance Type: ${instancetype}"
  echo "iaminstanceprofile : ${iaminstanceprofile}"
  echo "userdata: ${userdata}"
  def securitygroup = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep SECURITYGROUPS | sed \"s/SECURITYGROUPS//g\" | sed \"s/ //g\" ").trim()
  def instancemonitoring = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep INSTANCEMONITORING | sed \"s/INSTANCEMONITORING//g\" | sed \"s/ //g\"| tr '[:upper:]' '[:lower:]'").trim()
  def ebs = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep -w EBS | sed \"s/EBS//g\" | sed \"s/ //g\"").trim()
  def ebssize = sh(returnStdout: true, script: "echo $ebs | cut -f 1 -d ' '").trim()
  def ebstype = sh(returnStdout: true, script: "echo $ebs | cut -f 2 -d ' '").trim()
  def blockdevicemappings = sh(returnStdout: true, script: "cat output/launchconfig.txt | grep BLOCKDEVICEMAPPINGS  | sed \"s/BLOCKDEVICEMAPPINGS//g\" | sed \"s/ //g\"").trim()
  def imageid1 = imageid.trim()
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
    writeFile file: "output/ltdata.txt", text: "{\"KeyName\": \"${instancekey}\", \"SecurityGroupIds\": [\"$securitygroup\"], \"InstanceType\": \"$instancetype\", \"EbsOptimized\": true, \"IamInstanceProfile\": { \"Name\": \"$iaminstanceprofile\" }, \"UserData\": \"$userdata\", \"BlockDeviceMappings\": [{\"Ebs\":{\"VolumeSize\":$ebssize,\"VolumeType\":\"$ebstype\"},\"DeviceName\":\"$blockdevicemappings\"}], \"ImageId\": \"$imageid1\"}"
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 create-launch-template --launch-template-name ${newlt} --launch-template-data file://output/ltdata.txt")
  }
}

def ltnewversion(awsroleArn,newlt,imageid,awsregion,instancesize) {
  def imageid1 = imageid.trim()
  if ("${awsroleArn}" != "null") {
    arn = "${awsroleArn}"
    echo "role name is: ${arn}"
    def object1
    def keysList = []
    keysList = assumeawsRole.getKeys(arn)
    def AWS_ACCESS_KEY_ID = keysList[0].trim()
    def AWS_SECURITY_TOKEN = keysList[1].trim()
    def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 create-launch-template-version --launch-template-name ${newlt} --source-version 1 --launch-template-data ImageId=$imageid1,InstanceType=$instancesize")
}
}

def attachlt(awsroleArn,lt,version,awsregion) {
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
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} autoscaling update-auto-scaling-group --auto-scaling-group-name $asgname --launch-template LaunchTemplateName=$lt,Version=$version")
    }
}

def taglt(awsroleArn,lt,awsregion) {
  if ("${awsroleArn}" != "null") {
    arn = "${awsroleArn}"
    echo "role name is: ${arn}"
    def object1
    def keysList = []
    keysList = assumeawsRole.getKeys(arn)
    def AWS_ACCESS_KEY_ID = keysList[0].trim()
    def AWS_SECURITY_TOKEN = keysList[1].trim()
    def AWS_SECRET_ACCESS_KEY = keysList[2].trim()
    def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 describe-launch-templates --launch-template-names $lt | jq '.LaunchTemplates[].LaunchTemplateId' | sed \'s/\"//g\'").trim()
    echo "Template ID is $currOUT"
    def currOUT2 = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region ${awsregion} ec2 create-tags --resources $currOUT --tags Key=Name,Value=DNE Key=Services,Value=DNE")
}
}
return this
