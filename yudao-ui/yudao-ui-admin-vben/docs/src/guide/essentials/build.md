# 构建与部署

::: tip 前言

由于是展示项目，所以打包后相对较大，如果项目中没有用到的插件，可以删除对应的文件或者路由，不引用即可，没有引用就不会打包。

:::

## 构建

项目开发完成之后，执行以下命令进行构建：

**注意：** 请在项目根目录下执行以下命令

```bash
pnpm build
```

构建打包成功之后，会在根目录生成对应的应用下的 `dist` 文件夹，里面就是构建打包好的文件，例如: `apps/web-antd/dist/`

## 预览

发布之前可以在本地进行预览，有多种方式，这里介绍两种：

- 使用项目自定的命令进行预览(推荐)

**注意：** 请在项目根目录下执行以下命令

```bash
pnpm preview
```

等待构建成功后，访问 `http://localhost:4173` 即可查看效果。

- 本地服务器预览

可以在电脑全局安装 `serve` 服务，如 `live-server`,

```bash
npm i -g live-server
```

然后在 `dist` 目录下执行 `live-server` 命令，即可在本地查看效果。

```bash
cd apps/web-antd/dist
# 本地预览，默认端口8080
live-server
# 指定端口
live-server --port=9000
```

## 压缩

### 开启 `gzip` 压缩

需要在打包的时候更改`.env.production`配置:

```bash
VITE_COMPRESS=gzip
```

### 开启 `brotli` 压缩

需要在打包的时候更改`.env.production`配置:

```bash
VITE_COMPRESS=brotli
```

### 同时开启 `gzip` 和 `brotli` 压缩

需要在打包的时候更改`.env.production`配置:

```bash
VITE_COMPRESS=gzip,brotli
```

::: tip 提示

`gzip` 和 `brotli` 都需要安装特定模块才能使用。

:::

::: details gzip 与 brotli 在 nginx 内的配置

```bash
http {
  # 开启gzip
  gzip on;
  # 开启gzip_static
  # gzip_static 开启后可能会报错，需要安装相应的模块, 具体安装方式可以自行查询
  # 只有这个开启，vue文件打包的.gz文件才会有效果，否则不需要开启gzip进行打包
  gzip_static on;
  gzip_proxied any;
  gzip_min_length 1k;
  gzip_buffers 4 16k;
  #如果nginx中使用了多层代理 必须设置这个才可以开启gzip。
  gzip_http_version 1.0;
  gzip_comp_level 2;
  gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/x-httpd-php image/jpeg image/gif image/png;
  gzip_vary off;
  gzip_disable "MSIE [1-6]\.";

  # 开启 brotli压缩
  # 需要安装对应的nginx模块,具体安装方式可以自行查询
  # 可以与gzip共存不会冲突
  brotli on;
  brotli_comp_level 6;
  brotli_buffers 16 8k;
  brotli_min_length 20;
  brotli_types text/plain text/css application/json application/x-javascript text/xml application/xml application/xml+rss text/javascript application/javascript image/svg+xml;
}
```

:::

## 构建分析

如果你的构建文件很大，可以通过项目内置 [rollup-plugin-analyzer](https://github.com/doesdev/rollup-plugin-analyzer) 插件进行代码体积分析，从而优化你的代码。只需要在`根目录`下执行以下命令：

```bash
pnpm run build:analyze
```

运行之后，在自动打开的页面可以看到具体的体积分布，以分析哪些依赖有问题。

![Build analysis report](/guide/report.png)

## 部署

简单的部署只需要将最终生成的静态文件，dist 文件夹的静态文件发布到你的 cdn 或者静态服务器即可，需要注意的是其中的 index.html 通常会是你后台服务的入口页面，在确定了 js 和 css 的静态之后可能需要改变页面的引入路径。

例如上传到 nginx 服务器，可以将 dist 文件夹下的文件上传到服务器的 `/srv/www/project/index.html` 目录下，然后访问配置好的域名即可。

```bash
# nginx配置
location / {
  # 不缓存html，防止程序更新后缓存继续生效
  if ($request_filename ~* .*\.(?:htm|html)$) {
    add_header Cache-Control "private, no-store, no-cache, must-revalidate, proxy-revalidate";
    access_log on;
  }
  # 这里是vue打包文件dist内的文件的存放路径
  root   /srv/www/project/;
  index  index.html index.htm;
}
```

部署时可能会发现资源路径不对，只需要修改`.env.production`文件即可。

```bash
# 根据自己路径来配置更改
# 注意需要以 / 开头和结尾
VITE_BASE=/
VITE_BASE=/xxx/
```

### 前端路由与服务端的结合

项目前端路由使用的是 vue-router，所以你可以选择两种方式：history 和 hash。

- `hash` 默认会在 url 后面拼接`#`
- `history` 则不会，不过 `history` 需要服务器配合

可在 `.env.production` 内进行 mode 修改

```bash
VITE_ROUTER_HISTORY=hash
```

### history 路由模式下服务端配置

开启 `history` 模式需要服务器配置，更多的服务器配置详情可以看 [history-mode](https://router.vuejs.org/guide/essentials/history-mode.html#html5-mode)

这里以 `nginx` 配置为例：

#### 部署到根目录

```bash {5}
server {
  listen 80;
  location / {
    # 用于配合 History 使用
    try_files $uri $uri/ /index.html;
  }
}
```

#### 部署到非根目录

- 首先需要在打包的时候更改`.env.production`配置:

```bash
VITE_BASE = /sub/
```

- 然后在 nginx 配置文件中配置

```bash {8}
server {
    listen       80;
    server_name  localhost;
    location /sub/ {
      # 这里是vue打包文件dist内的文件的存放路径
      alias   /srv/www/project/;
      index index.html index.htm;
      try_files $uri $uri/ /sub/index.html;
    }
}
```

## 跨域处理

使用 nginx 处理项目部署后的跨域问题

1. 配置前端项目接口地址，在项目目录下的`.env.production`文件中配置：

```bash
VITE_GLOB_API_URL=/api
```

2. 在 nginx 配置请求转发到后台

```bash {10-11}
server {
  listen       8080;
  server_name  localhost;
  # 接口代理，用于解决跨域问题
  location /api {
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    # 后台接口地址
    proxy_pass http://110.110.1.1:8080/api;
    rewrite "^/api/(.*)$" /$1 break;
    proxy_redirect default;
    add_header Access-Control-Allow-Origin *;
    add_header Access-Control-Allow-Headers X-Requested-With;
    add_header Access-Control-Allow-Methods GET,POST,OPTIONS;
  }
}
```
