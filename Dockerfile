FROM openjdk:jre-alpine

WORKDIR srv

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle
ENV mokochalie.db.host "localhost"

COPY target/scala-2.12/mokocharlie-api-assembly-1.0.jar srv/

ENTRYPOINT ["java", \
"-Dmokocharlie.db.host=mdatabase.crmzwlm1cy8l.us-east-1.rds.amazonaws.com", \
"-Dmokocharlie.db.user=mokocharlie", "-Dmokocharlie.db.password=bauladre", \
"-Dmokocharlie.db.dbName=mokocharlie", \
"-Dmokocharlie.smtp.user=postmaster@postman.mokocharlie.com", \
"-Dmokocharlie.smtp.password=547bd3f5cd42c0119b1cbae100caeda9", \
"-Dmokocharlie.smtp.host=smtp.mailgun.org", \
"-jar", "srv/mokocharlie-api-assembly-1.0.jar" \
]
