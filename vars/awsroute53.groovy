def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		def bt = "${env.BUILD_TAG}"
                def directory = sh(returnStdout: true, script: "mkdir /tmp/\"${bt}\"")
		def resource = getresources("${bt}")
	        def out = deserialize(resource)
		switch ("${config.environment}") {
			case 'dne.dev':
				int count=0
				int idcounter=0
				def result
				def idlist = []
				hostedzonename = "aptechdevlab.com"
				hostedzoneid = "Z16I7PW5QGAK8Y"
				if ("${out.dne.dev.sites}" != "null") {
				stage "Add Rt53 alias for Websites"
				count=0
				while ("${out.dne.dev.sites[count]}" != "null") {
				echo "Adding DNS for site:  ${out.dne.dev.sites[count]}"
				result=upsertwebcname("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.dev.sites[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
				resultout=deserialize(result)
				idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
				count++
				idcounter++
				}
				count--
				}

				if ("${out.dne.dev.cachedomains}" != "null") {
				stage "Add Rt53 alias for elasticache"
				count=0
				while ("${out.dne.dev.cachedomains[count]}" != "null") {
				echo "Adding DNS for cache cluster..."
				result=upsertcachecname("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.dev.cachedomains[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
				resultout=deserialize(result)
				idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
			        count++
			        idcounter++
				}	
                                count--

				count=0
				}

				if ("${out.dne.dev.goliveswitch}" != "null") {
				stage "Add Rt53 alias for go live switch"
				count=0
				while ("${out.dne.dev.goliveswitch[count]}" != "null") {
				echo "Triggering go live with this stack version ${config.version}"
				result=upsertgolive("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.dev.goliveswitch[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
				resultout=deserialize(result)
				idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
				count++
				idcounter++
				}
				count--
				}

				idcounter--
				int counter=0
				def finalstatus
				echo "Here is the full list of IDs submitted for change: $idlist"
				stage "Waiting on Rt53 Insync status"
                                while ("$idcounter" >= 0) {
                                echo "Now checking DNS status for ${idlist[idcounter]}"
                                finalstatus = "PENDING"
                                while("${finalstatus}" != "INSYNC" && counter < 30) {
				echo "DNS UPSERT on ${idlist[idcounter]} still pending.."
                                counter++
                                sleep(10)
                                result=checkroute53rstatus("${bt}","${config.awsroleArn}","${idlist[idcounter]}")
                                resultout=deserialize(result)
                                finalstatus = resultout.ChangeInfo.Status
				if ( "${finalstatus}" == "INSYNC" ) { echo "${idlist[idcounter]} DNS Update Succeeded..." }
                                }
				idcounter--
                                }

				if ( "${finalstatus}" != "INSYNC" ) { currentBuild.result = 'FAILED' }

			break
			case 'dne.qa':
                                int count=0
                                int idcounter=0
                                def result
                                def idlist = []
                                hostedzonename = "aptechlab.com"
                                hostedzoneid = "ZFCNGY166H5RN"
                                if ("${out.dne.qa.sites}" != "null") {
				stage "Add Rt53 alias for Websites"
                                count=0
                                while ("${out.dne.qa.sites[count]}" != "null") {
                                echo "Adding DNS for site:  ${out.dne.qa.sites[count]}"
                                result=upsertwebcname("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.qa.sites[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
				}

				if ("${out.dne.qa.cachedomains}" != "null") {
				stage "Add Rt53 alias for elasticache"
                                count=0
                                while ("${out.dne.qa.cachedomains[count]}" != "null") {
                                echo "Adding DNS for cache cluster..."
                                result=upsertcachecname("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.qa.cachedomains[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
				}

                                if ("${out.dne.qa.goliveswitch}" != "null") {
				stage "Add Rt53 alias go live switch"
                                count=0
                                while ("${out.dne.qa.goliveswitch[count]}" != "null") {
                                echo "Triggering go live with this stack version ${config.version}"
                                result=upsertgolive("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.qa.goliveswitch[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
				}

                                idcounter--
                                int counter=0
                                def finalstatus
				echo "Here is the full list of IDs submitted for change: $idlist"
				stage "Waiting on Rt53 Insync status"
                                while ("$idcounter" >= 0) {
                                echo "Now checking DNS status for ${idlist[idcounter]}"
                                finalstatus = "PENDING"
                                while("${finalstatus}" != "INSYNC" && counter < 30) {
                                echo "DNS UPSERT on ${idlist[idcounter]} still pending.."
                                counter++
                                sleep(10)
                                result=checkroute53rstatus("${bt}","${config.awsroleArn}","${idlist[idcounter]}")
                                resultout=deserialize(result)
                                finalstatus = resultout.ChangeInfo.Status
                                if ( "${finalstatus}" == "INSYNC" ) { echo "${idlist[idcounter]} DNS Update Succeeded..." }
                                }
                                idcounter--
                                }

                                if ( "${finalstatus}" != "INSYNC" ) { currentBuild.result = 'FAILED' }

			break
			case 'dne.prod':
                                int count=0
                                int idcounter=0
                                def result
                                def idlist = []
                                hostedzonename = "associatedpress.com"
                                hostedzoneid = "Z1AB9MWRV4JE4L"
                                if ("${out.dne.prod.sites}" != "null") {
				stage "Add Rt53 alias for websites"
                                count=0
                                while ("${out.dne.prod.sites[count]}" != "null") {
                                echo "Adding DNS for site:  ${out.dne.prod.sites[count]}"
                                result=upsertwebcname("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.prod.sites[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
				}

                                if ("${out.dne.prod.cachedomains}" != "null") {
				stage "Add Rt53 alias for elasticache"
                                count=0
                                while ("${out.dne.prod.cachedomains[count]}" != "null") {
                                echo "Adding DNS for cache cluster..."
                                result=upsertcachecname("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.prod.cachedomains[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
				}

                                if ("${out.dne.prod.goliveswitch}" != "null") {
				stage "Add Rt53 alias go live switch"
                                count=0
                                while ("${out.dne.prod.goliveswitch[count]}" != "null") {
                                echo "Triggering go live with this stack version ${config.version}"
                                result=upsertgolive("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.prod.goliveswitch[count]}","${config.version}","${hostedzonename}","${hostedzoneid}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
				}

                                idcounter--
                                int counter=0
                                def finalstatus
				echo "Here is the full list of IDs submitted for change: $idlist"
				stage "Waiting on Rt53 Insync status"
                                while ("$idcounter" >= 0) {
                                echo "Now checking DNS status for ${idlist[idcounter]}"
                                finalstatus = "PENDING"
                                while("${finalstatus}" != "INSYNC" && counter < 30) {
                                echo "DNS UPSERT on ${idlist[idcounter]} still pending.."
                                counter++
                                sleep(10)
                                result=checkroute53rstatus("${bt}","${config.awsroleArn}","${idlist[idcounter]}")
                                resultout=deserialize(result)
                                finalstatus = resultout.ChangeInfo.Status
                                if ( "${finalstatus}" == "INSYNC" ) { echo "${idlist[idcounter]} DNS Update Succeeded..." }
                                }
                                idcounter--
                                }

                                if ( "${finalstatus}" != "INSYNC" ) { currentBuild.result = 'FAILED' }

			break
                        case 'dne.dr':
                                int count=0
                                int idcounter=0
                                def result
                                def idlist = []
                                hostedzonename = "associatedpress.com"
                                hostedzoneid = "Z1AB9MWRV4JE4L"
                                if ("${out.dne.dr.originservers}" != "null") {
                                stage "Add Rt53 alias for DR switch"
                                count=0
                                while ("${out.dne.dr.originservers[count]}" != "null") {
                                echo "Triggering go live with this stack version ${config.version}"
                                result=upsertgolivedr("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.dr.originservers[count]}","${config.version}","${hostedzonename}","${hostedzoneid}","${config.location}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
                                }

                                idcounter--
                                int counter=0
                                def finalstatus
                                echo "Here is the full list of IDs submitted for change: $idlist"
                                stage "Waiting on Rt53 Insync status"
                                while ("$idcounter" >= 0) {
                                echo "Now checking DNS status for ${idlist[idcounter]}"
                                finalstatus = "PENDING"
                                while("${finalstatus}" != "INSYNC" && counter < 30) {
                                echo "DNS UPSERT on ${idlist[idcounter]} still pending.."
                                counter++
                                sleep(10)
                                result=checkroute53rstatus("${bt}","${config.awsroleArn}","${idlist[idcounter]}")
                                resultout=deserialize(result)
                                finalstatus = resultout.ChangeInfo.Status
                                if ( "${finalstatus}" == "INSYNC" ) { echo "${idlist[idcounter]} DNS Update Succeeded..." }
                                }
                                idcounter--
                                }

                                if ( "${finalstatus}" != "INSYNC" ) { currentBuild.result = 'FAILED' }

                        break
                        case 'dne.drinteractive':
                                int count=0
                                int idcounter=0
                                def result
                                def idlist = []
                                hostedzonename = "associatedpress.com"
                                hostedzoneid = "Z1AB9MWRV4JE4L"
                                if ("${out.dne.drinteractive.originservers}" != "null") {
                                stage "Add Rt53 alias for DR switch"
                                count=0
                                while ("${out.dne.drinteractive.originservers[count]}" != "null") {
                                echo "Triggering go live with this stack version ${config.version}"
                                result=upsertgolivedr("${bt}","${config.awsroleArn}","${config.environment}","${out.dne.drinteractive.originservers[count]}","${config.version}","${hostedzonename}","${hostedzoneid}","${config.location}")
                                resultout=deserialize(result)
                                idlist[idcounter] = resultout.ChangeInfo.Id.toString().replaceAll("^/change/", "")
                                count++
                                idcounter++
                                }
                                count--
                                }

                                idcounter--
                                int counter=0
                                def finalstatus
                                echo "Here is the full list of IDs submitted for change: $idlist"
                                stage "Waiting on Rt53 Insync status"
                                while ("$idcounter" >= 0) {
                                echo "Now checking DNS status for ${idlist[idcounter]}"
                                finalstatus = "PENDING"
                                while("${finalstatus}" != "INSYNC" && counter < 30) {
                                echo "DNS UPSERT on ${idlist[idcounter]} still pending.."
                                counter++
                                sleep(10)
                                result=checkroute53rstatus("${bt}","${config.awsroleArn}","${idlist[idcounter]}")
                                resultout=deserialize(result)
                                finalstatus = resultout.ChangeInfo.Status
                                if ( "${finalstatus}" == "INSYNC" ) { echo "${idlist[idcounter]} DNS Update Succeeded..." }
                                }
                                idcounter--
                                }

                                if ( "${finalstatus}" != "INSYNC" ) { currentBuild.result = 'FAILED' }

                        break
		}	
 }

def upsertwebcname(bt,awsroleArn,environment,sourcename,version,hostedzonename,hostedzoneid) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 route53 change-resource-record-sets --hosted-zone-id $hostedzoneid --change-batch '{ \"Comment\": \"Added by Jenkins via shared library\", \"Changes\": [ { \"Action\": \"UPSERT\", \"ResourceRecordSet\": { \"Name\": \"${sourcename}.${environment}.web.us-east-1.$hostedzonename\", \"Type\": \"CNAME\", \"TTL\": 300, \"ResourceRecords\": [ { \"Value\": \"${sourcename}.${environment}.web.v${version}.us-east-1.$hostedzonename\" }]}}]}'")
		return currOUT
                }
}

def upsertcname(awsroleArn,environment,sourcename,destination,hostedzonename,hostedzoneid) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 route53 change-resource-record-sets --hosted-zone-id $hostedzoneid --change-batch '{ \"Comment\": \"Added by Jenkins via shared library\", \"Changes\": [ { \"Action\": \"UPSERT\", \"ResourceRecordSet\": { \"Name\": \"${sourcename}.$hostedzonename\", \"Type\": \"CNAME\", \"TTL\": 300, \"ResourceRecords\": [ { \"Value\": \"${destination}\" }]}}]}'")
		return currOUT
                }
}

def upsertcachecname(bt,awsroleArn,environment,sourcename,version,hostedzonename,hostedzoneid) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 route53 change-resource-record-sets --hosted-zone-id $hostedzoneid --change-batch '{ \"Comment\": \"Added by Jenkins via shared library\", \"Changes\": [ { \"Action\": \"UPSERT\", \"ResourceRecordSet\": { \"Name\": \"${environment}.${sourcename}.cfg.us-east-1.$hostedzonename\", \"Type\": \"CNAME\", \"TTL\": 300, \"ResourceRecords\": [ { \"Value\": \"${environment}.${sourcename}.cfg.v${version}.us-east-1.$hostedzonename\" }]}}]}'")
		return currOUT
                }
}

def upsertgolive(bt,awsroleArn,environment,sourcename,version,hostedzonename,hostedzoneid) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 route53 change-resource-record-sets --hosted-zone-id $hostedzoneid --change-batch '{ \"Comment\": \"Added by Jenkins via shared library\", \"Changes\": [ { \"Action\": \"UPSERT\", \"ResourceRecordSet\": { \"Name\": \"${environment}.web.us-east-1.$hostedzonename\", \"Type\": \"CNAME\", \"TTL\": 300, \"ResourceRecords\": [ { \"Value\": \"${environment}.web.v${version}.us-east-1.$hostedzonename\" }]}}]}'")
		return currOUT
                }
}

def upsertgolivedr(bt,awsroleArn,environment,sourcename,version,hostedzonename,hostedzoneid,location) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
		def currOUT
		if ("${location}" == "us-west-2") {
		echo "switching to WEST"
                currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 route53 change-resource-record-sets --hosted-zone-id $hostedzoneid --change-batch '{ \"Comment\": \"Added by Jenkins via shared library\", \"Changes\": [ { \"Action\": \"UPSERT\", \"ResourceRecordSet\": { \"Name\": \"${sourcename}\", \"Type\": \"CNAME\", \"TTL\": 60, \"ResourceRecords\": [ { \"Value\": \"dne.prod.web.v${version}.${location}.$hostedzonename\" }]}}]}'")
		}
		else {
		echo "switching to EAST"
                currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 route53 change-resource-record-sets --hosted-zone-id $hostedzoneid --change-batch '{ \"Comment\": \"Added by Jenkins via shared library\", \"Changes\": [ { \"Action\": \"UPSERT\", \"ResourceRecordSet\": { \"Name\": \"${sourcename}\", \"Type\": \"CNAME\", \"TTL\": 60, \"ResourceRecords\": [ { \"Value\": \"dne.prod.web.${location}.$hostedzonename\" }]}}]}'")
		}
		return currOUT
                }
}

def checkroute53rstatus(bt,awsroleArn,id) {
                if ("${awsroleArn}" != "null") {
                arn = "${awsroleArn}"
                def keysList = []
                keysList = assumeawsRole.getKeys(arn)
                env.AWS_ACCESS_KEY_ID = keysList[0].trim()
                env.AWS_SECURITY_TOKEN = keysList[1].trim()
                env.AWS_SECRET_ACCESS_KEY = keysList[2].trim()
                def currOUT = sh(returnStdout: true, script: "AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} AWS_SECURITY_TOKEN=${AWS_SECURITY_TOKEN} /usr/bin/aws --region us-east-1 route53 get-change --id $id")
		return currOUT
		}
}

def getresources(directory)
{
                def resource = libraryResource "aws/route53/resources.json" 
                writeFile file: "/tmp/${directory}/resources.json", text: "${resource}"
		def f = readFile file: "/tmp/${directory}/resources.json"
		return f
}

def deserialize(object) {
                def slurper = new groovy.json.JsonSlurperClassic()
                def result = slurper.parseText(object)
                return result
}

return this;
