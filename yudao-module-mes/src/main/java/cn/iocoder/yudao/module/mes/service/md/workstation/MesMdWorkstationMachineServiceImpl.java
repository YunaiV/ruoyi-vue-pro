package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.machine.MesMdWorkstationMachineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationMachineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.workstation.MesMdWorkstationMachineMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.md.workstation.MesMdWorkstationMapper;
import cn.iocoder.yudao.module.mes.service.dv.machinery.MesDvMachineryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 设备资源 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdWorkstationMachineServiceImpl implements MesMdWorkstationMachineService {

    @Resource
    private MesMdWorkstationMachineMapper workstationMachineMapper;

    @Resource
    private MesMdWorkstationMapper workstationMapper;

    @Resource
    private MesDvMachineryService machineryService;

    @Override
    public Long createWorkstationMachine(MesMdWorkstationMachineSaveReqVO createReqVO) {
        // 校验数据
        validateWorkstationMachineSaveData(createReqVO);

        // 插入
        MesMdWorkstationMachineDO machine = BeanUtils.toBean(createReqVO, MesMdWorkstationMachineDO.class);
        workstationMachineMapper.insert(machine);
        return machine.getId();
    }

    private void validateWorkstationMachineSaveData(MesMdWorkstationMachineSaveReqVO reqVO) {
        // 校验设备是否存在
        machineryService.validateMachineryExists(reqVO.getMachineryId());
        // 校验该设备是否已分配到其他工作站（一台设备只能分配到一个工作站）
        MesMdWorkstationMachineDO existing = workstationMachineMapper.selectByMachineryId(reqVO.getMachineryId());
        if (existing != null) {
            MesMdWorkstationDO workstation = workstationMapper.selectById(existing.getWorkstationId());
            throw exception(MD_WORKSTATION_MACHINE_EXISTS,
                    workstation != null ? workstation.getName() : String.valueOf(existing.getWorkstationId()));
        }
    }

    @Override
    public void deleteWorkstationMachine(Long id) {
        if (workstationMachineMapper.selectById(id) == null) {
            throw exception(MD_WORKSTATION_MACHINE_NOT_EXISTS);
        }
        workstationMachineMapper.deleteById(id);
    }

    @Override
    public List<MesMdWorkstationMachineDO> getWorkstationMachineListByWorkstationId(Long workstationId) {
        return workstationMachineMapper.selectListByWorkstationId(workstationId);
    }

    @Override
    public void deleteWorkstationMachineByWorkstationId(Long workstationId) {
        workstationMachineMapper.deleteByWorkstationId(workstationId);
    }

}
