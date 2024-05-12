/**
 * 从 https://github.com/spring-projects/spring-ai 拷贝。
 *
 * 最大目的：适配 JDK8 兼容性
 *
 * 包路径：
 * 1. chat、parser、model、parser 包：https://github.com/spring-projects/spring-ai/tree/main/spring-ai-core 拷贝
 * 2. models 包：对标 https://github.com/spring-projects/spring-ai/tree/main/models 拷贝
 *  2.1 tongyi 包：【阿里】通义千问，对标 spring-cloud-alibaba 提供的 ai 包
 *  2.2 yiyan 包：【百度】文心一言，自己实现
 *  2.3 xinghuo 包：【讯飞】星火，自己实现
 *  2.4 openai 包：【OpenAI】ChatGPT，拷贝 spring-ai 提供的 models/openai 包
 *  2.5 midjourney 包：Midjourney，参考 https://github.com/novicezk/midjourney-proxy 实现
 */
package org.springframework.ai;