package cn.iocoder.yudao.module.iot.service.rule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataBridgeDO;

import javax.validation.Valid;

/**
 * IoT 数据桥梁 Service 接口
 *
 * @author HUIHUI
 */
public interface IotDataBridgeService {

    /**
     * 创建数据桥梁
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDataBridge(@Valid IotDataBridgeSaveReqVO createReqVO);

    /**
     * 更新数据桥梁
     *
     * @param updateReqVO 更新信息
     */
    void updateDataBridge(@Valid IotDataBridgeSaveReqVO updateReqVO);

    /**
     * 删除数据桥梁
     *
     * @param id 编号
     */
    void deleteDataBridge(Long id);

    /**
     * 获得数据桥梁
     *
     * @param id 编号
     * @return 数据桥梁
     */
    IotDataBridgeDO getDataBridge(Long id);

    /**
     * 获得数据桥梁分页
     *
     * @param pageReqVO 分页查询
     * @return 数据桥梁分页
     */
    PageResult<IotDataBridgeDO> getDataBridgePage(IotDataBridgePageReqVO pageReqVO);

}