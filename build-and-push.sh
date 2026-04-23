#!/bin/bash
set -e

# 项目根目录
PROJECT_DIR="/Users/Zhuanz/IdeaProjects/AnyMock"
cd "$PROJECT_DIR"

# Harbor 配置
HARBOR_URL="219.139.241.229:8844"
HARBOR_USERNAME="18963948278"
HARBOR_PASSWORD="Zhengwei0101"
IMAGE_NAME="spd/anymock-app"

echo "============================================"
echo "开始构建 AnyMock 应用..."
echo "============================================"

# 1. 拉取最新代码
echo "1. 拉取最新代码..."
# git pull

# 2. Maven 构建
echo "2. Maven 构建..."
mvn clean package -DskipTests

# 3. 构建前端（如果需要）
echo "3. 构建前端..."
cd anymock-fe-new
npm install
npm run build
cd "$PROJECT_DIR"

# 4. 准备输出目录
echo "4. 准备输出目录..."
rm -rf output
mkdir -p output/manager
mkdir -p output/core
mkdir -p output/fe

# 5. 复制 JAR 包
echo "5. 复制 JAR 包..."
cp manager/runner/target/anymock-manager-runner-1.0-SNAPSHOT.jar output/manager/
cp core/runner/target/anymock-core-runner-1.0-SNAPSHOT.jar output/core/

# 6. 复制前端构建产物
echo "6. 复制前端构建产物..."
cp -r anymock-fe-new/dist/* output/fe/

# 7. 复制启动脚本
echo "7. 复制启动脚本..."
cp package-dist/start.sh output/
cp package-dist/stop.sh output/
cp package-dist/docker-entrypoint.sh output/

# 8. 计算下一个版本号
echo "8. 计算下一个版本号..."
# 查找最新的 vN.tar 文件（N 是纯数字）
LATEST_VERSION=$(ls -1t anymock-app-v*.tar 2>/dev/null | grep -E "anymock-app-v[0-9]+\.tar$" | head -1 | sed 's/.*anymock-app-v//' | sed 's/\.tar$//')

if [ -z "$LATEST_VERSION" ]; then
    echo "未找到现有的 vN.tar 文件，使用 v1"
    NEXT_VERSION=1
else
    NEXT_VERSION=$((LATEST_VERSION + 1))
    echo "最新版本: v${LATEST_VERSION}, 下一个版本: v${NEXT_VERSION}"
fi

# 9. 打包
echo "9. 打包成 anymock-app-v${NEXT_VERSION}.tar..."
cd output
tar -czf "../anymock-app-v${NEXT_VERSION}.tar" .
cd "$PROJECT_DIR"

echo ""
echo "============================================"
echo "✓ 应用打包完成！"
echo "  输出文件: anymock-app-v${NEXT_VERSION}.tar"
echo "  文件大小: $(ls -lh anymock-app-v${NEXT_VERSION}.tar | awk '{print $5}')"
echo "============================================"

# 10. 构建 Docker 镜像
echo ""
echo "10. 构建 Docker 镜像..."
cd output

# 创建 Dockerfile
cat > Dockerfile << 'EOF'
# AnyMock 后端镜像（仅包含 Manager 8329 + Core 8330）
# MySQL 和 Redis 使用线上地址，不纳入容器
FROM eclipse-temurin:8-jre-jammy
WORKDIR /app

# 拷贝两个 JAR 包
COPY manager/*.jar app-manager.jar
COPY core/*.jar app-core.jar

# 拷贝前端静态资源
COPY fe/ /app/dist/

# 拷贝启动脚本
COPY start.sh /app/start.sh
COPY stop.sh /app/stop.sh
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/start.sh /app/stop.sh /app/docker-entrypoint.sh

# 暴露端口
EXPOSE 8329 8330

# 同时启动 Core(8330) + Manager(8329)
ENTRYPOINT ["/app/docker-entrypoint.sh"]
EOF

# 构建镜像（强制使用 amd64 平台以兼容 K8s）
docker build --platform linux/amd64 -t ${IMAGE_NAME}:v${NEXT_VERSION} .

cd "$PROJECT_DIR"

echo ""
echo "============================================"
echo "✓ Docker 镜像构建完成！"
echo "  镜像名称: ${IMAGE_NAME}:v${NEXT_VERSION}"
echo "============================================"

# 11. 登录 Harbor
echo ""
echo "11. 登录 Harbor..."
echo ${HARBOR_PASSWORD} | docker login ${HARBOR_URL} --username ${HARBOR_USERNAME} --password-stdin

# 12. 打 tag 并推送
echo ""
echo "12. 打 tag 并推送到 Harbor..."

# 打 tag
docker tag ${IMAGE_NAME}:v${NEXT_VERSION} ${HARBOR_URL}/${IMAGE_NAME}:v${NEXT_VERSION}

# 推送镜像
docker push ${HARBOR_URL}/${IMAGE_NAME}:v${NEXT_VERSION}

echo ""
echo "============================================"
echo "✓ 推送完成！"
echo "  Harbor 地址: ${HARBOR_URL}/${IMAGE_NAME}:v${NEXT_VERSION}"
echo "  使用命令: docker pull ${HARBOR_URL}/${IMAGE_NAME}:v${NEXT_VERSION}"
echo "============================================"

# 13. 清理临时文件
echo ""
echo "13. 清理临时文件..."
rm -rf output

echo ""
echo "============================================"
echo "✓ 所有操作完成！"
echo "  输出文件: anymock-app-v${NEXT_VERSION}.tar"
echo "  Docker 镜像: ${IMAGE_NAME}:v${NEXT_VERSION}"
echo "  Harbor 镜像: ${HARBOR_URL}/${IMAGE_NAME}:v${NEXT_VERSION}"
echo "============================================"
