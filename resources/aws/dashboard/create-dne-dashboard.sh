echo "{
    \"widgets\": [
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 2,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/ApplicationELB\", \"TargetResponseTime\", \"LoadBalancer\", \"$1\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"DNE ALB target response time\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 20,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/ApplicationELB\", \"RequestCount\", \"TargetGroup\", \"$2\", \"LoadBalancer\", \"$1\", { \"period\": 300, \"stat\": \"Sum\" } ],
                    [ \"...\", \"$3\", \".\", \".\", { \"period\": 300, \"stat\": \"Sum\" } ]
                ],
                \"period\": 60,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Sum\",
                \"title\": \"DNE ELB Request Count\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 2,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/ElastiCache\", \"CurrConnections\", \"CacheClusterId\", \"$4\" ],
                    [ \"...\", \"$5\" ],
                    [ \"...\", \"$6\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"Elasticache - Current Connections\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 26,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/EC2\", \"CPUUtilization\", \"AutoScalingGroupName\", \"$7\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"Web Stack : CPUUtilization , StatusCheckFailed \",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 14,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"EC2: DNE-NewRelic-BrowserLoad\", \"dne-newrelic-browserload\", \"InstanceId\", \"ELB\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stat\": \"Average\",
                \"title\": \"dne-newrelic-browserload \",
                \"yAxis\": {
                    \"left\": null,
                    \"right\": null
                }
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 8,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"EC2: DNE-NewRelic-Response\", \"dne-newrelic-responsetime\", \"InstanceId\", \"ELB\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stat\": \"Average\",
                \"title\": \"dne-newrelic-responsetime in seconds\",
                \"yAxis\": {
                    \"left\": null,
                    \"right\": null
                }
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 14,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/ApplicationELB\", \"HTTPCode_Target_5XX_Count\", \"LoadBalancer\", \"$1\", { \"region\": \"${11}\" } ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Sum\",
                \"view\": \"timeSeries\",
                \"yAxis\": {
                    \"left\": null,
                    \"right\": null
                }
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 8,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/ApplicationELB\", \"HealthyHostCount\", \"TargetGroup\", \"$2\", \"LoadBalancer\", \"$1\" ],
                    [ \"...\", \"$3\", \".\", \".\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"HealthyHostCount\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 20,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"CloudTrailMetrics\", \"NatGW-UtilizationOut\" ]
                ],
                \"period\": 3600,
                \"region\": \"${11}\",
                \"stat\": \"Sum\",
                \"yAxis\": {
                    \"left\": null,
                    \"right\": null
                }
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 12,
            \"y\": 32,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/RDS\", \"CPUUtilization\", \"DBInstanceIdentifier\", \"dnerds11952\" ],
                    [ \"...\", \"dnerds11952slave\" ],
                    [ \"...\", \"hostedrds2189\" ],
                    [ \"...\", \"hostedrds2189slave\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"DNE RDS CPUUtilization \",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 12,
            \"y\": 14,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/RDS\", \"DatabaseConnections\", \"DBInstanceIdentifier\", \"dnerds11952\" ],
                    [ \"...\", \"dnerds11952slave\" ],
                    [ \"...\", \"hostedrds2189\" ],
                    [ \"...\", \"hostedrds2189slave\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"DNE RDS DatabaseConnections \",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 32,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/CloudSearch\", \"SuccessfulRequests\", \"ClientId\", \"198401342403\", \"DomainName\", \"dne-bigstory-26038\" ],
                    [ \"...\", \"dne-collegefootball-32521\" ],
                    [ \"...\", \"dne-dne-2520\" ],
                    [ \"...\", \"dne-elections-4612\" ],
                    [ \"...\", \"dne-pro32-27860\" ],
                    [ \"...\", \"dne-racing-472\" ],
                    [ \"...\", \"dne-summergames-12105\" ],
                    [ \"...\", \"dne-wintergames-3616\" ],
                    [ \"...\", \"DomainName\", \"dne-hosted-8772\", \"ClientId\", \"198401342403\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"Cloudsearch SuccessfulRequests\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 12,
            \"y\": 26,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/ELB\", \"Latency\", \"LoadBalancerName\", \"dne-prod-as-WebELB-CNPGOHB7A2YR\", \"AvailabilityZone\", \"${11}\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"Cloudproxy ELB Latency\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 38,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/EC2\", \"CPUUtilization\", \"AutoScalingGroupName\", \"dne-prod-fpe-v2-FpeStack-1EPGAGVH547XM-FpeAG-9NG2EXJ34KCN\" ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stacked\": false,
                \"stat\": \"Average\",
                \"title\": \"FPE CPU Utilization\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 12,
            \"y\": 20,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/SQS\", \"ApproximateNumberOfMessagesVisible\", \"QueueName\", \"dne-prod-associatedpressproduction-us-east-1-cms-bstory-prod\" ],
                    [ \"...\", \"dne-prod-associatedpressproduction-us-east-1-cms-cbb-prod\" ],
                    [ \"...\", \"dne-prod-associatedpressproduction-us-east-1-cms-elections-prod\" ],
                    [ \"...\", \"dne-prod-associatedpressproduction-us-east-1-cms-olyp-prod\" ],
                    [ \"...\", \"dne-prod-associatedpressproduction-us-east-1-cms-pro32-prod\" ],
                    [ \"...\", \"dne-prod-associatedpressproduction-us-east-1-cms-racing-prod\" ],
                    [ \".\", \"ApproximateNumberOfMessagesNotVisible\", \".\", \"dne-prod-associatedpressproduction-us-east-1-cms-wgames-prod\", { \"yAxis\": \"right\" } ]
                ],
                \"period\": 300,
                \"region\": \"${11}\",
                \"stat\": \"Average\",
                \"title\": \"FPE SQS Messages Visible\",
                \"yAxis\": {
                    \"left\": null,
                    \"right\": null
                }
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 44,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"annotations\": {
                    \"alarms\": [
                        \"arn:aws:cloudwatch:us-east-1:198401342403:alarm:dne-sqlstate-pattern-errors\"
                    ]
                },
                \"stacked\": false,
                \"title\": \"dne-sqlstate-pattern-errors\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 66,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/EFS\", \"DataWriteIOBytes\", \"FileSystemId\", \"fs-1db77154\" ],
                    [ \"AWS/EFS\", \"DataReadIOBytes\", \"FileSystemId\", \"fs-1db77154\" ]
                ],
                \"region\": \"${11}\",
                \"stacked\": false,
                \"title\": \"EFS - DataReadIOBytes, DataWriteIOBytes\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 0,
            \"y\": 44,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"annotations\": {
                    \"alarms\": [
                        \"arn:aws:cloudwatch:us-east-1:198401342403:alarm:dne efs read\"
                    ]
                },
                \"stacked\": false,
                \"title\": \"dne efs read\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 38,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-interactives\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-tbs\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-cbb-widgets\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-cbb\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-cfb\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-cfb-widgets\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-pro32-widgets\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-pro32\", \"InstanceId\", \"ELB\" ]
                ],
                \"region\": \"${11}\",
                \"stacked\": false,
                \"title\": \"Realtime DNE Traffic from GA: dne-realtime-cbb, dne-realtime-cbb-widgets, dne-realtime-cfb, dne-realtime-cfb-widgets, dne-realtime-interactives, dne-realtime-pro32, dne-realtime-pro32-widgets, dne-realtime-tbs\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 6,
            \"y\": 50,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"EC2: DNE-Widget404\", \"dne-realtime-widget-pro32-404\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne-realtime-widget-cbb-404\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne-realtime-widget-cfb-404\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne-realtime-widget-tbs-404\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne-realtime-widget-racing-404\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne--realtime-widget-404-cfb\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne--realtime-widget-404-tbs\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne--realtime-widget-404-pro32\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne--realtime-widget-404-racing\", \"InstanceId\", \"ELB\" ],
                    [ \"EC2: DNE-Widget404\", \"dne--realtime-widget-404-cbb\", \"InstanceId\", \"ELB\" ]
                ],
                \"region\": \"${11}\",
                \"stacked\": false,
                \"title\": \"Realtime DNE 404: dne--realtime-widget-404-cbb, dne--realtime-widget-404-cfb, dne--realtime-widget-404-pro32, dne--realtime-widget-404-racing, dne--realtime-widget-404-tbs, dne-realtime-widget-cbb-404, dne-realtime-widget-cfb-404, dne-realtime-widget-pro32-404, dne-realtime-widget-racing-404, dne-realtime-widget-tbs-404\",
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 12,
            \"y\": 6,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"EC2: DNE-NewRelic-NginX\", \"dne-newrelic-nginx\", \"InstanceId\", \"ELB\" ]
                ],
                \"region\": \"${11}\",
                \"stacked\": false,
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 12,
            \"y\": 4,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"EC2: DNE-Traffic\", \"dne-realtime-wintergames\", \"InstanceId\", \"ELB\" ]
                ],
                \"region\": \"${11}\",
                \"stacked\": false,
                \"view\": \"timeSeries\"
            }
        },
        {
            \"type\": \"metric\",
            \"x\": 12,
            \"y\": 36,
            \"width\": 6,
            \"height\": 6,
            \"properties\": {
                \"metrics\": [
                    [ \"AWS/ApiGateway\", \"5XXError\", \"ApiName\", \"contentgateway\", { \"stat\": \"Sum\" } ],
                    [ \".\", \"Latency\", \".\", \".\" ],
                    [ \".\", \"Count\", \".\", \".\", { \"stat\": \"Sum\" } ],
                    [ \".\", \"IntegrationLatency\", \".\", \".\" ],
                    [ \".\", \"4XXError\", \".\", \".\", { \"stat\": \"Sum\" } ]
                ],
                \"view\": \"timeSeries\",
                \"stacked\": false,
                \"region\": \"${11}\",
                \"title\": \"Content Gateway Stats\",
                \"period\": 300
            }
       }
    ]
}
" >/tmp/dne-dashboard.json

dashbody=`cat /tmp/dne-dashboard.json | python -mjson.tool`
AWS_ACCESS_KEY_ID=$8 AWS_SECRET_ACCESS_KEY=$9 AWS_SECURITY_TOKEN=${10} /usr/bin/aws --region ${11} cloudwatch put-dashboard --dashboard-name "DNE-Dashboard" --dashboard-body "$dashbody"
