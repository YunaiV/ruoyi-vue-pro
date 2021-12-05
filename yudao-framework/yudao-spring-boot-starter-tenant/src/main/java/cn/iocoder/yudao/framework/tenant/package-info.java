/**
 * 多租户，支持如下层面：
 * 1. DB：基于 MyBatis Plus 多租户的功能实现。
 * 2. Web：请求 HTTP API 时，Header 带上 tenant-id 租户编号。
 * 3. Job：在 JobHandler 执行任务时，会按照每个租户，都独立并行执行一次。
 * 4. MQ：TODO
 */
package cn.iocoder.yudao.framework.tenant;
