FROM node:16-alpine as DIST

WORKDIR /admim

COPY ./package.json .
COPY ./yarn.lock .
COPY ./.npmrc .
RUN yarn install

COPY . .
ARG NODE_ENV=""
RUN env ${NODE_ENV} yarn build:prod

## -- stage: dist => nginx --
FROM nginx:alpine

ENV TZ=Asia/Shanghai

COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=DIST /admim/dist /usr/share/nginx/html

EXPOSE 80
