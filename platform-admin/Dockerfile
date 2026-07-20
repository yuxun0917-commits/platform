FROM library/eclipse-temurin:17-jre
WORKDIR /app

# 非 root 用户运行，降低容器提权风险
RUN groupadd -r app && useradd -r -g app app \
    && mkdir -p /data/platform/uploads /app/logs \
    && chown -R app:app /app /data/platform

# 拷贝本地已构建好的可执行 jar（构建上下文为 platform-admin 目录）
COPY target/*.jar /app/app.jar

USER app

# 服务端口（与 application.yml server.port 一致）
EXPOSE 8080

# 时区；默认 prod 环境（dev 含 Druid 控制台 admin/admin，禁止容器启用）；JAVA_OPTS 可 docker run -e 覆盖
ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom --add-opens java.base/java.lang=ALL-UNNAMED"

# 上传目录（prod 为 /data/platform/uploads）与日志目录挂载为卷，便于持久化
VOLUME ["/data/platform/uploads", "/app/logs"]

# 项目未引入 actuator，使用 TCP 端口探活；接入 actuator 后可改为 HTTP 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080' || exit 1

# exec 形式确保 java 为 PID 1，可正确接收 SIGTERM 触发应用层优雅停机
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
