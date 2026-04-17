#!/bin/bash
# AnyMock Core 启动脚本

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "启动 AnyMock Core..."
java -jar "${SCRIPT_DIR}/core/runner/target/anymock-core-runner-1.0-SNAPSHOT.jar" --spring.config.additional-location=classpath:anymock-core-config.yml
