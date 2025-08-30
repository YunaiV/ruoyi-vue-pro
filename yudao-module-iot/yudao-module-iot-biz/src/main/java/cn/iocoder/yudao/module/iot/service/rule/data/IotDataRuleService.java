package cn.iocoder.yudao.module.iot.service.rule.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRulePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.data.rule.IotDataRuleSaveReqVO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 数据流转规则 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDataRuleService {

    /**
     * 创建数据流转规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDataRule(@Valid IotDataRuleSaveReqVO createReqVO);

    /**
     * 更新数据流转规则
     *
     * @param updateReqVO 更新信息
     */
    void updateDataRule(@Valid IotDataRuleSaveReqVO updateReqVO);

    /**
     * 删除数据流转规则
     *
     * @param id 编号
     */
    void deleteDataRule(Long id);

    /**
     * 获得数据流转规则
     *
     * @param id 编号
     * @return 数据流转规则
     */
    IotDataRuleDO getDataRule(Long id);

    /**
     * 获得数据流转规则分页
     *
     * @param pageReqVO 分页查询
     * @return 数据流转规则分页
     */
    PageResult<IotDataRuleDO> getDataRulePage(IotDataRulePageReqVO pageReqVO);

    /**
     * 根据数据目的编号，获得数据流转规则列表
     *
     * @param sinkId 数据目的编号
     * @return 是否被使用
     */
    List<IotDataRuleDO> getDataRuleListBySinkId(Long sinkId);

    /**
     * 执行数据流转规则
     *
     * @param message 消息
     */
    void executeDataRule(IotDeviceMessage message);

}