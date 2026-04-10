package cn.iocoder.yudao.module.mes.service.dv.checkrecord;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.MesDvCheckRecordPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.MesDvCheckRecordSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanSubjectDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkrecord.MesDvCheckRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkrecord.MesDvCheckRecordLineDO;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvCheckRecordStatusEnum;
import cn.iocoder.yudao.module.mes.enums.dv.MesDvCheckResultEnum;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.checkrecord.MesDvCheckRecordMapper;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanService;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanSubjectService;
import cn.iocoder.yudao.module.mes.service.dv.machinery.MesDvMachineryService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 设备点检记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvCheckRecordServiceImpl implements MesDvCheckRecordService {

    @Resource
    private MesDvCheckRecordMapper checkRecordMapper;
    @Resource
    @Lazy
    private MesDvCheckRecordLineService checkRecordLineService;
    @Resource
    private MesDvMachineryService machineryService;
    @Resource
    private MesDvCheckPlanService checkPlanService;
    @Resource
    private MesDvCheckPlanSubjectService checkPlanSubjectService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCheckRecord(MesDvCheckRecordSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateCheckRecordSave(createReqVO);

        // 2. 插入主记录，状态默认为草稿
        MesDvCheckRecordDO checkRecord = BeanUtils.toBean(createReqVO, MesDvCheckRecordDO.class)
                .setStatus(MesDvCheckRecordStatusEnum.DRAFT.getStatus());
        checkRecordMapper.insert(checkRecord);

        // 3. 如果指定了点检计划，自动生成明细行
        if (createReqVO.getPlanId() != null) {
            generateCheckRecordLines(checkRecord.getId(), createReqVO.getPlanId());
        }
        return checkRecord.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheckRecord(MesDvCheckRecordSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 状态为草稿
        validateCheckRecordDraft(updateReqVO.getId());
        // 1.2 校验关联数据
        validateCheckRecordSave(updateReqVO);

        // 2. 如果计划变更，删除旧明细并重新生成
        MesDvCheckRecordDO existRecord = checkRecordMapper.selectById(updateReqVO.getId());
        Long oldPlanId = existRecord.getPlanId();
        Long newPlanId = updateReqVO.getPlanId();
        boolean planChanged = ObjUtil.notEqual(oldPlanId, newPlanId);
        if (planChanged) {
            checkRecordLineService.deleteByRecordId(updateReqVO.getId());
            if (newPlanId != null) {
                generateCheckRecordLines(updateReqVO.getId(), newPlanId);
            }
        }

        // 3. 更新主记录
        MesDvCheckRecordDO updateObj = BeanUtils.toBean(updateReqVO, MesDvCheckRecordDO.class);
        checkRecordMapper.updateById(updateObj);
    }

    @Override
    public void submitCheckRecord(Long id) {
        // 1.1 校验状态为草稿
        validateCheckRecordDraft(id);
        // 1.2 校验至少有一条明细
        List<MesDvCheckRecordLineDO> lines = checkRecordLineService.getCheckRecordLineListByRecordId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(DV_CHECK_RECORD_NO_LINE);
        }

        // 2. 状态改为已完成
        MesDvCheckRecordDO updateObj = new MesDvCheckRecordDO()
                .setId(id).setStatus(MesDvCheckRecordStatusEnum.FINISHED.getStatus());
        checkRecordMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCheckRecord(Long id) {
        // 1. 校验状态为草稿（已完成不可删除）
        validateCheckRecordDraft(id);

        // 2.1 删除主记录
        checkRecordMapper.deleteById(id);
        // 2.2 级联删除明细
        checkRecordLineService.deleteByRecordId(id);
    }

    @Override
    public void validateCheckRecordExists(Long id) {
        if (checkRecordMapper.selectById(id) == null) {
            throw exception(DV_CHECK_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public Long getCheckRecordCountByMachineryId(Long machineryId) {
        return checkRecordMapper.selectCountByMachineryId(machineryId);
    }

    @Override
    public MesDvCheckRecordDO getCheckRecord(Long id) {
        return checkRecordMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvCheckRecordDO> getCheckRecordPage(MesDvCheckRecordPageReqVO pageReqVO) {
        return checkRecordMapper.selectPage(pageReqVO);
    }

    // ==================== 校验方法 ====================

    @Override
    public void validateCheckRecordDraft(Long id) {
        MesDvCheckRecordDO record = checkRecordMapper.selectById(id);
        if (record == null) {
            throw exception(DV_CHECK_RECORD_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(MesDvCheckRecordStatusEnum.DRAFT.getStatus(), record.getStatus())) {
            throw exception(DV_CHECK_RECORD_NOT_DRAFT);
        }
    }

    private void validateCheckRecordSave(MesDvCheckRecordSaveReqVO reqVO) {
        // 校验设备是否存在
        machineryService.validateMachineryExists(reqVO.getMachineryId());
        // 校验点检计划是否存在
        if (reqVO.getPlanId() != null) {
            checkPlanService.validateCheckPlanExists(reqVO.getPlanId());
        }
        // 校验点检人是否存在
        if (reqVO.getUserId() != null) {
            adminUserApi.validateUser(reqVO.getUserId());
        }
    }

    /**
     * 根据计划自动生成点检项目明细行
     */
    private void generateCheckRecordLines(Long recordId, Long planId) {
        List<MesDvCheckPlanSubjectDO> planSubjects = checkPlanSubjectService.getCheckPlanSubjectListByPlanId(planId);
        if (CollUtil.isEmpty(planSubjects)) {
            return;
        }
        List<MesDvCheckRecordLineDO> lines = new ArrayList<>(planSubjects.size());
        for (MesDvCheckPlanSubjectDO planSubject : planSubjects) {
            lines.add(new MesDvCheckRecordLineDO()
                    .setRecordId(recordId).setSubjectId(planSubject.getSubjectId())
                    .setCheckStatus(MesDvCheckResultEnum.NORMAL.getResult()));
        }
        checkRecordLineService.createCheckRecordLineList(lines);
    }

}
