FROM sasol/dind4j:2.3
ADD shathel-deployer /usr/bin/shathel-deployer
RUN chmod +x /usr/bin/shathel-deployer
VOLUME /solution
#VOLUME /stacks
#VOLUME /envs

ENV SHATHEL_DIR=/solution
ENV SHATHEL_SOLUTION_FILE_BASE_DIR=/stacks,/stacks0,/stacks1,/stacks2,/stacks3
ENV SHATHEL_SOLUTION_FILE_ENV_BASE_DIR=/envs,/envs0,/envs1
WORKDIR /solution
ENV SKIPDAEMON=true
ENTRYPOINT ["/start.sh", "/usr/bin/shathel-deployer"]
CMD [""]
