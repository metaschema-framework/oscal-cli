ARG BUILDER_IMAGE=maven:3.9.9-eclipse-temurin-17-alpine
ARG RUNNER_IMAGE=eclipse-temurin:17-alpine
# Not set by default, so it will build in container locally. See the GitHub
# Actions build.yml where build-arg is provider to override and build inside
# the container.
ARG USE_PREBUILT_ZIP

FROM ${BUILDER_IMAGE} AS builder
ARG USE_PREBUILT_ZIP
ARG BUILDER_JDK_VENDOR=temurin
ARG BUILDER_JDK_MAJOR_VERSION=17
ARG BUILDER_JDK_HOME_PATH=/opt/java/openjdk
ADD . /usr/local/src
# You can't copy conditionally for a folder that doesn't exist.
# Make the build boostrap files regardless.
RUN mkdir -p "/root/.m2"
COPY  <<M2TEMPLATE /root/.m2/toolchains.xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
    <toolchain>
    <type>jdk</type>
    <provides>
        <version>${BUILDER_JDK_MAJOR_VERSION}</version>
        <vendor>${BUILDER_JDK_VENDOR}</vendor>
        <id>${BUILDER_JDK_VENDOR}_${BUILDER_JDK_MAJOR_VERSION}</id>
    </provides>
    <configuration>
        <jdkHome>${BUILDER_JDK_HOME_PATH}</jdkHome>
    </configuration>
    </toolchain>
</toolchains>
M2TEMPLATE
RUN if [[ -z "$USE_PREBUILT_ZIP" ]]; then \
        apk add --no-cache git unzip && \
        cd /usr/local/src && \
        mvn -B -e -Prelease package && \
        cp ./target/*.zip /tmp; \
    else \
        echo "Using prebuilt ZIP archive from outside container"; \
    fi

# Conditional source to prevent failed check for pre-built zip when the variable
# USE_PREBUILT_ZIP is set. See the following:
# https://stackoverflow.com/a/43656644   
# https://stackoverflow.com/a/46801962
COPY pom.xml ./target/*.zi[p] /tmp
WORKDIR /tmp
RUN unzip *.zip -d /opt/oscal-cli-extended

FROM ${RUNNER_IMAGE} AS runner
COPY --from=builder /opt/oscal-cli-extended /opt/oscal-cli-extended
WORKDIR /opt/oscal-cli-extended
RUN /opt/oscal-cli-extended/bin/oscal-cli --version
ENTRYPOINT [ "/opt/oscal-cli-extended/bin/oscal-cli" ]
