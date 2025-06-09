## AdoptOpenJDK 停止发布 OpenJDK 二进制，而 Eclipse Temurin 是它的延伸，提供更好的稳定性
## 感谢复旦核博士的建议！灰子哥，牛皮！jammy 是 Ubuntu 22.04 LTS 的代号，长期稳定。
FROM eclipse-temurin:21-jre-jammy

## 创建目录，并使用它作为工作目录
RUN mkdir -p /work
WORKDIR /work
## 将后端项目的 Jar 文件，复制到镜像中
COPY ./target/*.jar app.jar

## 设置 TZ 时区
ENV TZ=Asia/Shanghai

## 安装 Noto 字体（适用于 Debian/Ubuntu 系列镜像）、覆盖全球几乎所有语言的 Unicode 字符（包括中文、日文、韩文、emoji）、开源、跨平台
RUN apt-get update && apt-get install -y   fonts-noto-cjk  fonts-noto-color-emoji  && rm -rf /var/lib/apt/lists/*  && fc-cache -fv

## 设置 JAVA_OPTS 环境变量，可通过 docker run -e "JAVA_OPTS=" 进行覆盖
ENV JAVA_OPTS="-Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

## 应用参数
ENV ARGS=""

## 暴露后端项目的 48080 端口
EXPOSE 48080

## 启动后端项目
CMD java ${JAVA_OPTS} -jar app.jar $ARGS
