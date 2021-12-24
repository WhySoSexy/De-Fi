ARG FROM_TAG=8-jre-alpine
ARG FROM_IMAGE=openjdk

FROM ${FROM_IMAGE}:${FROM_TAG}

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY build/libs/pms-feeds-*.jar /usr/src/app/pms-feeds.jar

EXPOSE 8080

ENV JAVA_OPTS=''

CMD java $JAVA_OPTS -jar /usr/src/app/pms-feeds.jar