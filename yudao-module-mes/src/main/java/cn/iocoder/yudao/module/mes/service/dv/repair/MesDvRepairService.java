package cn.iocoder.yudao.module.mes.service.dv.repair;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.MesDvRepairConfirmReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.MesDvRepairPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.MesDvRepairSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.repair.MesDvRepairDO;
import jakarta.validation.Valid;

/**
 * MES 维修工单 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvRepairService {

    /**
     * 创建维修工单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRepair(@Valid MesDvRepairSaveReqVO createReqVO);

    /**
     * 更新维修工单
     *
     * @param updateReqVO 更新信息
     */
    void updateRepair(@Valid MesDvRepairSaveReqVO updateReqVO);

    /**
     * 删除维修工单
     *
     * @param id 编号
     */
    void deleteRepair(Long id);

    /**
     * 校验维修工单是否存在
     *
     * @param id 编号
     * @return 维修工单
     */
    MesDvRepairDO validateRepairExists(Long id);

    /**
     * 获得指定设备的维修工单数量
     *
     * @param machineryId 设备编号
     * @return 维修工单数量
     */
    Long getRepairCountByMachineryId(Long machineryId);

    /**
     * 获得维修工单
     *
     * @param id 编号
     * @return 维修工单
     */
    MesDvRepairDO getRepair(Long id);

    /**
     * 获得维修工单分页
     *
     * @param pageReqVO 分页查询
     * @return 维修工单分页
     */
    PageResult<MesDvRepairDO> getRepairPage(MesDvRepairPageReqVO pageReqVO);

    /**
     * 提交维修工单（草稿→维修中）
     *
     * @param id 编号
     */
    void submitRepair(Long id, Long userId);

    /**
     * 确认维修完成（维修中→待验收）
     *
     * @param confirmReqVO 确认信息
     */
    void confirmRepair(@Valid MesDvRepairConfirmReqVO confirmReqVO);

    /**
     * 完成验收（待验收→已确认）
     *
     * @param id 编号
     * @param result 验收结果
     * @param userId 当前登录用户编号
     */
    void finishRepair(Long id, Integer result, Long userId);

}
