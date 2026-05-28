# Baseline Build

Date: 2026-05-28
Branch: yaya/platform-a
Pinned upstream commit: 74b73e4c777b80bab2cdffcec3079886f0a2e98f
Current commit before baseline: b07f9233a1ed9f2978e863e443de0e7455b87d2b

## Toolchain

```bash
java -version
mvn -version
node -v
pnpm -v || npm -v
docker --version
docker compose version
```

```text
The operation couldn’t be completed. Unable to locate a Java Runtime.
Please visit http://www.java.com for information on installing Java.

zsh:3: command not found: mvn
v25.6.1
zsh:5: command not found: pnpm
11.9.0
Docker version 29.4.0, build 9d7ad9f
Docker Compose version v5.1.2
```

## Backend Build

### Direct local Maven

```bash
mvn -DskipTests clean package
```

Result: FAILURE, exit code 127.

```text
zsh:1: command not found: mvn
```

The local macOS shell does not currently have Java or Maven installed. To avoid
changing the global developer environment during Phase 0, the backend baseline
was verified with Docker using Maven 3.9.9 and Eclipse Temurin JDK 17.

### Docker Maven/JDK 17

Command:

```bash
docker run --rm -u "$(id -u):$(id -g)" -v "$PWD":/workspace -v "$HOME/.m2":/.m2 -e MAVEN_CONFIG=/.m2 -w /workspace maven:3.9.9-eclipse-temurin-17 mvn -DskipTests clean package
```

Result: SUCCESS, exit code 0.

```text
[INFO] Reactor Summary for yudao 2026.04-SNAPSHOT:
[INFO]
[INFO] yudao-dependencies ................................. SUCCESS [ 15.671 s]
[INFO] yudao .............................................. SUCCESS [  0.066 s]
[INFO] yudao-framework .................................... SUCCESS [  0.049 s]
[INFO] yudao-common ....................................... SUCCESS [04:02 min]
[INFO] yudao-spring-boot-starter-web ...................... SUCCESS [ 25.409 s]
[INFO] yudao-spring-boot-starter-security ................. SUCCESS [ 16.881 s]
[INFO] yudao-spring-boot-starter-mybatis .................. SUCCESS [04:31 min]
[INFO] yudao-spring-boot-starter-redis .................... SUCCESS [ 13.376 s]
[INFO] yudao-spring-boot-starter-mq ....................... SUCCESS [02:07 min]
[INFO] yudao-spring-boot-starter-job ...................... SUCCESS [  0.965 s]
[INFO] yudao-spring-boot-starter-test ..................... SUCCESS [ 23.559 s]
[INFO] yudao-spring-boot-starter-biz-tenant ............... SUCCESS [  0.704 s]
[INFO] yudao-spring-boot-starter-websocket ................ SUCCESS [  1.265 s]
[INFO] yudao-spring-boot-starter-monitor .................. SUCCESS [ 25.910 s]
[INFO] yudao-spring-boot-starter-protection ............... SUCCESS [  2.744 s]
[INFO] yudao-spring-boot-starter-biz-ip ................... SUCCESS [  1.171 s]
[INFO] yudao-spring-boot-starter-excel .................... SUCCESS [ 46.293 s]
[INFO] yudao-spring-boot-starter-biz-data-permission ...... SUCCESS [  0.738 s]
[INFO] yudao-module-infra ................................. SUCCESS [01:20 min]
[INFO] yudao-module-system ................................ SUCCESS [ 49.156 s]
[INFO] yudao-module-wms ................................... SUCCESS [  2.173 s]
[INFO] yudao-server ....................................... SUCCESS [  9.901 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  17:03 min
[INFO] Finished at: 2026-05-28T04:40:19Z
[INFO] ------------------------------------------------------------------------
```

Artifact:

```text
yudao-server/target/yudao-server.jar
```

## Runtime Smoke

Runtime smoke is not complete until the database, Redis, backend URL, admin URL, and login result are recorded from real commands or browser checks. This Phase 0 baseline currently records backend build readiness only; full runtime smoke is completed in the acceptance task.
