#!/bin/bash

workingDir=/tmp
logFile=$workingDir/deploayRabbitMQServer.log
ipAddrStarting=10
securityToken=rabbitMqDns

. /etc/environment

mkdir $workingDir

sudo apt-get -y install erlang

wget --directory-prefix=$workingDir https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_5_3/rabbitmq-server-generic-unix-3.5.3.tar.gz

if [ $? -ne 0 ]; then
	echo "Could not download rabbitMQ tar file!" >> $logFile
	exit
fi

tar -xf $workingDir/rabbitmq-server-generic-unix-3.5.3.tar.gz --directory=$workingDir

if [ $? -ne 0 ]; then
	echo "Could not extract rabbitMQ!" >> $logFile
	exit
fi

echo "PDNSIP:$rabbitServerToPowerDns_IP" >> $logFile

myIpsString=$(ip addr | grep "inet $ipAddrStarting" | grep -E -o '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
IFS=' ' read -a myIps <<< $myIpsString
myIp=${myIps[0]}

hostName=hostname --long

# Add a new record to the new zone (would replace any existing test.example.org/A records)
curl -X PATCH --data '{"rrsets": [ {"name": "'$hostName'", "type": "A", "changetype": "REPLACE", "records": [ {"content": "'$myIp'", "disabled": false, "name": "'$hostName'", "ttl": 86400, "type": "A" } ] } ] }' -H "X-API-Key: $securityToken" http://$rabbitServerToPowerDns_IP:8081/servers/localhost/zones/novalocal


RABBITMQ_USE_LONGNAME=true
export RABBITMQ_USE_LONGNAME

$workingDir/rabbitmq_server-3.5.3/sbin/rabbitmq-server -detached >> $logFile