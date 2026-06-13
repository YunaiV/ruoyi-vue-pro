package cn.iocoder.yudao.module.mes.service.qc.template;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.indicator.MesQcTemplateIndicatorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.indicator.MesQcTemplateIndicatorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.template.MesQcTemplateIndicatorMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.QC_TEMPLATE_INDICATOR_NOT_EXISTS;

/**
 * MES 质检方案-检测指标项 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcTemplateIndicatorServiceImpl implements MesQcTemplateIndicatorService {

    @Resource
    private MesQcTemplateIndicatorMapper templateIndicatorMapper;

    @Resource
    @Lazy
    private MesQcTemplateService templateService;

    @Override
    public Long createTemplateIndicator(MesQcTemplateIndicatorSaveReqVO createReqVO) {
        // 校验方案存在
        templateService.validateTemplateExists(createReqVO.getTemplateId());

        // 插入
        MesQcTemplateIndicatorDO indicator = BeanUtils.toBean(createReqVO, MesQcTemplateIndicatorDO.class);
        templateIndicatorMapper.insert(indicator);
        return indicator.getId();
    }

    @Override
    public void updateTemplateIndicator(MesQcTemplateIndicatorSaveReqVO updateReqVO) {
        // 校验存在
        validateTemplateIndicatorExists(updateReqVO.getId());

        // 更新
        MesQcTemplateIndicatorDO updateObj = BeanUtils.toBean(updateReqVO, MesQcTemplateIndicatorDO.class);
        templateIndicatorMapper.updateById(updateObj);
    }

    @Override
    public void deleteTemplateIndicator(Long id) {
        // 校验存在
        validateTemplateIndicatorExists(id);
        // 删除
        templateIndicatorMapper.deleteById(id);
    }

    private void validateTemplateIndicatorExists(Long id) {
        if (templateIndicatorMapper.selectById(id) == null) {
            throw exception(QC_TEMPLATE_INDICATOR_NOT_EXISTS);
        }
    }

    @Override
    public MesQcTemplateIndicatorDO getTemplateIndicator(Long id) {
        return templateIndicatorMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcTemplateIndicatorDO> getTemplateIndicatorPage(MesQcTemplateIndicatorPageReqVO pageReqVO) {
        return templateIndicatorMapper.selectPage(pageReqVO);
    }

    @Override
    public void deleteTemplateIndicatorByTemplateId(Long templateId) {
        templateIndicatorMapper.deleteByTemplateId(templateId);
    }

    @Override
    public List<MesQcTemplateIndicatorDO> getTemplateIndicatorListByTemplateId(Long templateId) {
        return templateIndicatorMapper.selectListByTemplateId(templateId);
    }

    @Override
    public Long getTemplateIndicatorCountByUnitMeasureId(Long unitMeasureId) {
        return templateIndicatorMapper.selectCountByUnitMeasureId(unitMeasureId);
    }

}
