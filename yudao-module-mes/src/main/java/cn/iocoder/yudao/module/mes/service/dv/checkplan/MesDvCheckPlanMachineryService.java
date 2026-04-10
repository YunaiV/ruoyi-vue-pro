package cn.iocoder.yudao.module.mes.service.dv.checkplan;

import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.machinery.MesDvCheckPlanMachinerySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanMachineryDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 点检保养方案设备 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvCheckPlanMachineryService {

    /**
     * 创建方案设备关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCheckPlanMachinery(@Valid MesDvCheckPlanMachinerySaveReqVO createReqVO);

    /**
     * 删除方案设备关联
     *
     * @param id 编号
     */
    void deleteCheckPlanMachinery(Long id);

    /**
     * 获得指定方案的设备列表
     *
     * @param planId 方案编号
     * @return 设备关联列表
     */
    List<MesDvCheckPlanMachineryDO> getCheckPlanMachineryListByPlanId(Long planId);

    /**
     * 获得指定方案的设备数量
     *
     * @param planId 方案编号
     * @return 设备数量
     */
    Long getCheckPlanMachineryCountByPlanId(Long planId);

    /**
     * 根据方案编号删除所有设备关联
     *
     * @param planId 方案编号
     */
    void deleteByPlanId(Long planId);

    /**
     * 获得指定设备关联的方案设备数量
     *
     * @param machineryId 设备编号
     * @return 方案设备数量
     */
    Long getCheckPlanMachineryCountByMachineryId(Long machineryId);

    /**
     * 获得指定设备关联的方案设备列表
     *
     * @param machineryId 设备编号
     * @return 方案设备列表
     */
    List<MesDvCheckPlanMachineryDO> getCheckPlanMachineryListByMachineryId(Long machineryId);

}
