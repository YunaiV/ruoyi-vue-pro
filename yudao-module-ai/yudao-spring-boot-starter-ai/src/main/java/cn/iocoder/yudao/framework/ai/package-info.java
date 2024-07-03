/**
 * 从 https://github.com/spring-projects/spring-ai 拷贝。
 *
 * 最大目的：适配 JDK8 兼容性
 *
 * 包路径：
 * 1. chat、parser、model、parser 包：https://github.com/spring-projects/spring-ai/tree/main/spring-ai-core 拷贝
 * 2. models 包：对标 https://github.com/spring-projects/spring-ai/tree/main/models 拷贝
 *  2.1 xinghuo 包：【讯飞】星火，自己实现
 *  2.2 midjourney 包：Midjourney API，对接 https://github.com/novicezk/midjourney-proxy 实现
 *  2.3 suno 包：Suno API，对接 https://github.com/gcui-art/suno-api 实现
 */
package cn.iocoder.yudao.framework.ai;