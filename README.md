# AP Shared Libraries for Jenkins
Shareable library functions for Jenkins pipeline. Devops can utilize the various sub functions (within the groovy files) to write their own pipeline or modify Jenkinsfile 
to call the existing groovy functions. You can also write scripts in any language and upload to resources directory and reference in the pipeline.
idea is to make sure there are hard coding in any functions and function is re-usable.

##Git layout
```
perproject  README.md   resources   vars
```

perproject contains Jenkinsfile for every pipeline.
example perproject/dne/JenkinsfileQA  perproject/dne/JenkinsfilePROD


## Jenkinsfile

Create your own Jenkinsfile and upload to perproject/ folder.
example: perproject/dne/JenkinsfileQA

reference Jenkinsfile in Pipeline:
```
Configure Pipeline. Change Definition to "Pipeline script from SCM"
SCM: Git
Repositories: url:  git@git.ap.org;devops/jenkins_shared_lib.git
Credentials: None (if already configured in GIT)
Branches:  Your code branch, example: master
Script Path:  Above path where Jenkinsfile is stored.
                example: perproject/dne/JenkinsfileQA
```            
## Resources:

To store any parameter or static file you want to store in SCM.
Directory to create in:  
```
resources/
```
existing example: 
```
resources/aws/cloudformation/resources.json
resources/aws/elb/healthcheck.sh
```

## Groovy Functions:

* [downloadScm.groovy]() - Download SCM
* [downloadTfs.groovy]() - Download SCM TFS
* [multitfs.groovy]() - Download multiple SCM TFS (limited to two for now)
* [apArtifactory.groovy]() - Upload to and Download from artifactory.ap.org
* corpimage.groovy -Get the imageid of the AP created standard ec2 images
* assumeawsRole.groovy - Assume an AWS role
* awsCfops.groovy - Create cloudformation template in AWS
* awshealthcheck.groovy - AWS service health check.  url health check and/or ALB healthcheck currently supported.
* awss3ops.groovy - AWS S3 Operations.
* image.groovy - Create AWS Image
* buildpath.groovy  Return the build path for current build. referenced by other stages.
* awss3api.groovy Has functions for s3 bucket notifications.
	deleteNotification	Specify the notification name. All other notifications will not be deleted.
	deleteAllNotification	Delete ALL notifications for the bucket
	notificationJson	Json formatted input that has notification (Do not add start and closing parenthesis)
* apawsapi.groovy - Update AWS Autoscaling Group with new Launch configuration (Attach new LC to an ASG)
    currentlc  - LC to be used as source to create new LC
    newlc      - Name of the new LC
    currentasg - Name of the ASG to attach to.
* cleanupefs.groovy - Cleanup AWS EFS to remove any unwanted files. Specifically cleanup old stack instances directory containing log files
* route53.groovy - Add route53 dns aliases from a resource json file. Mainly used for go live type changes.

```
downloadScm.groovy  
    parameters: branchName/repositoryName
    example:
    @Library("dne-shared-library") _
    downloadScm {
        branchName = "nonprod"
        repositoryName = "git@git.ap.org:gi/dne.git"
    }

downloadTfs.groovy
    parameters: serverUrl/projectPath
    example:
    @Library("dne-shared-library") _
    import hudson.plugins.tfs.TeamFoundationServerScm
    downloadTfs {
        serverUrl = "http://ctcitfs.ap.org:8080/tfs/eAP"
        projectPath = "\$/CoreServices/AP.Ingestion/Work"
    }

apArtifactory.groovy  
    dowload parameters: artifactoryName/downloadsourcelist/destinationlist
    upload parameters: artifactoryName/uploadsourcelist/destinationlist
    example 1:
    @Library("dne-shared-library") _
    apArtifactory {
        artifactoryName = "artifactory"
        uploadsourcelist = [ "/tmp/install_script_web.bash" ]
        destinationlist = [ "dne-infrastructure-build/qa/install_script_web.bash" ]
    }
    example 2:
    apArtifactory {
        artifactoryName = "artifactory"
        uploadsourcelist = [ "/tmp/mydir/" ]
        destinationlist = [ "dne-infrastructure-build/qa/" ]
    }
    example 3:
    apArtifactory {
        artifactoryName = "artifactory"
        downloadsourcelist = [ "dne-infrastructure-build/qa/" ]
        destinationlist = [ "/tmp/" ]
    }
    example 4:
    apArtifactory {
        artifactoryName = "artifactory"
        downloadsourcelist = [ "dne-infrastructure-build/qa/myfile.txt" ]
        destinationlist = [ "/tmp/" ]
    }
    //artifactory name is pre configured in Jenkins as "artifactory" so using that shortname
    //uploadsourcelist and downloaddsourcelist You can specify multiple files comma separated.
    //destinationlist You can specify multiple files comma separated must match source one to one.
    //If you specify directory for filesourcelist, all files in that directory will be uploaded. Provide a directory in artifactory for destination. Destination is auto-created.

corpimage.groovy 
    parameter   :   awsroleArn/createdate/filterpattern
    example:
    @Library("dne-shared-library") _
    import java.util.regex.Matcher
    corpimage {
        awsroleArn = "arn:aws:iam::XXXXXXX:role/dne-jenkins-crossaccount-role"
        createdate = "2017-09-10"
        filterpattern = "CentOS7-AP-GOLD-pe-"
    }
    //If there are multiple images created after the specified date, you will get one random image so be specific with date.
    
assumeawsRole.groovy  
    parameters  :   awsroleArn
    example:
    @Library("dne-shared-library") _
    import java.util.regex.Matcher
    import java.math.MathContext
    assumeawsRole {
        awsroleArn = "arn:aws:iam::XXXXXXX:role/dne-jenkins-crossaccount-role"
    }
    //AWS_ACCESS_KEY_ID, AWS_SECURITY_TOKEN and AWS_SECRET_ACCESS_KEY will be available for one hour in your build. These variables are written into build path. 
    A new keys is created every 15 minutes in the build if you use the default groovy file provided here.
    
awsCfops.groovy  
    parameters  :   createStack/resourcePath/environment/awsroleArn/retrycount/sleeptime
    example:
    @Library("dne-shared-library") _
    import groovy.json.JsonSlurperClassic
    import groovy.json.JsonOutput
    import java.util.regex.Matcher
    awsCfops {
        createStack = "Y"   //Either Y or do not specify these parameter
        environment = "dne.dev" //must match the resources json structure you entered in resources file (mentioned above.)
        retrycount = "30"  //How many retries to check if stack status is completed.
        sleeptime = "60"  //How many minutes to sleep in seconds.
        resourcePath = "aws/cloudformation/resources.json"    //resources file location (mentioned above.)
        awsroleArn = "arn:aws:iam::XXXXXXX:role/dne-jenkins-crossaccount-role"  //AWS Role to use for stack operation. Make sure role has all the necessary permissions.
    }

awshealthcheck.groovy  
    parameters  :   resourcePath/retrycount/sleeptime/awsroleArn OR  urlname/retrycount/sleeptime
    examples:
    @Library("dne-shared-library") _
    import groovy.json.JsonSlurperClassic
    import groovy.json.JsonOutput
    awshealthcheck {
        retrycount = "3"  //retry count
        sleeptime = "10" //sleep time in seconds
        urlname = "http://dne.qa.web.v41.us-east-1.aptechlab.com"  //publically accessible url to poll
    }
    @Library("dne-shared-library") _
    import groovy.json.JsonSlurperClassic
    import groovy.json.JsonOutput
    awshealthcheck {
        resourcePath = "aws/elb/healthcheck.sh" //Your customized script in the resources directory (mentioned above) that has the script to run ALB healthcheck or 
                any script for that matter.
        retrycount = "3"
        sleeptime = "10"
        awsroleArn = "arn:aws:iam::XXXXXXX:role/dneqa-jenkins-crossaccount-role"
    }
    
awss3ops.groovy 
    parameters  :   uploadfilelist/destinationlist/bucketName/awsroleArn downloadfilelist/destinationlist/bucketName/awsroleArn 
                uploaddirectorylist/destinationlist/bucketName/awsroleArn downloaddirectorylist/destinationlist/bucketName/awsroleArn
    examples:
    awss3ops {
        bucketName = "dne-dev-apcapdevelopment-us-east-1-web"
        uploadfilelist = [ "Web/CloudFormation/web.template","Web/CloudFormation/dev_delta/dne.template","Web/CloudFormation/cache.template","Web/Scripts/install_script_web.bash","/tmp/prod.zip" ]
        destinationlist = [ "CloudFormation/web.template", "CloudFormation/dne.template","CloudFormation/cache.template","Scripts/install_script_web.bash","Puppet/prod.zip" ]
        awsroleArn = "arn:aws:iam::XXXXXXX:role/dne-jenkins-crossaccount-role"
    }

image.groovy  
    parameters  :   awsroleArn/instanceid
    example:
    @Library("dne-shared-library") _
    import groovy.json.JsonSlurperClassic
    import groovy.json.JsonOutput
    import java.util.regex.Matcher
    image {
        instanceid = "i-02875f94ccf329d85"
        awsroleArn = "arn:aws:iam::XXXXXXX:role/dne-jenkins-crossaccount-role"
    }

awss3api.groovy
    parameters  :   bucketName/awsroleArn/notificationJson
    example:
	@Library("dne-shared-library") _
	import groovy.json.JsonSlurperClassic
	import groovy.json.JsonOutput
	import java.util.regex.Matcher
    awss3api {
        bucketName = "dne-qa-associatedpressqa-us-east-1-web"
        awsroleArn = "arn:aws:iam::XXXXXXXXXXX:role/dneqa-jenkins-crossaccount-role"
        notificationJson = '"QueueConfigurations": [{"Id": "roshan-test","QueueArn": "arn:aws:sqs:us-east-1:720322524327:roshan-test","Events": ["s3:ReducedRedundancyLostObject"]}]'
    }

    awss3api {
        bucketName = "dne-qa-associatedpressqa-us-east-1-web"
        awsroleArn = "arn:aws:iam::XXXXXXXXXXX:role/dneqa-jenkins-crossaccount-role"
        deleteAllNotification = "Y"
    }
    awss3api {
        bucketName = "dne-qa-associatedpressqa-us-east-1-web"
        awsroleArn = "arn:aws:iam::XXXXXXXXXXX:role/dneqa-jenkins-crossaccount-role"
        deleteNotification = "roshan-test"
    }

apawsapi {
        awsroleArn = "arn:aws:iam::222259241209:role/dne-jenkins-crossaccount-role"
        currentlc  = "dne-dev-associatedpressdev-web-full-v4"
        currentasg = "dne-dev-associatedpressdev-web-full-v4"
        newlc      = "dne-dev-associatedpressdev-web-full-v4-updated"
    }

apslack {
  slack_channel = "random"
}

@Library("dne-shared-library") _
node ('unixaws') {
        try {
                deleteDir()
    			def out = multitfs {
    			    serverUrl1   = "http://ctcitfs.ap.org:8080/tfs/eAP"
    			    projectPath1 = "\$/EditorialApplications/Exposure/DotNet/release"
    			    serverUrl2   = "http://ctcitfs.ap.org:8080/tfs/eAP"
    			    projectPath2 ="\$/EditorialApplications/Exposure/Node/release"
    			}
    			if ("${out}" == "change") {
    			   echo "BUILD UPDATED.. KICK OFF ANOTHER PIPELINE AS NEEDED" }
    			if ("${out}" == "nochange") {
    			   echo "NO BUILD CHANGE.. NOTHING TO KICKOFF" }
    			  // echo "{$nonexistingvariable}"   force failure by writing out non existing variable so control goes to catch(err) section.
              } catch (err) {
                projectPath1 = "\$/EditorialApplications/Exposure/DotNet/release"
                multitfs.resetchangeset(projectPath1)
                currentBuild.result = 'FAILED'
                throw err
                }

}

route53

node ('unixaws') {
        try {
             deleteDir()
            awsroute53 {
                awsroleArn      = "arn:aws:iam::222259241209:role/dne-jenkins-crossaccount-role"   //Pass your ARN for the role
                environment     = "dne.dev"  //Pass the environment. This must be defined in the json file.
                version         = "61"  //Pass the version of the stack you tested and want to make live. Do not add "v" before the number, it is in the library.
                resourcePath    = "aws/route53/resources.json"   //path where you have the resource file in json format.
            }
        } catch (err) {
                currentBuild.result = 'FAILED'
                throw err
                }
}


buildpath.groovy
```

## Versioning

For the versions available, see the [tags on this repository](https://git.ap.org/devops/jenkins_shared_lib/tags). 

## Authors

* Roshan Shetty (rshetty@ap.org)

