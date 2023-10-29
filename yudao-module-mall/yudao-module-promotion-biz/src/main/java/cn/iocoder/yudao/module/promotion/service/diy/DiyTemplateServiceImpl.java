package cn.iocoder.yudao.module.promotion.service.diy;

import cn.hutool.core.util.BooleanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.diy.DiyTemplateConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DIY_TEMPLATE_NOT_EXISTS;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.DIY_TEMPLATE_USED_CANNOT_DELETE;

/**
 * 装修模板 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class DiyTemplateServiceImpl implements DiyTemplateService {

    @Resource
    private DiyTemplateMapper diyTemplateMapper;

    @Override
    public Long createDiyTemplate(DiyTemplateCreateReqVO createReqVO) {
        // 插入
        DiyTemplateDO diyTemplate = DiyTemplateConvert.INSTANCE.convert(createReqVO);
        diyTemplateMapper.insert(diyTemplate);
        // 返回
        return diyTemplate.getId();
    }

    @Override
    public void updateDiyTemplate(DiyTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateDiyTemplateExists(updateReqVO.getId());
        // 更新
        DiyTemplateDO updateObj = DiyTemplateConvert.INSTANCE.convert(updateReqVO);
        diyTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteDiyTemplate(Long id) {
        // 校验存在
        DiyTemplateDO diyTemplateDO = validateDiyTemplateExists(id);
        // 校验使用中
        if (BooleanUtil.isTrue(diyTemplateDO.getUsed())) {
            throw exception(DIY_TEMPLATE_USED_CANNOT_DELETE);
        }
        // 删除
        diyTemplateMapper.deleteById(id);
    }

    private DiyTemplateDO validateDiyTemplateExists(Long id) {
        DiyTemplateDO diyTemplateDO = diyTemplateMapper.selectById(id);
        if (diyTemplateDO == null) {
            throw exception(DIY_TEMPLATE_NOT_EXISTS);
        }
        return diyTemplateDO;
    }

    @Override
    public DiyTemplateDO getDiyTemplate(Long id) {
        return diyTemplateMapper.selectById(id);
    }

    @Override
    public PageResult<DiyTemplateDO> getDiyTemplatePage(DiyTemplatePageReqVO pageReqVO) {
        return diyTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public void useDiyTemplate(Long id) {
        // 校验存在
        validateDiyTemplateExists(id);
        // 已使用的更新为未使用
        DiyTemplateDO used = diyTemplateMapper.selectByUsed(true);
        if (used != null) {
            this.updateUsed(used.getId(), false, null);
        }
        // 更新为已使用
        this.updateUsed(id, true, LocalDateTime.now());
    }

    private void updateUsed(Long id, Boolean used, LocalDateTime usedTime) {
        DiyTemplateDO updateObj = new DiyTemplateDO().setId(id)
                .setUsed(used).setUsedTime(usedTime);
        diyTemplateMapper.updateById(updateObj);
    }

}
