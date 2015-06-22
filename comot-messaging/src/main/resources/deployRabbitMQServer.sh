#!/bin/bash

workingDir=/tmp
logFile=$workingDir/deploayRabbitMQServer.log

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

echo "PDNSIP:$PowerDnsIpReq" >> $logFile

$workingDir/rabbitmq_server-3.5.3/sbin/rabbitmq-server -detached >> $logFile