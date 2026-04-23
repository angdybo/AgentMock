#!/bin/bash
# 同时启动 Manager (8329) 和 Core (8330)
echo "=== Starting AnyMock Core (port 8330) ==="

# 先等几秒让网络就绪
sleep 3

nohup java -jar /app/app-core.jar > /tmp/anymock-core.log 2>&1 &
CORE_PID=$!
echo "Core started, PID: $CORE_PID"
echo "Waiting for Core to initialize..."
sleep 10

# 检查 Core 是否还活着
if kill -0 $CORE_PID 2>/dev/null; then
    echo "Core is running (PID: $CORE_PID)"
    head -20 /tmp/anymock-core.log
else
    echo "ERROR: Core failed to start!"
    cat /tmp/anymock-core.log
fi

echo ""
echo "=== Starting AnyMock Manager (port 8329) ==="
exec java -jar /app/app-manager.jar
