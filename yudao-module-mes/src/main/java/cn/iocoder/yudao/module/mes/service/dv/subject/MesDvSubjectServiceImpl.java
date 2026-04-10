package cn.iocoder.yudao.module.mes.service.dv.subject;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.subject.vo.MesDvSubjectPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.subject.vo.MesDvSubjectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.subject.MesDvSubjectMapper;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanSubjectService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 点检保养项目 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvSubjectServiceImpl implements MesDvSubjectService {

    @Resource
    private MesDvSubjectMapper subjectMapper;
    // DONE @AI：调用对方的 service
    @Resource
    @Lazy
    private MesDvCheckPlanSubjectService checkPlanSubjectService;

    @Override
    public Long createSubject(MesDvSubjectSaveReqVO createReqVO) {
        // 校验编码唯一
        validateSubjectCodeUnique(null, createReqVO.getCode());

        // 插入
        MesDvSubjectDO subject = BeanUtils.toBean(createReqVO, MesDvSubjectDO.class);
        subjectMapper.insert(subject);
        return subject.getId();
    }

    @Override
    public void updateSubject(MesDvSubjectSaveReqVO updateReqVO) {
        // 校验存在
        validateSubjectExists(updateReqVO.getId());
        // 校验编码唯一
        validateSubjectCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesDvSubjectDO updateObj = BeanUtils.toBean(updateReqVO, MesDvSubjectDO.class);
        subjectMapper.updateById(updateObj);
    }

    @Override
    public void deleteSubject(Long id) {
        // 校验存在
        validateSubjectExists(id);
        // 校验是否被点检保养方案引用
        if (checkPlanSubjectService.getCheckPlanSubjectCountBySubjectId(id) > 0) {
            throw exception(DV_SUBJECT_USED_BY_CHECK_PLAN);
        }

        // 删除
        subjectMapper.deleteById(id);
    }

    @Override
    public void validateSubjectExists(Long id) {
        if (subjectMapper.selectById(id) == null) {
            throw exception(DV_SUBJECT_NOT_EXISTS);
        }
    }

    private void validateSubjectCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        MesDvSubjectDO subject = subjectMapper.selectByCode(code);
        if (subject == null) {
            return;
        }
        if (ObjUtil.notEqual(subject.getId(), id)) {
            throw exception(DV_SUBJECT_CODE_DUPLICATE);
        }
    }

    @Override
    public MesDvSubjectDO getSubject(Long id) {
        return subjectMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvSubjectDO> getSubjectPage(MesDvSubjectPageReqVO pageReqVO) {
        return subjectMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesDvSubjectDO> getSubjectList() {
        return subjectMapper.selectList();
    }

    @Override
    public List<MesDvSubjectDO> getSubjectList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return subjectMapper.selectByIds(ids);
    }

}
