#!/usr/bin/python

import os
if 'ANSIBLE_VAULT_PASSWORD' not in os.environ:
    raise ValueError("Ansible vault password not found please set it")

print(os.environ['ANSIBLE_VAULT_PASSWORD'])
