FROM alpine:3.5
MAINTAINER Sasol

RUN apk -U add openssl
RUN apk -U add curl

ENV A=Ax

ADD ./docker-gen /usr/local/bin/docker-gen


RUN chmod +x /usr/local/bin/docker-gen
ENTRYPOINT ["/usr/local/bin/docker-gen"]
