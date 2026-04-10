package cn.iocoder.yudao.module.mes.service.dv.repair;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.MesDvRepairPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.MesDvRepairSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.repair.MesDvRepairDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.repair.MesDvRepairMapper;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvRepairStatusEnum;
import cn.iocoder.yudao.module.mes.service.dv.machinery.MesDvMachineryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 维修工单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvRepairServiceImpl implements MesDvRepairService {

    @Resource
    private MesDvRepairMapper repairMapper;

    @Resource
    private MesDvRepairLineService repairLineService;
    @Resource
    private MesDvMachineryService machineryService;

    @Override
    public Long createRepair(MesDvRepairSaveReqVO createReqVO) {
        // 1. 校验保存数据
        validateSaveData(createReqVO);

        // 2. 插入
        MesDvRepairDO repair = BeanUtils.toBean(createReqVO, MesDvRepairDO.class);
        repair.setStatus(MesDvRepairStatusEnum.PREPARE.getStatus());
        repairMapper.insert(repair);
        return repair.getId();
    }

    @Override
    public void updateRepair(MesDvRepairSaveReqVO updateReqVO) {
        // 1.1 校验存在，且状态为草稿
        validateRepairPrepare(updateReqVO.getId());
        // 1.2 校验保存数据
        validateSaveData(updateReqVO);

        // 2. 更新
        MesDvRepairDO updateObj = BeanUtils.toBean(updateReqVO, MesDvRepairDO.class);
        repairMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRepair(Long id) {
        // 校验存在，且状态为草稿
        validateRepairPrepare(id);

        // 删除
        repairMapper.deleteById(id);
        // 级联删除子表
        repairLineService.deleteByRepairId(id);
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateSaveData(MesDvRepairSaveReqVO reqVO) {
        // 校验编码唯一
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验设备存在
        machineryService.validateMachineryExists(reqVO.getMachineryId());
    }

    private void validateCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        MesDvRepairDO repair = repairMapper.selectByCode(code);
        if (repair == null) {
            return;
        }
        if (ObjUtil.notEqual(id, repair.getId())) {
            throw exception(DV_REPAIR_CODE_DUPLICATE);
        }
    }

    @Override
    public MesDvRepairDO validateRepairExists(Long id) {
        MesDvRepairDO repair = repairMapper.selectById(id);
        if (repair == null) {
            throw exception(DV_REPAIR_NOT_EXISTS);
        }
        return repair;
    }

    @Override
    public Long getRepairCountByMachineryId(Long machineryId) {
        return repairMapper.selectCountByMachineryId(machineryId);
    }

    /**
     * 校验维修工单是否为草稿状态
     *
     * @param id 编号
     * @return 维修工单
     */
    public MesDvRepairDO validateRepairPrepare(Long id) {
        MesDvRepairDO repair = validateRepairExists(id);
        if (ObjUtil.notEqual(MesDvRepairStatusEnum.PREPARE.getStatus(), repair.getStatus())) {
            throw exception(DV_REPAIR_NOT_PREPARE);
        }
        return repair;
    }

    @Override
    public MesDvRepairDO getRepair(Long id) {
        return repairMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvRepairDO> getRepairPage(MesDvRepairPageReqVO pageReqVO) {
        return repairMapper.selectPage(pageReqVO);
    }

    @Override
    public void submitRepair(Long id, Long userId) {
        // 1. 校验存在，且状态为草稿
        validateRepairPrepare(id);

        // 2. 更新状态为维修中，设置维修人
        repairMapper.updateById(new MesDvRepairDO().setId(id)
                .setStatus(MesDvRepairStatusEnum.CONFIRMED.getStatus()).setAcceptedUserId(userId));
    }

    @Override
    public void confirmRepair(cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo.MesDvRepairConfirmReqVO confirmReqVO) {
        // 1. 校验存在，且状态为维修中
        MesDvRepairDO repair = validateRepairExists(confirmReqVO.getId());
        if (ObjUtil.notEqual(MesDvRepairStatusEnum.CONFIRMED.getStatus(), repair.getStatus())) {
            throw exception(DV_REPAIR_NOT_CONFIRMED);
        }

        // 2. 更新状态为待验收，设置维修完成日期
        repairMapper.updateById(new MesDvRepairDO().setId(confirmReqVO.getId())
                .setStatus(MesDvRepairStatusEnum.APPROVING.getStatus()).setFinishDate(confirmReqVO.getFinishDate()));
    }

    @Override
    public void finishRepair(Long id, Integer result, Long userId) {
        // 1. 校验存在，且状态为待验收
        MesDvRepairDO repair = validateRepairExists(id);
        if (ObjUtil.notEqual(MesDvRepairStatusEnum.APPROVING.getStatus(), repair.getStatus())) {
            throw exception(DV_REPAIR_NOT_APPROVING);
        }

        // 2. 更新状态为已确认，设置验收结果、验收人和验收日期
        repairMapper.updateById(new MesDvRepairDO().setId(id)
                .setStatus(MesDvRepairStatusEnum.FINISHED.getStatus()).setResult(result)
                .setConfirmUserId(userId).setConfirmDate(LocalDateTime.now()));
    }

}
