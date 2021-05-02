/**
 * 对于工具类的选择，优先查找 Hutool 中有没对应的方法
 * 如果没有，则自己封装对应的工具类，以 Utils 结尾，用于区分
 *
 * ps：如果担心 Hutool 存在坑的问题，可以阅读 Hutool 的实现源码，以确保可靠性。并且，可以补充相关的单元测试。
 */
package cn.iocoder.yudao.framework.common.util;
