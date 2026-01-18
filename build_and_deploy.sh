#!/bin/bash
HOST=registry.apokalyptus.duckdns.org
TAG=$HOST/hanzispider
rm log.txt
rm id.txt
docker build --iidfile id.txt --progress=plain . 2>&1 | tee log.txt
ID=$(cat id.txt)
echo " ID:" $ID
echo "TAG:" $TAG
#docker login -u joern $HOST
docker tag $ID $TAG
docker push $TAG
