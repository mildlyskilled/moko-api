# Mokocharlie API

API for mokocharlie services (Scala)

[ ![Codeship Status for kaning/moko-api](https://app.codeship.com/projects/be6934c0-e0ba-0135-79ea-7a75524f0ffb/status?branch=master)](https://app.codeship.com/projects/266540)

## Known Issues

- Repositories
    - ~~Photos~~
    - ~~Album~~
    - Collection
    - Comment
    - Documentary
    - Favourite
    - User
    - Video 
- Ordering
- Handle errors better... at the moment everything is an internal server error
- ~~Dockerise this~~
- CORS support
- Validation with [Accord](https://github.com/wix/accord) maybe? or [Checklist](https://github.com/davegurnell/checklist/tree/0.4.0)
- Documentation with [Swagger](https://github.com/swagger-akka-http/swagger-akka-http)


## To build

sbt docker:publishLocal