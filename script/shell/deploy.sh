#!/bin/bash
set -e

DATE=$(date +%Y%m%d%H%M)
# 基础路径
BASE_PATH=/work/projects/yudao-server
# 编译后 jar 的地址。部署时，Jenkins 会上传 jar 包到该目录下
SOURCE_PATH=$BASE_PATH/build
# 服务名称。同时约定部署服务的 jar 包名字也为它。
SERVER_NAME=yudao-server
# 环境
PROFILES_ACTIVE=development
# 健康检查 URL
HEALTH_CHECK_URL=http://127.0.0.1:48080/actuator/health/

# heapError 存放路径
HEAP_ERROR_PATH=$BASE_PATH/heapError
# JVM 参数
JAVA_OPS="-Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$HEAP_ERROR_PATH"

# SkyWalking Agent 配置
#export SW_AGENT_NAME=$SERVER_NAME
#export SW_AGENT_COLLECTOR_BACKEND_SERVICES=192.168.0.84:11800
#export SW_GRPC_LOG_SERVER_HOST=192.168.0.84
#export SW_AGENT_TRACE_IGNORE_PATH="Redisson/PING,/actuator/**,/admin/**"
#export JAVA_AGENT=-javaagent:/work/skywalking/apache-skywalking-apm-bin/agent/skywalking-agent.jar

# 备份
function backup() {
    # 如果不存在，则无需备份
    if [ ! -f "$BASE_PATH/$SERVER_NAME.jar" ]; then
        echo "[backup] $BASE_PATH/$SERVER_NAME.jar 不存在，跳过备份"
    # 如果存在，则备份到 backup 目录下，使用时间作为后缀
    else
        echo "[backup] 开始备份 $SERVER_NAME ..."
        cp $BASE_PATH/$SERVER_NAME.jar $BASE_PATH/backup/$SERVER_NAME-$DATE.jar
        echo "[backup] 备份 $SERVER_NAME 完成"
    fi
}

# 最新构建代码 移动到项目环境
function transfer() {
    echo "[transfer] 开始转移 $SERVER_NAME.jar"

    # 删除原 jar 包
    if [ ! -f "$BASE_PATH/$SERVER_NAME.jar" ]; then
        echo "[transfer] $BASE_PATH/$SERVER_NAME.jar 不存在，跳过删除"
    else
        echo "[transfer] 移除 $BASE_PATH/$SERVER_NAME.jar 完成"
        rm $BASE_PATH/$SERVER_NAME.jar
    fi

    # 复制新 jar 包
    echo "[transfer] 从 $SOURCE_PATH 中获取 $SERVER_NAME.jar 并迁移至 $BASE_PATH ...."
    cp $SOURCE_PATH/$SERVER_NAME.jar $BASE_PATH

    echo "[transfer] 转移 $SERVER_NAME.jar 完成"
}

# 停止：优雅关闭之前已经启动的服务
function stop() {
    echo "[stop] 开始停止 $BASE_PATH/$SERVER_NAME"
    PID=$(ps -ef | grep $BASE_PATH/$SERVER_NAME | grep -v "grep" | awk '{print $2}')
    # 如果 Java 服务启动中，则进行关闭
    if [ -n "$PID" ]; then
        # 正常关闭
        echo "[stop] $BASE_PATH/$SERVER_NAME 运行中，开始 kill [$PID]"
        kill -15 $PID
        # 等待最大 120 秒，直到关闭完成。
        for ((i = 0; i < 120; i++))
            do
                sleep 1
                PID=$(ps -ef | grep $BASE_PATH/$SERVER_NAME | grep -v "grep" | awk '{print $2}')
                if [ -n "$PID" ]; then
                    echo -e ".\c"
                else
                    echo "[stop] 停止 $BASE_PATH/$SERVER_NAME 成功"
                    break
                fi
		    done

        # 如果正常关闭失败，那么进行强制 kill -9 进行关闭
        if [ -n "$PID" ]; then
            echo "[stop] $BASE_PATH/$SERVER_NAME 失败，强制 kill -9 $PID"
            kill -9 $PID
        fi
    # 如果 Java 服务未启动，则无需关闭
    else
        echo "[stop] $BASE_PATH/$SERVER_NAME 未启动，无需停止"
    fi
}

# 启动：启动后端项目
function start() {
    # 开启启动前，打印启动参数
    echo "[start] 开始启动 $BASE_PATH/$SERVER_NAME"
    echo "[start] JAVA_OPS: $JAVA_OPS"
    echo "[start] JAVA_AGENT: $JAVA_AGENT"
    echo "[start] PROFILES: $PROFILES_ACTIVE"

    # 开始启动
    BUILD_ID=dontKillMe nohup java -server $JAVA_OPS $JAVA_AGENT -jar $BASE_PATH/$SERVER_NAME.jar --spring.profiles.active=$PROFILES_ACTIVE &
    echo "[start] 启动 $BASE_PATH/$SERVER_NAME 完成"
}

# 健康检查：自动判断后端项目是否正常启动
function healthCheck() {
    # 如果配置健康检查，则进行健康检查
    if [ -n "$HEALTH_CHECK_URL" ]; then
        # 健康检查最大 120 秒，直到健康检查通过
        echo "[healthCheck] 开始通过 $HEALTH_CHECK_URL 地址，进行健康检查";
        for ((i = 0; i < 120; i++))
            do
                # 请求健康检查地址，只获取状态码。
                result=`curl -I -m 10 -o /dev/null -s -w %{http_code} $HEALTH_CHECK_URL || echo "000"`
                # 如果状态码为 200，则说明健康检查通过
                if [ "$result" == "200" ]; then
                    echo "[healthCheck] 健康检查通过";
                    break
                # 如果状态码非 200，则说明未通过。sleep 1 秒后，继续重试
                else
                    echo -e ".\c"
                    sleep 1
                fi
            done

        # 健康检查未通过，则异常退出 shell 脚本，不继续部署。
        if [ ! "$result" == "200" ]; then
            echo "[healthCheck] 健康检查不通过，可能部署失败。查看日志，自行判断是否启动成功";
            tail -n 10 nohup.out
            exit 1;
        # 健康检查通过，打印最后 10 行日志，可能部署的人想看下日志。
        else
            tail -n 10 nohup.out
        fi
    # 如果未配置健康检查，则 sleep 120 秒，人工看日志是否部署成功。
    else
        echo "[healthCheck] HEALTH_CHECK_URL 未配置，开始 sleep 120 秒";
        sleep 120
        echo "[healthCheck] sleep 120 秒完成，查看日志，自行判断是否启动成功";
        tail -n 50 nohup.out
    fi
}

# 部署
function deploy() {
    cd $BASE_PATH
    # 备份原 jar
    backup
    # 停止 Java 服务
    stop
    # 部署新 jar
    transfer
    # 启动 Java 服务
    start
    # 健康检查
    healthCheck
}

deploy
