FROM docker.1ms.run/library/eclipse-temurin:17-jre

WORKDIR /app

# 拷贝本地已构建好的可执行 jar（构建上下文为 platform-admin 目录）
COPY target/*.jar /app/app.jar

# 服务端口（与 application.yml server.port 一致）
EXPOSE 8085

# 时区；默认 prod 环境（dev 含 Druid 控制台 admin/admin，禁止容器启用）；JAVA_OPTS 可 docker run -e 覆盖
ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom --add-opens java.base/java.lang=ALL-UNNAMED"

# exec 形式确保 java 为 PID 1，可正确接收 SIGTERM 触发应用层优雅停机
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
