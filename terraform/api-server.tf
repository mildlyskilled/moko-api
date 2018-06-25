provider "aws" {
  shared_credentials_file = "/Users/kwabena/.aws/credentials"
  region     = "us-east-1"
}

resource "aws_ecs_cluster" "mokocharlie" {

  name = "moko-api"
}