# Build and Deployment

::: tip Preface

Since this is a demonstration project, the package size after building is relatively large. If there are plugins in the project that are not used, you can delete the corresponding files or routes. If they are not referenced, they will not be packaged.

:::

## Building

After the project development is completed, execute the following command to build:

**Note:** Please execute the following command in the project root directory.

```bash
pnpm build
```

After the build is successful, a `dist` folder for the corresponding application will be generated in the root directory, which contains the built and packaged files, for example: `apps/web-antd/dist/`

## Preview

Before publishing, you can preview it locally in several ways, here are two:

- Using the project's custom command for preview (recommended)

**Note：** Please execute the following command in the project root directory.

```bash
pnpm preview
```

After waiting for the build to succeed, visit `http://localhost:4173` to view the effect.

- Local server preview

You can globally install a `serve` service on your computer, such as `live-server`,

```bash
npm i -g live-server
```

Then execute the `live-server` command in the `dist` directory to view the effect locally.

```bash
cd apps/web-antd/dist
# Local preview, default port 8080
live-server
# Specify port
live-server --port 9000
```

## Compression

### Enable `gzip` Compression

To enable during the build process, change the `.env.production` configuration:

```bash
VITE_COMPRESS=gzip
```

### Enable `brotli` Compression

To enable during the build process, change the `.env.production` configuration:

```bash
VITE_COMPRESS=brotli
```

### Enable Both `gzip` and `brotli` Compression

To enable during the build process, change the `.env.production` configuration:

```bash
VITE_COMPRESS=gzip,brotli
```

::: tip Note

Both `gzip` and `brotli` require specific modules to be installed for use.

:::

::: details gzip 与 brotli 在 nginx 内的配置

```bash
http {
  # Enable gzip
  gzip on;
  # Enable gzip_static
  # After enabling gzip_static, there might be errors, requiring the installation of specific modules. The installation method can be researched independently.
  # Only with this enabled, the .gz files packaged by vue files will be effective; otherwise, there is no need to enable gzip for packaging.
  gzip_static on;
  gzip_proxied any;
  gzip_min_length 1k;
  gzip_buffers 4 16k;
  # If nginx uses multiple layers of proxy, this must be set to enable gzip.
  gzip_http_version 1.0;
  gzip_comp_level 2;
  gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/x-httpd-php image/jpeg image/gif image/png;
  gzip_vary off;
  gzip_disable "MSIE [1-6]\.";

  # Enable brotli compression
  # Requires the installation of the corresponding nginx module, which can be researched independently.
  # Can coexist with gzip without conflict.
  brotli on;
  brotli_comp_level 6;
  brotli_buffers 16 8k;
  brotli_min_length 20;
  brotli_types text/plain text/css application/json application/x-javascript text/xml application/xml application/xml+rss text/javascript application/javascript image/svg+xml;
}
```

:::

## Build Analysis

If your build files are large, you can optimize your code by analyzing the code size with the built-in [rollup-plugin-analyzer](https://github.com/doesdev/rollup-plugin-analyzer) plugin. Just execute the following command in the `root directory`:

```bash
pnpm run build:analyze
```

After running, you can see the specific distribution of sizes on the automatically opened page to analyze which dependencies are problematic.

![Build analysis report](/guide/report.png)

## Deployment

A simple deployment only requires publishing the final static files, the static files in the dist folder, to your CDN or static server. It's important to note that the index.html is usually the entry page for your backend service. After determining the static js and css, you may need to change the page's import path.

For example, to upload to an nginx server, you can upload the files under the dist folder to the server's `/srv/www/project/index.html` directory, and then access the configured domain name.

```bash
# nginx configuration
location / {
  # Do not cache html to prevent cache from continuing to be effective after program updates
  if ($request_filename ~* .*\.(?:htm|html)$) {
    add_header Cache-Control "private, no-store, no-cache, must-revalidate, proxy-revalidate";
    access_log on;
  }
  # This is the storage path for the files inside the vue packaged dist folder
  root   /srv/www/project/;
  index  index.html index.htm;
}
```

If you find the resource path is incorrect during deployment, you just need to modify the `.env.production` file.

```bash
# Configure the change according to your own path
# Note that it needs to start and end with /
VITE_BASE=/
VITE_BASE=/xxx/
```

### Integration of Frontend Routing and Server

The project uses vue-router for frontend routing, so you can choose between two modes: history and hash.

- `hash` mode will append `#` to the URL by default.
- `history` mode will not, but `history` mode requires server-side support.

You can modify the mode in `.env.production`:

```bash
VITE_ROUTER_HISTORY=hash
```

### Server Configuration for History Mode Routing

Enabling `history` mode requires server configuration. For more details on server configuration, see [history-mode](https://router.vuejs.org/guide/essentials/history-mode.html#html5-mode)

Here is an example of `nginx` configuration:

#### Deployment at the Root Directory

```bash {5}
server {
  listen 80;
  location / {
    # For use with History mode
    try_files $uri $uri/ /index.html;
  }
}
```

#### Deployment to a Non-root Directory

- First, you need to change the `.env.production` configuration during packaging:

```bash
VITE_BASE = /sub/
```

- Then configure in the nginx configuration file

```bash {8}
server {
    listen       80;
    server_name  localhost;
    location /sub/ {
      # This is the path where the vue packaged dist files are stored
      alias   /srv/www/project/;
      index index.html index.htm;
      try_files $uri $uri/ /sub/index.html;
    }
}
```

## Cross-Domain Handling

Using nginx to handle cross-domain issues after project deployment

1. Configure the frontend project API address in the `.env.production` file in the project directory:

```bash
VITE_GLOB_API_URL=/api
```

2. Configure nginx to forward requests to the backend

```bash {10-11}
server {
  listen       8080;
  server_name  localhost;
  # API proxy for solving cross-domain issues
  location /api {
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    # Backend API address
    proxy_pass http://110.110.1.1:8080/api;
    rewrite "^/api/(.*)$" /$1 break;
    proxy_redirect default;
    add_header Access-Control-Allow-Origin *;
    add_header Access-Control-Allow-Headers X-Requested-With;
    add_header Access-Control-Allow-Methods GET,POST,OPTIONS;
  }
}
```
