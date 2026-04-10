package cn.iocoder.yudao.module.mes.service.dv.repair;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.line.MesDvRepairLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.line.MesDvRepairLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.repair.MesDvRepairLineDO;

import jakarta.validation.Valid;

/**
 * MES 维修工单行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvRepairLineService {

    /**
     * 创建维修工单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRepairLine(@Valid MesDvRepairLineSaveReqVO createReqVO);

    /**
     * 更新维修工单行
     *
     * @param updateReqVO 更新信息
     */
    void updateRepairLine(@Valid MesDvRepairLineSaveReqVO updateReqVO);

    /**
     * 删除维修工单行
     *
     * @param id 编号
     */
    void deleteRepairLine(Long id);

    /**
     * 获得维修工单行
     *
     * @param id 编号
     * @return 维修工单行
     */
    MesDvRepairLineDO getRepairLine(Long id);

    /**
     * 获得维修工单行分页
     *
     * @param pageReqVO 分页查询
     * @return 维修工单行分页
     */
    PageResult<MesDvRepairLineDO> getRepairLinePage(MesDvRepairLinePageReqVO pageReqVO);

    /**
     * 根据维修工单编号删除所有行
     *
     * @param repairId 维修工单编号
     */
    void deleteByRepairId(Long repairId);

}
