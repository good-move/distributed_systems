#!/bin/sh

name=$1
port=$2
path_to_jar=$(printenv JAR_NAME)

if [ -f ${path_to_jar} ]
then
    echo "Starting akka node..."
    java -jar ${path_to_jar} ${name} ${port}
else
    echo "JAR file not found. Build project and rebuild docker image"
fi
