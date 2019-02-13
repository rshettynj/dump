#! /bin/bash

alb() {

PATH=/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin
export PATH

AWS_DEFAULT_REGION=$3
export AWS_DEFAULT_REGION
CS_HOME=/var/lib/jenkins/aws/cloud-search-tools-v2-2.0.1.0-2014.10.27
export CS_HOME
JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.85-2.6.1.2.el7_1.x86_64/jre
export JAVA_HOME

elb_name=`nslookup $1 | grep -i webal | grep Name | tail -1| cut -f 2 -d ":" | sed -s "s/ //g"|xargs | tr '[:upper:]' '[:lower:]'`
echo "ELB for web servers is: $elb_name"

if [ ${#elb_name} -eq 0 ]; then exit 1
fi

awsregion=$3

elb_arn=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region $awsregion elbv2 describe-load-balancers | jq '.LoadBalancers[] | {DNSName, LoadBalancerArn}' |  tr '[:upper:]' '[:lower:]' |  jq --arg elb_name "$elb_name" '. | select(.dnsname == $elb_name)' | jq '.loadbalancerarn' | sed "s/ //g"`
echo "ARN for the ELB is $elb_arn"

target_arn=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region $awsregion elbv2 describe-target-groups | jq '.TargetGroups[] | {LoadBalancerArns, TargetGroupArn}'  | tr '[:upper:]' '[:lower:]' | jq --arg arn_name "$elb_arn" '. | select(.loadbalancerarns[] |  contains ('"$elb_arn"'))' | jq '.targetgrouparn' | tail -1`
echo "Target ARN is $target_arn"

correct_target_arn=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region $awsregion elbv2 describe-target-groups | jq '.TargetGroups[] | {LoadBalancerArns, TargetGroupArn}' | grep -i $target_arn | tail -1 | cut -f 4 -d ' ' | cut -f 1 -d "," | xargs`
echo "Corrected target ARN is $correct_target_arn"

target_healthy=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region $awsregion elbv2 describe-target-health --target-group-arn "$correct_target_arn" | jq -r  '.TargetHealthDescriptions[] | .Target.Id + "=" + .TargetHealth.State'`
target_healthy_list=$(echo $target_healthy | tr " " "\n")
target_healthy_array=( $target_healthy )

if [ ${#target_healthy_array[@]} -eq 0 ]; then
echo "No hosts are healthy now..."
exit 1
fi

for i in "${target_healthy_array[@]}";
do
instance_candidate=`echo $i | cut -f 1 -d "="`
instance_state=`echo $i | cut -f 2 -d "="`
echo "---------------------------------"
echo "instance_candidate is $instance_candidate"
echo "instance state is $instance_state"
if [ $instance_state == "healthy" ];
then
instance_candidate_ip=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region $awsregion ec2 describe-instances --instance-id $instance_candidate | jq '.' | jq '.Reservations[].Instances[].PrivateIpAddress' | sed "s/ //g" | xargs`
break;
else
echo "$instance_candidate is not healthy"
echo "---------------------------------"
fi
done

if [ -z "$instance_candidate_ip" ]; then
echo "No healthy hosts found so exiting..."
exit 1
fi

for i in "${target_healthy_array[@]}";
do
instance_candidate=`echo $i | cut -f 1 -d "="`
instance_state=`echo $i | cut -f 2 -d "="`
if [ $instance_state == "healthy" ];
then
instance_list+=`echo $instance_candidate`" "
fi
done

echo "Instance list attached to load balancer is $instance_list"

instance_list=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region $awsregion ec2 describe-instances --instance-id $instance_list | jq '.' | jq '.Reservations[].Instances[].PrivateIpAddress' | sed "s/ //g" | xargs`
instance=$(echo $instance_list | tr " " "\n")
arr=( $instance )

echo "Instance IPs are: ${arr[*]}"
echo "$instance_candidate" >/tmp/$2/instanceid.txt
exit 0
}


elb() {

PATH=/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin
export PATH

AWS_DEFAULT_REGION=us-east-1
export AWS_DEFAULT_REGION
CS_HOME=/var/lib/jenkins/aws/cloud-search-tools-v2-2.0.1.0-2014.10.27
export CS_HOME
JAVA_HOME=/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.85-2.6.1.2.el7_1.x86_64/jre
export JAVA_HOME


elb_name=`nslookup $1 | grep webelb | grep Name | tail -1| cut -f 2 -d ":" | sed -s "s/ //g"|xargs | tr '[:upper:]' '[:lower:]'`
echo "ELB for web servers is: $elb_name"

if [ ${#elb_name} -eq 0 ]; then exit 1
fi

aws_elb_name=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 elb describe-load-balancers | jq '.LoadBalancerDescriptions[] | {DNSName, Instances,LoadBalancerName}'  | tr '[:upper:]' '[:lower:]' |  jq --arg elb_name "$elb_name" '. | select(.dnsname == $elb_name)' | jq '.loadbalancername' | sed "s/ //g" | xargs`

actual_aws_elb_name=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 elb describe-load-balancers | jq '.LoadBalancerDescriptions[] | {LoadBalancerName}' | grep -i $aws_elb_name | cut -f 2 -d : | sed "s/ //g" | xargs`

elb_instance_state=`AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY /usr/bin/aws --region us-east-1 elb describe-instance-health --load-balancer-name $actual_aws_elb_name | jq '.InstanceStates[].State'`

echo $elb_instance_state | grep InService

if [ $? != 0 ]; then
echo "FAIL;None of the backend instances is in service"
exit 1
fi

echo "OK; At least one instance is in service"
exit 0
}

check_elb=`nslookup $1 | grep webal`
if [ $? == "0" ];
then
alb $1 $2 $3
else
elb $1
fi
