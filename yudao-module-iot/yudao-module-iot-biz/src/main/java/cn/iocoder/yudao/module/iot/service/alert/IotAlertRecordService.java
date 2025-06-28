package cn.iocoder.yudao.module.iot.service.alert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod.IotAlertRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.recrod.IotAlertRecordProcessReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertRecordDO;

/**
 * IoT 告警记录 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAlertRecordService {

    /**
     * 获得告警记录
     *
     * @param id 编号
     * @return 告警记录
     */
    IotAlertRecordDO getAlertRecord(Long id);

    /**
     * 获得告警记录分页
     *
     * @param pageReqVO 分页查询
     * @return 告警记录分页
     */
    PageResult<IotAlertRecordDO> getAlertRecordPage(IotAlertRecordPageReqVO pageReqVO);

    /**
     * 处理告警记录
     *
     * @param processReqVO 处理请求
     */
    void processAlertRecord(IotAlertRecordProcessReqVO processReqVO);

    /**
     * 创建告警记录
     *
     * @param config 告警配置
     * @param deviceMessage 设备消息，可为空
     * @return 告警记录编号
     */
    Long createAlertRecord(IotAlertConfigDO config, IotDeviceMessage deviceMessage);

}