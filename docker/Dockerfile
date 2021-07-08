ARG BASE_IMAGE

FROM ${BASE_IMAGE}

COPY --chown=900:0 target/*.zip /packages/

USER root
RUN /install-packages.sh --offline /packages/*.zip
RUN chown -R 900:0 ${NUXEO_HOME} \
  && chmod -R g+rwX ${NUXEO_HOME}
USER 900
