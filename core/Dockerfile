FROM bellsoft/liberica-runtime-container:jdk-all-21-musl AS jre-build

# Create a custom Java runtime
RUN jlink \
   --add-modules ALL-MODULE-PATH \
   --strip-debug \
   --no-man-pages \
   --no-header-files \
   --compress=2 \
   --output /javaruntime

RUN strip -p --strip-unneeded javaruntime/lib/server/libjvm.so && \
   find javaruntime -name '*.so' | xargs -i strip -p --strip-unneeded {}

FROM bellsoft/alpaquita-linux-base:latest
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=${JAVA_HOME}/bin:${PATH}
COPY --from=jre-build /javaruntime $JAVA_HOME

ARG APP_VERSION
ARG APP_CREATED_DATE
ARG APP_PORT

LABEL org.opencontainers.image.created=${APP_CREATED_DATE}
LABEL org.opencontainers.image.authors=nantaaditya
LABEL org.opencontainers.image.url=https://hub.docker.com/r/nantaaditya/db-migration
LABEL org.opencontainers.image.documentation=https://github.com/nantaaditya/db-migration/blob/master/README.md
LABEL org.opencontainers.image.source=https://github.com/nantaaditya/db-migration
LABEL org.opencontainers.image.version=${APP_VERSION}
LABEL org.opencontainers.image.title=core-app

LABEL JRE_VERSION=21
LABEL JRE_DISTRIBUTION=liberica-runtime-container
LABEL JRE_BUILD_VERSION=jdk-all-21-musl
LABEL SPRING_VERSION=3.1.5

ENV TZ=Asia/Jakarta
ENV JAVA_OPTS='-server -Xms128m -Xmx512m -XX:+IgnoreUnrecognizedVMOptions -XX:+UseContainerSupport -XX:+IdleTuningCompactOnIdle -XX:+IdleTuningGcOnIdle -Xtune:virtualized -XX:+UseG1GC -XX:MaxHeapFreeRatio=40 -XX:MinHeapFreeRatio=10 -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -XX:+CompactStrings'

RUN apk --update add curl

RUN mkdir app
RUN mkdir app/resources-patch
RUN mkdir app/patch
RUN mkdir app/logs
RUN mkdir app/lib

ADD core/src/main/resources app/resources

ADD core/target/*.jar app/lib/
ADD core/target/dependency/*.jar app/lib/

WORKDIR /app

EXPOSE ${APP_PORT}

ENTRYPOINT /opt/java/openjdk/bin/java $JAVA_OPTS -cp .:resources:patch/*:lib/* com.example.app.core.CoreApplication
