#!/bin/bash

workingDir=/tmp
logFile=$workingDir/deploayRabbitMQServer.log
ipAddrStarting=10
securityToken=rabbitMqDns

. /etc/environment

mkdir $workingDir

sudo apt-get -y install erlang rabbitmq-server jq

#wget --directory-prefix=$workingDir https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_5_3/rabbitmq-server-generic-unix-3.5.3.tar.gz

#if [ $? -ne 0 ]; then
#	echo "Could not download rabbitMQ tar file!" >> $logFile
#	exit
#fi

#tar -xf $workingDir/rabbitmq-server-generic-unix-3.5.3.tar.gz --directory=$workingDir

#if [ $? -ne 0 ]; then
#	echo "Could not extract rabbitMQ!" >> $logFile
#	exit
#fi

echo "PDNSIP:$rabbitServerToPowerDns_IP" >> $logFile

myIpsString=$(ip addr | grep "inet $ipAddrStarting" | grep -E -o '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
IFS=' ' read -a myIps <<< $myIpsString
myIp=${myIps[0]}

echo "hostname:$HOSTNAME.novalocal" >> $logFile

# get existing rabbitMQ servers
rabbitServersString=$(curl -H "X-API-Key: $securityToken" http://$rabbitServerToPowerDns_IP:8081/servers/localhost/zones/novalocal | jq .records | jq 'map(.name)')
rabbitServersString=${rabbitServersString//,}
rabbitServersString=${rabbitServersString//\"}
rabbitServersString=${rabbitServersString//[}
rabbitServersString=${rabbitServersString//]}
rabbitServersString=${rabbitServersString//.}
rabbitServersString=$(echo $rabbitServersString | sed 's/novalocal//g')
echo "serversString:$rabbitServersString" >> $logFile
IFS=' ' read -a rabbitServers <<< $rabbitServersString

# Add a new record to the new zone (would replace any existing test.example.org/A records)
curl -X PATCH --data "{\"rrsets\": [ {\"name\": \"$HOSTNAME.novalocal\", \"type\": \"A\", \"changetype\": \"REPLACE\", \"records\": [ {\"content\": \"$myIp\", \"disabled\": false, \"name\": \"$HOSTNAME.novalocal\", \"ttl\": 86400, \"type\": \"A\" } ] } ] }" -H "X-API-Key: $securityToken" http://$rabbitServerToPowerDns_IP:8081/servers/localhost/zones/novalocal >> $logFile

#set DNS into resolv.conf so we ask our DNS when resolving
sudo sed -i "/WILL BE OVERWRITTEN/a nameserver $rabbitServerToPowerDns_IP" /etc/resolvconf/resolv.conf.d/head
sudo rm /etc/resolv.conf
sudo ln -s /run/resolvconf/resolv.conf /etc/resolv.conf
sudo resolvconf -u

cat /etc/resolv.conf >> $logFile

sudo service rabbitmq-server stop

# create our own erlang cookie for the clustering
sudo echo -n THISISTHEDSGERLANGCOOKIEFORRABBITMQ > /var/lib/rabbitmq/.erlang.cookie

RABBITMQ_USE_LONGNAME=true
export RABBITMQ_USE_LONGNAME

sudo service rabbitmq-server start

# cluster rabbitMQServers together
serverCount=${#rabbitServers[@]}
echo "serverCount:$serverCount" >> $logFile

if [[ $serverCount -gt 0 ]]; then
  echo "Stoping server for clustering!" >> $logFile
  sudo rabbitmqctl stop_app
  loopDone=1
  count=0

  while (($loopDone != 0 && $count < $serverCount)); do
    echo "Trying to cluster to ${rabbitServers[$count]}!" >> $logFile
    sudo rabbitmqctl join_cluster rabbit@${rabbitServers[$count]} >> $logFile
    loopDone=$?
    ((count++))
  done

  sudo rabbitmqctl start_app
fi
