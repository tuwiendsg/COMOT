#!/bin/bash

echo "tomcat undeploy"

sudo apt-get -y erase tomcat7
sudo apt-get -y erase tomcat7-admin

cd ..
rm -rf artifactTomcat-1.0