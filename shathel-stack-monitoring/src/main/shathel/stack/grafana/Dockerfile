FROM grafana/grafana:4.1.1

RUN apt-get update && apt-get install -y curl

ADD rootfs /

ENV "GF_SECURITY_ADMIN_PASSWORD=admin" \
    "PROMETHEUS_ENDPOINT=http://prometheus:9090" \
    "ELASTICSEARCH_ENDPOINT=http://elasticsearch:9200" \
    "ELASTICSEARCH_USER=readuser" \
    "ELASTICSEARCH_PASSWORD=myelasticpass"
RUN chmod +x /init.sh
ENTRYPOINT ["/init.sh"]
