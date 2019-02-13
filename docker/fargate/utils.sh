AWS_DEFAULT_REGION=us-east-1
export AWS_DEFAULT_REGION

aws sts assume-role --role-arn "arn:aws:iam::559994907943:role/role-2" --role-session-name "role-2" | grep -w 'AccessKeyId\|SecretAccessKey\|SessionToken' | awk  '{print $2}' | sed  's/\"//g;s/\,//' >/tmp/aws.out

AWS_ACCESS_KEY_ID=`sed -n '3p' /tmp/aws.out`
AWS_SECRET_ACCESS_KEY=`sed -n '1p' /tmp/aws.out`
AWS_SECURITY_TOKEN=`sed -n '2p' /tmp/aws.out`

AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY AWS_SECURITY_TOKEN=$AWS_SECURITY_TOKEN aws ec2 ${1}-instances --instance-id i-04f38a2e80028e4a7
