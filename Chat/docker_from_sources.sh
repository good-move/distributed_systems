#!/bin/sh

image_name="akka_chat"

docker build -t ${image_name} --file DockerFile1 .
