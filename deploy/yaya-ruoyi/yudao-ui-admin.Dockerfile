FROM node:22-alpine AS build

WORKDIR /app
RUN corepack enable && corepack prepare pnpm@9.15.9 --activate

COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN node --max_old_space_size=3072 ./node_modules/vite/bin/vite.js build --mode prod

FROM nginx:1.27-alpine

RUN printf '%s\n' \
  'server {' \
  '  listen 80;' \
  '  server_name _;' \
  '  root /usr/share/nginx/html;' \
  '  index index.html;' \
  '  location / {' \
  '    try_files $uri $uri/ /index.html;' \
  '  }' \
  '}' \
  > /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist-prod /usr/share/nginx/html

EXPOSE 80
