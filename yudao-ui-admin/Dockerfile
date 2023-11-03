FROM node:16-alpine as build-stage

WORKDIR /admim

COPY .npmrc package.json yarn.lock ./
RUN --mount=type=cache,id=yarn-store,target=/root/.yarn-store \
    yarn install --frozen-lockfile

COPY . .
ARG NODE_ENV=""
RUN env ${NODE_ENV} yarn build:prod

## -- stage: dist => nginx --
FROM nginx:alpine

ENV TZ=Asia/Shanghai

COPY ./nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build-stage /admim/dist /usr/share/nginx/html

EXPOSE 80
