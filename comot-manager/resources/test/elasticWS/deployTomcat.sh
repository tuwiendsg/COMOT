#!/bin/bash

echo "tomcat deploy"

wget http://128.130.172.215/salsa/upload/files/juraj/elasticWS/artifactTomcat-1.0.tar.gz
tar -xzf ./artifactTomcat-1.0.tar.gz
cd ./artifactTomcat-1.0


sudo apt-get update
# install and start tomcat
sudo apt-get install tomcat7 -y
# install manager
sudo apt-get install tomcat7-admin -y
# set user access rights
sudo cp ./tomcat-users.xml /etc/tomcat7/tomcat-users.xml