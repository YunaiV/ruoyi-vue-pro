/**
 * 幂等组件，参考 https://github.com/it4alla/idempotent 项目实现
 * 实现原理是，相同参数的方法，一段时间内，有且仅能执行一次。通过这样的方式，保证幂等性。
 *
 * 使用场景：例如说，用户快速的双击了某个按钮，前端没有禁用该按钮，导致发送了两次重复的请求。
 *
 * 和 it4alla/idempotent 组件的差异点，主要体现在两点：
 *  1. 我们去掉了 @Idempotent 注解的 delKey 属性。原因是，本质上 delKey 为 true 时，实现的是分布式锁的能力
 * 此时，我们偏向使用 Lock4j 组件。原则上，一个组件只提供一种单一的能力。
 *  2. 考虑到组件的通用性，我们并未像 it4alla/idempotent 组件一样使用 Redisson RMap 结构，而是直接使用 Redis 的 String 数据格式。
 */
package cn.iocoder.yudao.framework.idempotent;
