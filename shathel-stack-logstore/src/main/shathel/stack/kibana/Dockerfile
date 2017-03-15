FROM docker.elastic.co/kibana/kibana:5.2.2
RUN /usr/share/kibana/bin/kibana-plugin remove x-pack
RUN /usr/local/bin/kibana-docker 2>&1 | grep -m 1 "Optimization of .* complete in .* seconds"