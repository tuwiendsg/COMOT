#!/bin/bash

log=deploymentLog

workingDir=/tmp
pdnsConf=/etc/powerdns/pdns.conf
ipAddrStarting=10
dbPath=/etc/powerdns/powerdns.sqlite
comotPlatformPath=http://128.130.172.215/iCOMOTTutorial/files/comot-messaging
securityToken=rabbitMqDns

mkdir $workingDir

wget --directory-prefix=$workingDir https://downloads.powerdns.com/releases/deb/pdns-static_3.4.5-1_amd64.deb
wget --directory-prefix=$workingDir $comotPlatformPath/powerdns.sqlite

sudo dpkg -i /$workingDir/pdns-static_3.4.5-1_amd64.deb
sudo cp $workingDir/powerdns.sqlite $dbPath

myIpsString=$(ip addr | grep "inet $ipAddrStarting" | grep -E -o '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
IFS=' ' read -a myIps <<< $myIpsString
myIp=${myIps[0]}

# db config
sudo sed -i 's/# launch=/launch=gsqlite3/' $pdnsConf
sudo sed -i "/launch=gsqlite3/a gsqlite3-database=$dbPath" $pdnsConf
#server config
sudo sed -i 's/# experimental-json-interface=no/experimental-json-interface=yes/' $pdnsConf
sudo sed -i "s/# experimental-api-key=/experimental-api-key=$securityToken/" $pdnsConf
sudo sed -i 's/# webserver=no/webserver=yes/' $pdnsConf
sudo sed -i "s/# webserver-address=127.0.0.1/webserver-address=$myIp/" $pdnsConf

PowerDnsIp=$myIp
export PowerDnsIp

sleep 5

sudo service pdns start

sleep 5

# Create new zone "example.org" with nameservers ns1.example.org, ns2.example.org
curl -X POST --data '{"name":"novalocal", "kind": "Native", "masters": [], "nameservers": []}' -v -H "X-API-Key: $securityToken" http://$myIp:8081/servers/localhost/zones > $workingDir/$log
