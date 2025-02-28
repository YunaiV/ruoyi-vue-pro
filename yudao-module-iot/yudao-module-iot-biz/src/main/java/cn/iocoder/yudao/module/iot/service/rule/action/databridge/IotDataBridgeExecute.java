package cn.iocoder.yudao.module.iot.service.rule.action.databridge;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;
import cn.iocoder.yudao.module.iot.mq.message.IotDeviceMessage;


/**
 * IoT 数据桥梁的执行器 execute 接口
 *
 * @author HUIHUI
 */
public interface IotDataBridgeExecute {

    /**
     * 执行数据桥梁操作
     *
     * @param message    设备消息
     * @param dataBridge 数据桥梁
     */
    void execute(IotDeviceMessage message, IotDataBridgeDO dataBridge);

    // TODO @芋艿：因为下面的，都是有状态的，所以通过 guava 缓存连接，然后通过 RemovalNotification 实现关闭。例如说，一次新建有效期是 10 分钟；
    // TODO @芋艿：mq-redis
    // TODO @芋艿：mq-数据库
    // TODO @芋艿：kafka
    // TODO @芋艿：rocketmq
    // TODO @芋艿：rabbitmq
    // TODO @芋艿：mqtt
    // TODO @芋艿：tcp
    // TODO @芋艿：websocket

}
