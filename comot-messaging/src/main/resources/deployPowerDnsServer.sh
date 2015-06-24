#!/bin/bash

workingDir=/tmp
logFile=$workingDir/deploayPowerDnsServer.log
pdnsConf=/etc/powerdns/pdns.conf
ipAddrStarting=10
dbPath=/etc/powerdns/powerdns.sqlite
comotPlatformPath=http://128.130.172.215/repository/files/comot-messaging
securityToken=rabbitMqDns

mkdir $workingDir

wget --directory-prefix=$workingDir https://downloads.powerdns.com/releases/deb/pdns-static_3.4.5-1_amd64.deb
wget --directory-prefix=$workingDir $comotPlatformPath/powerdns.sqlite
cp $workingDir/powerdns.sqlite $dbPath

dpkg -i /$workingDir/pdns-static_3.4.5-1_amd64.deb

myIpsString=$(ip addr | grep "inet $ipAddrStarting" | grep -E -o '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
IFS=' ' read -a myIps <<< $myIpsString
myIp=${myIps[0]}

# db config
sed -i 's/# launch=/launch=gsqlite3/' $pdnsConf
sed -i "/launch=gsqlite3/a gsqlite3-database=$dbPath" $pdnsConf
#server config
sed -i 's/# experimental-json-interface=no/experimental-json-interface=yes/' $pdnsConf
sed -i "s/# experimental-api-key=/experimental-api-key=$securityToken/" $pdnsConf
sed -i 's/# webserver=no/webserver=yes/' $pdnsConf
sed -i "s/# webserver-address=127.0.0.1/webserver-address=$myIp/" $pdnsConf

PowerDnsIp=$myIp
export PowerDnsIp

/etc/init.d/pdns start

# Create new zone "example.org" with nameservers ns1.example.org, ns2.example.org
curl -X POST --data '{"name":"novalocal", "kind": "Native", "masters": [], "nameservers": []}' -v -H "X-API-Key: $rabbitMqDns" http://$myIp:8081/servers/localhost/zones