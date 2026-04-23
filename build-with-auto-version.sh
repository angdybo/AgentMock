#!/bin/bash
set -e

# 项目根目录
PROJECT_DIR="/Users/Zhuanz/IdeaProjects/AnyMock"
cd "$PROJECT_DIR"

echo "开始构建 AnyMock 应用..."

# 1. 拉取最新代码
echo "1. 拉取最新代码..."
git pull

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

# 10. 清理临时文件
echo "10. 清理临时文件..."
rm -rf output

# 11. 显示结果
echo ""
echo "============================================"
echo "✓ 构建完成！"
echo "  输出文件: anymock-app-v${NEXT_VERSION}.tar"
echo "  文件大小: $(ls -lh anymock-app-v${NEXT_VERSION}.tar | awk '{print $5}')"
echo "============================================"

