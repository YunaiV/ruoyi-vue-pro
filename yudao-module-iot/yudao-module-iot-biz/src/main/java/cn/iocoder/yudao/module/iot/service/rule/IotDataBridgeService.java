package cn.iocoder.yudao.module.iot.service.rule;

import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;

/**
 * IoT 数据桥梁的 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDataBridgeService {

    /**
     * 获得指定数据桥梁
     *
     * @param id 数据桥梁编号
     * @return 数据桥梁
     */
    IotDataBridgeDO getIotDataBridge(Long id);

}
