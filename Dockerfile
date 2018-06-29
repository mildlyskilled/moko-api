FROM phusion/baseimage

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && add-apt-repository -y ppa:webupd8team/java && apt-get update && apt-get install -y oracle-java8-installer && rm -rf /var/lib/apt/lists/* && rm -rf /var/cache/oracle-jdk8-installer

WORKDIR /srv


ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

ADD target/scala-2.12/mokocharlie-api-assembly-*.jar srv/
CMD java -jar srv/mokocharile-api-assembly-*.jar