#!/bin/bash

echo "war deploy"

wget http://128.130.172.215/salsa/upload/files/juraj/elasticWS/artifactWar-1.0.tar.gz
tar -xzf ./artifactWar-1.0.tar.gz
cd ./artifactWar-1.0

DIR=$(pwd)

curl "http://comot:comot@localhost:8080/manager/text/deploy?war=file:"DIR"S/testWS-0.1.war&path=/testWS"
