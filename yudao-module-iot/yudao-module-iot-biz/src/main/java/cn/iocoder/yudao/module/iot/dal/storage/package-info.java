/**
 * iot 模块的时序数据存储 DAO 接口：设备属性、设备消息的统一契约，与具体的时序库无关
 *
 * 1. Service 只依赖本包的 DAO 接口
 * 2. 实现类分别在 dal/tdengine、dal/iotdb 包下，由 yudao.iot.storage.type 统一选择其中一个
 */
package cn.iocoder.yudao.module.iot.dal.storage;
