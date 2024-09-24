ARG BUILDER_IMAGE=maven:3.9.9-eclipse-temurin-17-alpine
ARG RUNNER_IMAGE=eclipse-temurin:17-alpine
ARG CONTAINER_BUILD=yes

FROM ${BUILDER_IMAGE} as builder
ARG CONTAINER_BUILD
COPY . /usr/local/src
RUN if [ -n "$CONTAINER_BUILD" ]; \
    then apk add --no-cache git unzip && \
    cd /usr/local/src && \
    mvn -B -e -Prelease package; \
    else echo Building on host outside container to copy later; \
    fi && \
    cp target/*.zip /tmp
COPY ./target/oscal-cli-enhanced-2.1.0-SNAPSHOT-oscal-cli.zip /tmp
WORKDIR /tmp
RUN unzip *.zip -d /opt/oscal-cli-extended


FROM ${RUNNER_IMAGE} as runner
COPY --from=builder /opt/oscal-cli-extended /opt/oscal-cli-extended
WORKDIR /opt/oscal-cli-extended
ENTRYPOINT [ "/opt/oscal-cli-extended/bin/oscal-cli" ]
