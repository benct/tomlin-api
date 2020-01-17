#!/usr/bin/env bash

echo "Stopping application..."
kill -9 $(ps aux | grep tomlin-api | awk '{print $2}')

echo "Clearing logs..."
sudo rm tomlin-api.log

echo "Starting database..."
sudo systemctl start mysql

echo "Starting application..."
nohup java -jar build/libs/tomlin-api-1.0.0.jar --spring.profiles.active=prod > tomlin-api.log &

ps aux | grep tomlin-api

echo "Reading logs..."
tail -f tomlin-api.log
