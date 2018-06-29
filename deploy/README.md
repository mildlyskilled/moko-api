** Deploying This Project

*To deploy this appliaction you will need the faculty key pair*

Simply run the ansible command as follows

    ansible-playbook aws/launch-application.yml 
    --private-key <the faculty issued key pair> 
    --extra-vars "env=prod commit_id=<commit successfully built by codeship> 
      application_name=<application name>"
      service_count=<number of instances [default 1]>


Alternative you can run the following 
`./ansible.sh <env> <commitid> <appname> <servicecount>`