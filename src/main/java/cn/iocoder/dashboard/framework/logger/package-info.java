/**
 * 日志组件，包括：
 *
 * 1. 用户操作日志：记录用户的操作，用于对用户的操作的审计与追溯，永久保存。
 * 2. API 日志：包含两类
 *      2.1 API 访问日志：记录用户访问 API 的访问日志，定期归档历史日志。
 *      2.2 API 异常日志：记录用户访问 API 的系统异常，方便日常排查问题与告警。
 * 3. 通用 Logger 日志：将 {@link org.slf4j.Logger} 打印的日志，只满足大于等于 {@link org.slf4j.event.Level} 进行持久化，可以理解成简易的“日志中心”。
 */
package cn.iocoder.dashboard.framework.logger;
