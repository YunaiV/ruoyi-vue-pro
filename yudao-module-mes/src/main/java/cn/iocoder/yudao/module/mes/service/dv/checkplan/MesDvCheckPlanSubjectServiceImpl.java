package cn.iocoder.yudao.module.mes.service.dv.checkplan;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.subject.MesDvCheckPlanSubjectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanSubjectDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.checkplan.MesDvCheckPlanSubjectMapper;
import cn.iocoder.yudao.module.mes.service.dv.subject.MesDvSubjectService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 点检保养方案项目 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvCheckPlanSubjectServiceImpl implements MesDvCheckPlanSubjectService {

    @Resource
    private MesDvCheckPlanSubjectMapper checkPlanSubjectMapper;

    @Resource
    @Lazy
    private MesDvCheckPlanService checkPlanService;
    @Resource
    private MesDvSubjectService subjectService;

    @Override
    public Long createCheckPlanSubject(MesDvCheckPlanSubjectSaveReqVO createReqVO) {
        // 1.1 校验方案草稿状态
        checkPlanService.validateCheckPlanPrepare(createReqVO.getPlanId());
        // 1.2 校验项目存在
        subjectService.validateSubjectExistsAndEnable(createReqVO.getSubjectId());
        // 1.3 校验同一方案下项目不重复
        if (checkPlanSubjectMapper.selectByPlanIdAndSubjectId(createReqVO.getPlanId(), createReqVO.getSubjectId()) != null) {
            throw exception(DV_CHECK_PLAN_SUBJECT_DUPLICATE);
        }

        // 2. 插入
        MesDvCheckPlanSubjectDO planSubject = BeanUtils.toBean(createReqVO, MesDvCheckPlanSubjectDO.class);
        checkPlanSubjectMapper.insert(planSubject);
        return planSubject.getId();
    }

    @Override
    public void deleteCheckPlanSubject(Long id) {
        // 1.1 校验存在
        MesDvCheckPlanSubjectDO existRecord = checkPlanSubjectMapper.selectById(id);
        if (existRecord == null) {
            throw exception(DV_CHECK_PLAN_SUBJECT_NOT_EXISTS);
        }
        // 1.2 校验方案草稿状态
        checkPlanService.validateCheckPlanPrepare(existRecord.getPlanId());

        // 2. 删除
        checkPlanSubjectMapper.deleteById(id);
    }

    @Override
    public List<MesDvCheckPlanSubjectDO> getCheckPlanSubjectListByPlanId(Long planId) {
        return checkPlanSubjectMapper.selectListByPlanId(planId);
    }

    @Override
    public Long getCheckPlanSubjectCountByPlanId(Long planId) {
        return checkPlanSubjectMapper.selectCountByPlanId(planId);
    }

    @Override
    public void deleteByPlanId(Long planId) {
        checkPlanSubjectMapper.deleteByPlanId(planId);
    }

    @Override
    public Long getCheckPlanSubjectCountBySubjectId(Long subjectId) {
        return checkPlanSubjectMapper.selectCountBySubjectId(subjectId);
    }

}
