# Mokocharlie API

API for mokocharlie services (Scala)

## Known Issues

- Ordering
- Handle errors better... at the moment everything is an internal server error
- Dockerise this
- CORS support
- Validation with [Accord](https://github.com/wix/accord) maybe? or [Checklist](https://github.com/davegurnell/checklist/tree/0.4.0)
- Documentation with [Swagger](https://github.com/swagger-akka-http/swagger-akka-http)


## To build

sbt docker:publishLocal