# AnyMock 后端镜像（仅包含 Manager 8329 + Core 8330）
# MySQL 和 Redis 使用线上地址，不纳入容器
FROM eclipse-temurin:8-jre-jammy
WORKDIR /app

# 拷贝两个 JAR 包
COPY core/runner/target/*.jar app-core.jar
COPY manager/runner/target/*.jar app-manager.jar

# 拷贝前端静态资源
COPY anymock-fe/dist/ /app/dist/

# 拷贝启动脚本S
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

# 暴露端口
EXPOSE 8329 8330
 同时启动 Core(8330) + Manager(8329)
ENTRYPOINT ["/app/docker-entrypoint.sh"]
#
