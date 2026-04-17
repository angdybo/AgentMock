#!/bin/bash
# AnyMock Manager 启动脚本
# 解决本地 fe.path 静态资源配置问题

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FE_PATH="file:${SCRIPT_DIR}/anymock-fe/dist/"

echo "启动 AnyMock Manager..."
echo "FE_PATH=${FE_PATH}"

FE_PATH="${FE_PATH}" java -jar "${SCRIPT_DIR}/manager/runner/target/anymock-manager-runner-1.0-SNAPSHOT.jar" --server.port=8329
