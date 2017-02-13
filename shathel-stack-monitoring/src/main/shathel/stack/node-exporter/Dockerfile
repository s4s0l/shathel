FROM prom/node-exporter:v0.13.0

ENV "NODE_EXPORTER_BIN=/bin/node_exporter"

COPY ./rootfs /

RUN chmod +x /entrypoint.sh
ENTRYPOINT  [ "/entrypoint.sh" ]
CMD [ "/bin/node_exporter" ]