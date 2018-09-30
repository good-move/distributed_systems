#!/bin/sh

IMAGE_NAME="akka_cluster"
echo "Creating docker image $IMAGE_NAME..."
docker build -t $IMAGE_NAME .
echo "Created image $IMAGE_NAME"
echo "run 'docker run -t $IMAGE_NAME <node_name> <port>' to launch cluster node"
