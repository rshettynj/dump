#! /bin/bash
PATH=/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin
export PATH
WS_DEFAULT_REGION=us-east-1
export AWS_DEFAULT_REGION
CS_HOME=/var/lib/jenkins/aws/cloud-search-tools-v2-2.0.1.0-2014.10.27
export CS_HOME
JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.85-2.6.1.2.el7_1.x86_64/jre
export JAVA_HOME

dev_env="dne.dev.web.us-east-1.aptechdevlab.com"
qa_env="dne.qa.web.us-east-1.aptechlab.com"
stage_env="dne.stage.web.us-east-1.aptechlab.com"
prod_env="dne.prod.web.us-east-1.associatedpress.com"

case $1 in 
   dev) echo "Environment is dev"
        envr="dne.dev.web.us-east-1.aptechdevlab.com"
        ;;
   qa)  echo "Environment is qa"
        envr="dne.qa.web.us-east-1.aptechlab.com"
        ;;
 stage) echo "Environment is stage"
        envr="dne.stage.web.us-east-1.aptechlab.com"
        ;;
  prod) echo "Environment is prod"
        envr="dne.prod.web.us-east-1.associatedpress.com"
        ;;
     *) echo "Wrong environment"
	exit
	;;
esac


elb_name=`nslookup $envr | grep -i webal | grep Name | tail -1| cut -f 2 -d ":" | sed -s "s/ //g"|xargs | tr '[:upper:]' '[:lower:]'`
echo "ELB for web servers is: $elb_name"

elb_arn=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 elbv2 describe-load-balancers | jq '.LoadBalancers[] | {DNSName, LoadBalancerArn}' |  tr '[:upper:]' '[:lower:]' |  jq --arg elb_name "$elb_name" '. | select(.dnsname == $elb_name)' | jq '.loadbalancerarn' | sed "s/ //g"`
echo "ARN for the ELB is $elb_arn"

target_arn=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 elbv2 describe-target-groups | jq '.TargetGroups[] | {LoadBalancerArns, TargetGroupArn}'  | tr '[:upper:]' '[:lower:]' | jq --arg arn_name "$elb_arn" '. | select(.loadbalancerarns[] |  contains ('"$elb_arn"'))' | jq '.targetgrouparn' | tail -1`
echo "Target ARN is $target_arn"

correct_target_arn=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 elbv2 describe-target-groups | jq '.TargetGroups[] | {LoadBalancerArns, TargetGroupArn}' | grep -i $target_arn | tail -1 | cut -f 4 -d ' ' | cut -f 1 -d "," | xargs`
echo "Corrected target ARN is $correct_target_arn"

target_healthy=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 elbv2 describe-target-health --target-group-arn "$correct_target_arn" | jq -r  '.TargetHealthDescriptions[] | .Target.Id'`
target_healthy_list=$(echo $target_healthy | tr " " "\n")
target_healthy_array=( $target_healthy )

instance_list=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 ec2 describe-instances --instance-id $target_healthy_list | jq '.' | jq '.Reservations[].Instances[].PrivateIpAddress' | sed "s/ //g" | xargs`
instance=$(echo $instance_list | tr " " "\n")
arr=( $instance )

echo "Instance IPs are: ${arr[*]}"

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t -i /tmp/keys/dne-${1}-key.pem centos@${arr[0]} "cd /mnt/gfs/files/log/files; sudo rm -rf av*"

for i in "${arr[@]}";
do
echo "IP is $i"

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -t -i /tmp/keys/dne-${1}-key.pem centos@${i} 'cd /mnt/gfs/files/log/files; sudo mkdir `hostname`; sudo chown -R nginx:nginx `hostname`; sudo service nginx restart; sudo service php-fpm restart; sudo service varnish restart; sudo service varnishncsa restart; sudo service rsyslog restart'

done
