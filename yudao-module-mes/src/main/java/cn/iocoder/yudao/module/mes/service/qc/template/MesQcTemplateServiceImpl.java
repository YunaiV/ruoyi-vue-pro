package cn.iocoder.yudao.module.mes.service.qc.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.MesQcTemplatePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.MesQcTemplateSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.template.MesQcTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 质检方案 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcTemplateServiceImpl implements MesQcTemplateService {

    @Resource
    private MesQcTemplateMapper templateMapper;
    @Resource
    @Lazy
    private MesQcTemplateIndicatorService templateIndicatorService;
    @Resource
    @Lazy
    private MesQcTemplateItemService templateItemService;

    @Override
    public Long createTemplate(MesQcTemplateSaveReqVO createReqVO) {
        // 校验编码唯一
        validateTemplateCodeUnique(null, createReqVO.getCode());

        // 插入
        MesQcTemplateDO template = BeanUtils.toBean(createReqVO, MesQcTemplateDO.class);
        templateMapper.insert(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(MesQcTemplateSaveReqVO updateReqVO) {
        // 校验存在
        validateTemplateExists(updateReqVO.getId());
        // 校验编码唯一
        validateTemplateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesQcTemplateDO updateObj = BeanUtils.toBean(updateReqVO, MesQcTemplateDO.class);
        templateMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        // 校验存在
        validateTemplateExists(id);

        // 删除主表
        templateMapper.deleteById(id);
        // 级联删除检测指标项
        templateIndicatorService.deleteTemplateIndicatorByTemplateId(id);
        // 级联删除产品关联
        templateItemService.deleteTemplateItemByTemplateId(id);
    }

    @Override
    public MesQcTemplateDO validateTemplateExists(Long id) {
        MesQcTemplateDO template = templateMapper.selectById(id);
        if (template == null) {
            throw exception(QC_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private void validateTemplateCodeUnique(Long id, String code) {
        MesQcTemplateDO template = templateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        if (ObjUtil.notEqual(template.getId(), id)) {
            throw exception(QC_TEMPLATE_CODE_DUPLICATE);
        }
    }

    @Override
    public MesQcTemplateDO getTemplate(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcTemplateDO> getTemplatePage(MesQcTemplatePageReqVO pageReqVO) {
        return templateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesQcTemplateDO> getTemplateList() {
        return templateMapper.selectList();
    }

    @Override
    public List<MesQcTemplateDO> getTemplateList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return templateMapper.selectByIds(ids);
    }

}

