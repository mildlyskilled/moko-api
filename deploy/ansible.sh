#!/usr/bin/env bash

ENV=$1
COMMIT_ID=$2
APPLICATION=$3
SERVICECOUNT=${4:-1}

ansible-playbook aws/launch-application.yml \
    --private-key ~/.ssh/thefaculty.pem  \
    --extra-vars "env=$ENV commit_id=$COMMIT_ID application_name=$APPLICATION service_count=$SERVICECOUNT"