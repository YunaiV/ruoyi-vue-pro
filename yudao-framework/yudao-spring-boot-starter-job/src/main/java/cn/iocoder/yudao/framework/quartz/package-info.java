/**
 * 1. 定时任务，采用 Quartz 实现进程内的任务执行。
 * 考虑到高可用，使用 Quartz 自带的 MySQL 集群方案。
 *
 * 2. 异步任务，采用 Spring Async 异步执行。
 */
package cn.iocoder.yudao.framework.quartz;
