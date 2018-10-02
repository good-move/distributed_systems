#!/bin/sh

user_name="goodmove"
image_name="akka_chat"

sbt assembly
docker build -t ${image_name} .
