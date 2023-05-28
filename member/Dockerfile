FROM azul/zulu-openjdk-alpine:17-jre

ENV TZ=Asia/Jakarta


RUN mkdir app
RUN mkdir app/resources-patch
RUN mkdir app/patch
RUN mkdir app/logs
RUN mkdir app/lib

ADD member/src/main/resources app/resources

ADD member/target/*.jar app/lib/
ADD member/target/dependency/*.jar app/lib/

WORKDIR /app

ENTRYPOINT [ "java","-cp",".:resources:patch/*:lib/*","com.example.app.member.MemberApplication" ]

