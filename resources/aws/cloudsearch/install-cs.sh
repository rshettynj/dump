#!/bin/bash
echo "This script will create an empty cloudsearch index for interactives"

core_name=`echo interactives-$1-$RANDOM`
echo $core_name

AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch  create-domain --domain-name $core_name
sleep 3
#Now create index schema
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "*" --type "literal" --facet-enabled true --search-enabled true --return-enabled true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "last_name" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "first_name" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "middle_name" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "suffix" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "title" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "birthdate" --type "date" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "birth_year" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "party" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "phone" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "residential_address" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "residential_unit" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "residential_city" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "country" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "state" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "zipcode" --type "text" --return-enabled  true
sleep 4
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-index-field --domain-name $core_name --name  "date_acquired" --type "text" --return-enabled  true
sleep 4

#create the suggester
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch define-suggester --domain-name $core_name --suggester '{ "SuggesterName": "residential_city", "DocumentSuggesterOptions": { "SourceField": "residential_city" } }'

echo "Waiting for endpoint.. this may take few minutes...."

value=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch describe-domains | grep "$core_name" | grep "search-" | grep Endpoint | cut -f 2 -d ':'`

if [[ -z $value ]]; then
counter="0"
while [ $counter -lt 400 ]
do
sleep 10
value=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 cloudsearch describe-domains | grep "$core_name" | grep "search-" | grep Endpoint | cut -f 2 -d ':'`
if [ ! -z $value ]; then
break
fi
counter=$[$counter+1]
done
fi

echo "Endpoint IS: $value"
echo "Your cloudsearch $domain $environment is setup. Endpoint is $value.."  | mailx -s "Cloudsearch domain created" rshetty@ap.org
