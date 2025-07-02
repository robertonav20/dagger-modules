#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

parent_folder=$(pwd)

# Add Dagger Module to Gitea
curl -X 'DELETE' \
    'http://localhost:3000/api/v1/repos/admin/java-dagger-module' \
    -H 'accept: application/json' \
    -H 'authorization: Basic YWRtaW46YWRtaW4xMjM='
curl -X 'POST' \
    'http://localhost:3000/api/v1/user/repos' \
    -H 'accept: application/json' \
    -H 'authorization: Basic YWRtaW46YWRtaW4xMjM=' \
    -H 'Content-Type: application/json' \
    -d '{
    "name": "java-dagger-module"
}'
rm -rf ../java-dagger-module
mkdir -p ../java-dagger-module
cp -r modules/java/java-build/* ../java-dagger-module/
cd ../java-dagger-module
git init
git checkout -b main
git remote add origin http://admin:admin123@localhost:3000/admin/java-dagger-module.git
git add .
git commit -m "initial commit"
git push --force origin main

cd $parent_folder

# Add Hello World to Gitea
curl -X 'DELETE' \
    'http://localhost:3000/api/v1/repos/admin/hello-world' \
    -H 'accept: application/json' \
    -H 'authorization: Basic YWRtaW46YWRtaW4xMjM='
curl -X 'POST' \
    'http://localhost:3000/api/v1/user/repos' \
    -H 'accept: application/json' \
    -H 'authorization: Basic YWRtaW46YWRtaW4xMjM=' \
    -H 'Content-Type: application/json' \
    -d '{
    "name": "hello-world"
}'
rm -rf ../hello-world
mkdir -p ../hello-world
cp -r hello-world ../
cd ../hello-world
git init
git checkout -b main
git remote add origin http://admin:admin123@localhost:3000/admin/hello-world.git
git add .
git commit -m "initial commit"
git push --force origin main