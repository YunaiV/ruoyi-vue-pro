## AdoptOpenJDK 停止发布 OpenJDK 二进制，而 Eclipse Temurin 是它的延伸，提供更好的稳定性
## 感谢复旦核博士的建议！灰子哥，牛皮！
FROM eclipse-temurin:21-jre

## 创建目录，并使用它作为工作目录
RUN mkdir -p /work
WORKDIR /work
## 将后端项目的 Jar 文件，复制到镜像中
COPY ./target/*.jar app.jar

# 安装字体和时区设置
#apt-get update && apt-get install -y tzdata fonts-arphic-uming &&
#RUN cp C:\Windows\Fonts\* /usr/share/fonts && fc-cache -fv

## 设置 TZ 时区
ENV TZ=Asia/Shanghai
## 设置 JAVA_OPTS 环境变量，可通过 docker run -e "JAVA_OPTS=" 进行覆盖
ENV JAVA_OPTS="-Xms1512m -Xmx1512m -Djava.security.egd=file:/dev/./urandom"

## 应用参数
ENV ARGS="--server.port=65002"

## 暴露后端项目的 48080 端口
EXPOSE 65002

## 启动后端项目
CMD java ${JAVA_OPTS} -jar app.jar $ARGS
