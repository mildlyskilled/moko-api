provider "aws" {
  shared_credentials_file = "/Users/kwabena/.aws/credentials"
  region     = "us-east-1"
}

resource "aws_instance" "moko-api" {
  ami           = "ami-2757f631"
  instance_type = "t2.micro"
}

resource "aws_db_instance" "mokocharlie" {

}