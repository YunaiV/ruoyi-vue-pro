package cn.iocoder.yudao.module.mes.service.dv.maintenrecord;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.maintenrecord.MesDvMaintenRecordMapper;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvMaintenRecordStatusEnum;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvCheckPlanTypeEnum;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanService;
import cn.iocoder.yudao.module.mes.service.dv.machinery.MesDvMachineryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 设备保养记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvMaintenRecordServiceImpl implements MesDvMaintenRecordService {

    @Resource
    private MesDvMaintenRecordMapper maintenRecordMapper;

    @Resource
    @Lazy
    private MesDvMaintenRecordLineService maintenRecordLineService;
    @Resource
    private MesDvMachineryService machineryService;
    @Resource
    private MesDvCheckPlanService checkPlanService;

    @Override
    public Long createMaintenRecord(MesDvMaintenRecordSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateMaintenRecordRelation(createReqVO);

        // 2. 插入
        MesDvMaintenRecordDO maintenRecord = BeanUtils.toBean(createReqVO, MesDvMaintenRecordDO.class);
        maintenRecord.setStatus(MesDvMaintenRecordStatusEnum.PREPARE.getStatus());
        maintenRecordMapper.insert(maintenRecord);
        return maintenRecord.getId();
    }

    @Override
    public void updateMaintenRecord(MesDvMaintenRecordSaveReqVO updateReqVO) {
        // 1.1 校验存在，且状态为草稿
        validateMaintenRecordDraft(updateReqVO.getId());
        // 1.2 校验关联数据
        validateMaintenRecordRelation(updateReqVO);

        // 2. 更新
        MesDvMaintenRecordDO updateObj = BeanUtils.toBean(updateReqVO, MesDvMaintenRecordDO.class);
        maintenRecordMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMaintenRecord(Long id) {
        // 1.1 校验状态为草稿
        validateMaintenRecordDraft(id);
        // 1.2 校验至少有一条明细
        List<MesDvMaintenRecordLineDO> lines = maintenRecordLineService.getMaintenRecordLineListByRecordId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(MAINTEN_RECORD_NO_LINE);
        }

        // 2. 状态改为已提交
        MesDvMaintenRecordDO record = maintenRecordMapper.selectById(id);
        MesDvMaintenRecordDO updateObj = new MesDvMaintenRecordDO();
        updateObj.setId(id);
        updateObj.setStatus(MesDvMaintenRecordStatusEnum.SUBMITTED.getStatus());
        maintenRecordMapper.updateById(updateObj);

        // 3. 回写设备台账的【最近保养时间】
        if (record.getMaintenTime() != null) {
            machineryService.updateMachineryLastMaintenTime(record.getMachineryId(), record.getMaintenTime());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaintenRecord(Long id) {
        // 校验存在，且状态为草稿
        validateMaintenRecordDraft(id);

        // 删除
        maintenRecordMapper.deleteById(id);
        // 级联删除子表
        maintenRecordLineService.deleteMaintenRecordLineByRecordId(id);
    }

    private void validateMaintenRecordRelation(MesDvMaintenRecordSaveReqVO reqVO) {
        // 校验设备是否存在
        machineryService.validateMachineryExists(reqVO.getMachineryId());
        // 校验保养计划是否存在
        if (reqVO.getPlanId() != null) {
            checkPlanService.validateCheckPlanExistsAndType(reqVO.getPlanId(), MesDvCheckPlanTypeEnum.MAINTENANCE.getType());
        }
    }

    @Override
    public void validateMaintenRecordExists(Long id) {
        if (maintenRecordMapper.selectById(id) == null) {
            throw exception(MAINTEN_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public Long getMaintenRecordCountByMachineryId(Long machineryId) {
        return maintenRecordMapper.selectCountByMachineryId(machineryId);
    }

    @Override
    public MesDvMaintenRecordDO validateMaintenRecordDraft(Long id) {
        MesDvMaintenRecordDO record = maintenRecordMapper.selectById(id);
        if (record == null) {
            throw exception(MAINTEN_RECORD_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(MesDvMaintenRecordStatusEnum.PREPARE.getStatus(), record.getStatus())) {
            throw exception(MAINTEN_RECORD_NOT_DRAFT);
        }
        return record;
    }

    @Override
    public MesDvMaintenRecordDO getMaintenRecord(Long id) {
        return maintenRecordMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvMaintenRecordDO> getMaintenRecordPage(MesDvMaintenRecordPageReqVO pageReqVO) {
        return maintenRecordMapper.selectPage(pageReqVO);
    }

}
