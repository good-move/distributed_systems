#!/bin/sh

user_name="goodmove"
image_name="akka_chat"

sbt assembly
docker build -t ${image_name} .
docker tag ${image_name} ${user_name}/${image_name}
docker push ${user_name}/${image_name}
