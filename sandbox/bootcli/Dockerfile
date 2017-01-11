FROM java:openjdk-8-jre-alpine

MAINTAINER mwielgus@outlook.com

RUN apk add --no-cache su-exec \
     && apk add --update curl \
     && apk add --update bash \
     && apk add --update zip \
     && apk add --update unzip \
     && rm -rf /var/cache/apk/*
ENV SDKMAN_DIR=/sdkman
RUN curl -s "https://get.sdkman.io" | bash
RUN /bin/bash -c "source /sdkman/bin/sdkman-init.sh; sdk install springboot;"
COPY entry.sh /entry.sh
RUN chmod +x /entry.sh
ENTRYPOINT ["/entry.sh"]
HEALTHCHECK --interval=10s --timeout=3s --retries=10 \
  CMD curl -f http://localhost:8080 || exit 1
