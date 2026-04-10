package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.machine.MesMdWorkstationMachineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationMachineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 设备资源 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdWorkstationMachineService {

    /**
     * 创建设备资源
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWorkstationMachine(@Valid MesMdWorkstationMachineSaveReqVO createReqVO);

    /**
     * 删除设备资源
     *
     * @param id 编号
     */
    void deleteWorkstationMachine(Long id);

    /**
     * 获得设备资源列表
     *
     * @param workstationId 工作站编号
     * @return 设备资源列表
     */
    List<MesMdWorkstationMachineDO> getWorkstationMachineListByWorkstationId(Long workstationId);

    /**
     * 按工作站编号删除设备资源（级联删除）
     *
     * @param workstationId 工作站编号
     */
    void deleteWorkstationMachineByWorkstationId(Long workstationId);

}
