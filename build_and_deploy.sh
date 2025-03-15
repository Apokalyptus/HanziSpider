#!/bin/bash
HOST=registry.apokalyptus.duckdns.org
TAG=$HOST/hanzespider
rm log.txt
docker build --progress=plain . 2>&1 | tee log.txt
ID=`grep 'writing image' log.txt | awk '{print $4}'`
echo " ID:" $ID
echo "TAG:" $TAG
docker login -u joern $HOST
docker tag $ID $TAG
docker push $TAG

