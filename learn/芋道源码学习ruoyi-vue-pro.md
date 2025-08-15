# 芋道源码学习ruoyi-vue-pro

## 从零开始

## 安装前置环境

```
JDK8:>=1.8.0_144
Maven：>=3.5.0
mysql:>=5.7
```

### 源码下载

```
git clone git@github.com:jing-jing510/ruoyi-vue-pro.git
```

### 初始化Mysql

项目使用 MySQL 存储数据，所以需要启动一个 MySQL 服务。

① 创建一个名字为 `ruoyi-vue-pro` 数据库，**【只要】** 执行对应数据库类型的 [`sql` (opens new window)](https://github.com/YunaiV/ruoyi-vue-pro/tree/master/sql)目录下的 `ruoyi-vue-pro.sql` SQL 文件，进行初始化。

![导入 MySQL 数据库](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\快速启动.png)

② 默认配置下，MySQL 需要启动在 3306 端口，并且账号是 root，密码是 123456。如果不一致，需要修改 `application-local.yaml` 配置文件。

![修改配置文件](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\1024.webp)

### 初始化redis

项目使用 Redis 缓存数据，所以需要启动一个 Redis 服务。

> 不会安装的胖友，可以选择阅读下文，良心的艿艿。
>
> - Windows 安装 Redis 指南：[http://www.iocoder.cn/Redis/windows-install(opens new window)](http://www.iocoder.cn/Redis/windows-install)
> - Mac 安装 Redis 指南：[http://www.iocoder.cn/Redis/mac-install(opens new window)](http://www.iocoder.cn/Redis/mac-install)

默认配置下，Redis 启动在 6379 端口，不设置账号密码。如果不一致，需要修改 `application-local.yaml` 配置文件。

![修改配置文件](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\1280-1754990895392-7.webp)

### 启动后端项目

[`yudao-server` (opens new window)](https://github.com/YunaiV/ruoyi-vue-pro/blob/master/yudao-server)是后端项目，提供管理后台、用户 APP 的 RESTful API 接口。

#### 编译项目

第一步，使用 IDEA 自带的 Maven 插件，进行项目的编译。如下图所示：

![后端编译](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\后端编译.png)

【可选】也可以使用 Maven 命令编译：

- 使用 IDEA 打开 Terminal 终端，在 **根目录** 下直接执行 `mvn clean install package '-Dmaven.test.skip=true'` 命令。
- 如果执行报 `Unknown lifecycle phase “.test.skip=true”` 错误，使用 `mvn clean install package -Dmaven.test.skip=true` 即可。

ps：只有首次需要执行 Maven 命令，解决基础 `pom.xml` 不存在，导致报 BaseDbUnitTest 类不存在的问题。

整个过程，预计需要 1 分钟左右。成功后，控制台日志如下：

```bash
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  9.139 s (Wall Clock)
[INFO] Finished at: 2024-05-03T18:56:03+08:00
[INFO] ------------------------------------------------------------------------
```

#### 启动项目

第二步，执行 [YudaoServerApplication (opens new window)](https://github.com/YunaiV/ruoyi-vue-pro/blob/master/yudao-server/src/main/java/cn/iocoder/yudao/server/YudaoServerApplication.java)类，进行启动。

启动还是报类不存在？

可能是 IDEA 的 bug，点击 [File -> Invalidate Caches] 菜单，清空下缓存，重启后在试试看。

![启动后端项目](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\1280-1754990933229-10.webp)

启动完成后，使用浏览器访问 [http://127.0.0.1:48080 (opens new window)](http://127.0.0.1:48080/)地址，返回如下 JSON 字符串，说明成功。

> 友情提示：注意，默认配置下，后端项目启动在 48080 端口。

```json
{
    "code": 401,
    "data": null,
    "msg": "账号未登录"
}
```

如果报 “Command line is too long” 错误，参考 [《Intellij IDEA 运行时报 Command line is too long 解决方法 》 (opens new window)](https://www.iocoder.cn/Fight/Intellij-IDEA-Indicates-that-Command-Line-is-too-long/?yudao)文章解决，或者直接点击 YudaoServerApplication **蓝字**部分！

### vue3前端页面启动

![image-20250812161713611](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\image-20250812161713611.png)

node版本要求：18.18

![image-20250812161738760](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\image-20250812161738760.png)

[`yudao-ui-admin-vue3` (opens new window)](https://github.com/yudaocode/yudao-ui-admin-vue3/)是前端 Vue3 管理后台项目。

① 克隆 [https://github.com/yudaocode/yudao-ui-admin-vue3.git (opens new window)](https://github.com/yudaocode/yudao-ui-admin-vue3.git)项目，并 Star 关注下该项目。

```
git clone https://github.com/jing-jing510/yudao-ui-admin-vue3.git
```

② 在根目录执行如下命令，进行启动：

```shell
# 安装 pnpm，提升依赖的安装速度
npm config set registry https://registry.npmmirror.com
npm install -g pnpm
# 安装依赖
pnpm install
# 启动服务
npm run dev

#废弃 会报错
#安装Yarn，提升以来的安装速度
npm install --global yarn
# 后面使用yarn安装依赖
yarn install
# 启动服务
npm run local
```

### uni-app前端页面启动

[`yudao-ui-admin-uniapp` (opens new window)](https://github.com/yudaocode/yudao-ui-admin-uniapp/)是前端 uni-app 管理后台项目。

① 克隆 [https://github.com/yudaocode/yudao-ui-admin-uniapp.git (opens new window)](https://github.com/yudaocode/yudao-ui-admin-uniapp.git)项目，并 Star 关注下该项目。

```
git clone https://github.com/jing-jing510/yudao-ui-admin-uniapp.git
```

② 下载 [HBuilder (opens new window)](https://www.dcloud.io/hbuilderx.html)工具，并进行安装。

③ 点击 HBuilder 的 [文件 -> 导入 -> 从本地项目导入...] 菜单，选择项目的 `yudao-ui-admin-uniapp` 目录。

④ 执行如下命令，安装 npm 依赖：

```bash
# 安装 npm 依赖
npm i
```

⑤ 点击 HBuilder 的 [运行 -> 运行到内置浏览器] 菜单，使用 H5 的方式运行。成功后，界面如下图所示：

![前端界面](C:\Users\jingjing\Desktop\芋道源码学习ruoyi-vue-pro.assets\1280.webp)

友情提示：登录时，滑块验证码，在内存浏览器可能存在兼容性的问题，此时使用 Chrome 浏览器，并使用“开发者工具”，设置为 iPhone 12 Pro 模式！



