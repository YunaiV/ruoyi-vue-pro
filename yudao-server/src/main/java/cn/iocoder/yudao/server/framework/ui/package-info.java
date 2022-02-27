/**
 * 目的：解决后端开发，不太擅长 node 环境的配置，导致启动 yudao-ui-admin 项目一直失败
 * 所以，本项目将 yudao-ui-admin 项目通过 npm run build:demo1024 的方式，将它构建成静态资源，
 * 然后，使用 Spring Boot 作为静态资源服务器，进行启动访问。
 * 注意，这个项目仅仅作为后端开发的快速体验，并不要部署到生产环境！！！
 */
package cn.iocoder.yudao.server.framework.ui;
