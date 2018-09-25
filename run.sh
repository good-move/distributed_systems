#!/bin/sh

name=$1
port=$2

echo "Compiling project..."
sbt compile

echo "Starting akka node..."
sbt "run $name $port"
