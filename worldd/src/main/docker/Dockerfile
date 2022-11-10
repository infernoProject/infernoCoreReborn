FROM @docker.baseImage@

ENTRYPOINT ["java", "-jar", "/opt/@project.artifactId@/@project.artifactId@.jar", "--spring.config.location=classpath:application.yml,/opt/@project.artifactId@/@project.artifactId@.yml"]
WORKDIR "/opt/@project.artifactId@"
EXPOSE 8085 8086

RUN mkdir -p /opt/@project.artifactId@
COPY classes/application.yml /opt/@project.artifactId@/@project.artifactId@.yml
COPY @project.artifactId@-@project.version@.jar /opt/@project.artifactId@/@project.artifactId@.jar

LABEL project.groupid="@project.groupId@" \
      project.artifactid="@project.artifactId@" \
      project.version="@project.version@"