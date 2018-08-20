#!/usr/bin/env bash

ENV=$1
COMMIT_ID=$2
APPLICATION=$3
SERVICECOUNT=${4:-1}

ansible-playbook --vault-password-file "scripts_python/vault_pass.py" \
    "aws/launch-container-application.yml" \
    --private-key "aws/shared/deploy.pem"  \
    --extra-vars "env=$ENV commit_id=$COMMIT_ID application_name=$APPLICATION service_count=$SERVICECOUNT" 
    