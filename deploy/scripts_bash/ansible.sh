#!/usr/bin/env bash

ansible-playbook deploy/aws/launch-application.yml \
    --private-key ~/.ssh/thefaculty.pem  \
    --extra-vars "env=$1 commit_id=$2 application_name=$3"