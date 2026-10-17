param(
  [string]$OutputDir = "single-module-output"
)

$ErrorActionPreference = "Stop"

function Write-Info($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Warn($msg) { Write-Host "[WARN] $msg" -ForegroundColor Yellow }
function Write-Err($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

# Resolve repo root (script located in script\tools)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")
Write-Info "Repo root: $RepoRoot"

# Prepare output dir under repo
$OutDir = Join-Path $RepoRoot $OutputDir
if (Test-Path $OutDir) {
  Write-Warn "Output directory already exists: $OutDir. It will be overwritten."
  Remove-Item -Recurse -Force $OutDir
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

# Read root pom.xml to extract versions
$RootPomPath = Join-Path $RepoRoot "pom.xml"
if (-not (Test-Path $RootPomPath)) { Write-Err "Cannot find root pom.xml at $RootPomPath"; exit 1 }
[xml]$RootPom = Get-Content -Raw -Encoding UTF8 $RootPomPath
$ns = New-Object System.Xml.XmlNamespaceManager($RootPom.NameTable)
$ns.AddNamespace("m", "http://maven.apache.org/POM/4.0.0")

$Revision = ($RootPom.project.properties.revision)
if (-not $Revision) { $Revision = "1.0.0-SNAPSHOT" }
$SpringBootVersion = ($RootPom.project.properties.'spring.boot.version')
if (-not $SpringBootVersion) { $SpringBootVersion = "3.2.5" }
$GroupId = ($RootPom.project.groupId)
if (-not $GroupId) { $GroupId = "cn.iocoder.boot" }

Write-Info "Detected revision=$Revision spring.boot.version=$SpringBootVersion groupId=$GroupId"

# Generate single-module pom.xml
$PomContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>$SpringBootVersion</version>
    <relativePath/>
  </parent>
  <groupId>$GroupId</groupId>
  <artifactId>yudao-single</artifactId>
  <version>$Revision</version>
  <name>yudao-single</name>
  <description>单模块打包的芋道项目，由脚本生成；建议手工精简依赖。</description>

  <properties>
    <java.version>17</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencyManagement>
    <dependencies>
      <!-- 继续复用仓库中的 yudao-dependencies 作为 BOM，确保依赖版本一致 -->
      <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-dependencies</artifactId>
        <version>$Revision</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- 基础 Spring Boot -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>

    <!-- 数据访问 & 中间件（根据需要后续手动精简） -->
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid-spring-boot-3-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>org.redisson</groupId>
      <artifactId>redisson-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.rocketmq</groupId>
      <artifactId>rocketmq-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>org.quartz-scheduler</groupId>
      <artifactId>quartz</artifactId>
    </dependency>

    <!-- 常用三方 -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>org.mapstruct</groupId>
      <artifactId>mapstruct</artifactId>
    </dependency>

    <!-- 测试 -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-ekycfacecheck</artifactId>
      <scope>ekycfacecheck</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <annotationProcessorPaths>
            <path>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-configuration-processor</artifactId>
              <version>$SpringBootVersion</version>
            </path>
            <path>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
            </path>
            <path>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok-mapstruct-binding</artifactId>
              <version>0.2.0</version>
            </path>
            <path>
              <groupId>org.mapstruct</groupId>
              <artifactId>mapstruct-processor</artifactId>
            </path>
          </annotationProcessorPaths>
          <compilerArgs>
            <arg>-parameters</arg>
          </compilerArgs>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <mainClass>cn.iocoder.yudao.YudaoApplication</mainClass>
          <layers>
            <enabled>true</enabled>
          </layers>
        </configuration>
      </plugin>
    </plugins>
  </build>

  <repositories>
    <repository>
      <id>huaweicloud</id>
      <name>huawei</name>
      <url>https://mirrors.huaweicloud.com/repository/maven/</url>
    </repository>
    <repository>
      <id>aliyunmaven</id>
      <name>aliyun</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </repository>
  </repositories>
</project>
"@

Set-Content -Path (Join-Path $OutDir "pom.xml") -Value $PomContent -Encoding UTF8

# Create basic folder structure
$SrcMainJava = Join-Path $OutDir "src\main\java"
$SrcMainResources = Join-Path $OutDir "src\main\resources"
$SrcTestJava = Join-Path $OutDir "src\test\java"
New-Item -ItemType Directory -Force -Path $SrcMainJava, $SrcMainResources, $SrcTestJava | Out-Null

# Collect source directories to copy
$DirsToCopy = @(
  "yudao-server\src\main\java",
  "yudao-framework\yudao-common\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-biz-data-permission\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-biz-ip\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-biz-tenant\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-excel\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-job\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-monitor\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-mq\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-mybatis\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-protection\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-redis\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-security\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-ekycfacecheck\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-web\src\main\java",
  "yudao-framework\yudao-spring-boot-starter-websocket\src\main\java",
  "yudao-module-system\src\main\java",
  "yudao-module-infra\src\main\java",
  "yudao-module-member\src\main\java",
  "yudao-module-bpm\src\main\java",
  "yudao-module-report\src\main\java",
  "yudao-module-mp\src\main\java",
  "yudao-module-pay\src\main\java",
  "yudao-module-mall\yudao-module-trade\src\main\java",
  "yudao-module-mall\yudao-module-trade-api\src\main\java",
  "yudao-module-mall\yudao-module-product\src\main\java",
  "yudao-module-mall\yudao-module-promotion\src\main\java",
  "yudao-module-mall\yudao-module-statistics\src\main\java",
  "yudao-module-crm\src\main\java",
  "yudao-module-erp\src\main\java"
)

foreach ($rel in $DirsToCopy) {
  $full = Join-Path $RepoRoot $rel
  if (Test-Path $full) {
    Write-Info "Copying $rel -> src/main/java"
    robocopy "$full" "$SrcMainJava" /E /NFL /NDL /NJH /NJS /NP | Out-Null
  } else {
    Write-Warn "Skip missing: $rel"
  }
}

# Copy resources
$ResDirs = @(
  "yudao-server\src\main\resources",
  "yudao-framework\yudao-common\src\main\resources",
  "yudao-framework\yudao-spring-boot-starter-mybatis\src\main\resources",
  "yudao-framework\yudao-spring-boot-starter-redis\src\main\resources",
  "yudao-framework\yudao-spring-boot-starter-web\src\main\resources",
  "yudao-module-system\src\main\resources",
  "yudao-module-infra\src\main\resources",
  "yudao-module-member\src\main\resources",
  "yudao-module-bpm\src\main\resources",
  "yudao-module-report\src\main\resources",
  "yudao-module-mp\src\main\resources",
  "yudao-module-pay\src\main\resources",
  "yudao-module-mall\yudao-module-trade\src\main\resources",
  "yudao-module-mall\yudao-module-product\src\main\resources",
  "yudao-module-mall\yudao-module-promotion\src\main\resources",
  "yudao-module-mall\yudao-module-statistics\src\main\resources",
  "yudao-module-crm\src\main\resources",
  "yudao-module-erp\src\main\resources"
)

foreach ($rel in $ResDirs) {
  $full = Join-Path $RepoRoot $rel
  if (Test-Path $full) {
    Write-Info "Copying resources $rel -> src/main/resources"
    robocopy "$full" "$SrcMainResources" /E /NFL /NDL /NJH /NJS /NP | Out-Null
  }
}

# Generate a simple application bootstrap class if not exists
$AppClassPath = Join-Path $SrcMainJava "cn\iocoder\yudao\YudaoApplication.java"
$AppDir = Split-Path -Parent $AppClassPath
if (-not (Test-Path $AppClassPath)) {
  New-Item -ItemType Directory -Force -Path $AppDir | Out-Null
  @"
package cn.iocoder.yudao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YudaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(YudaoApplication.class, args);
    }
}
"@ | Set-Content -Path $AppClassPath -Encoding UTF8
  Write-Info "Created bootstrap class: cn.iocoder.yudao.YudaoApplication"
}

# Add BUILD.md with instructions
$BuildMd = @'
# 单模块项目（生成版）

本目录由 script\\tools\\gen-single-module.ps1 脚本生成，用于将多模块工程拍平成一个可编译/可运行的单模块工程。该脚本会：

- 复制核心 Java 源码（server、framework、各业务模块）至 src\\main\\java；
- 合并资源文件至 src\\main\\resources；
- 生成一个基础 pom.xml，继承 Spring Boot Parent，并导入仓库内 yudao-dependencies 作为 BOM；
- 生成一个启动类 cn.iocoder.yudao.YudaoApplication（如原仓库未提供）。

使用方法：

1. 在仓库根目录执行：

   PowerShell（推荐）:

   ```powershell
   .\script\tools\gen-single-module.ps1 -OutputDir single-module-output
   ```

2. 进入输出目录构建运行：

   ```powershell
   cd .\single-module-output
   mvn -T 1C -U -e -DskipTests package
   java -jar .\target\yudao-single-${Revision}.jar
   ```

注意事项：

- 本脚本为“最佳努力”方式合并，难免存在类/资源冲突和多余依赖，建议根据你的业务实际进行精简与调整；
- 如果使用了可选组件（如 RocketMQ、RabbitMQ、Quartz、Iot 等），请按需在 pom.xml 中增删依赖；
- 数据源、Redis、消息队列等配置请在 application-*.yaml 中按需修改；
- 生成物不会修改原始仓库代码，可随时删除后重新生成。
'@
Set-Content -Path (Join-Path $OutDir "BUILD.md") -Value $BuildMd -Encoding UTF8

Write-Info "Single-module project generated at: $OutDir"
