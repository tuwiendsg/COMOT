workingDir=/tmp
logFile=$workingDir/deploayPowerDnsServer.log
pdnsConf=/etc/powerdns/pdns.conf

mkdir $workingDir

wget --directory-prefix=$workingDir https://downloads.powerdns.com/releases/deb/pdns-static_3.4.5-1_amd64.deb

dpkg -i /$workingDir/pdns-static_3.4.5-1_amd64.deb

myIpsString=$(ip addr | grep "inet 128" | grep -E -o '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
IFS=' ' read -a myIps <<< $myIpsString
myIp=${myIps[0]}

# db config
sed 's/# launch=/gsqlite3-database=/db/' $pdnsConf
sed -i 's/# launch=/launch=gsqlite3/' $pdnsConf
#server config
sed -i 's/# experimental-json-interface=no/experimental-json-interface=yes/' $pdnsConf
sed -i 's/# experimental-api-key=/experimental-api-key=rabbitMqDns/' $pdnsConf
sed -i 's/# webserver=no/webserver=yes/' $pdnsConf
sed -i 's/# webserver-address=127.0.0.1/webserver-address=$myIP/' $pdnsConf


#echo $myIp > http://128.130.172.215/salsa/upload/files/TMP/comot-messaging/dns/myIp