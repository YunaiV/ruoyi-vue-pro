package cn.iocoder.yudao.module.promotion.service.diy;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplatePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplatePropertyUpdateRequestVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.template.DiyTemplateUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.diy.DiyPageConvert;
import cn.iocoder.yudao.module.promotion.convert.diy.DiyTemplateConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyTemplateDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.diy.DiyTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

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

    @Resource
    private DiyPageService diyPageService;

    // TODO @疯狂：事务；
    @Override
    public Long createDiyTemplate(DiyTemplateCreateReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        DiyTemplateDO diyTemplate = DiyTemplateConvert.INSTANCE.convert(createReqVO);
        diyTemplate.setProperty("{}");
        diyTemplateMapper.insert(diyTemplate);
        // 创建默认页面
        createDefaultPage(diyTemplate);
        // 返回
        return diyTemplate.getId();
    }

    /**
     * 创建模板下面的默认页面
     * 默认创建两个页面：首页、我的
     *
     * @param diyTemplate 模板对象
     */
    private void createDefaultPage(DiyTemplateDO diyTemplate) {
        String remark = String.format("模板【%s】自动创建", diyTemplate.getName());
        diyPageService.createDiyPage(DiyPageConvert.INSTANCE.convertCreateVo(diyTemplate.getId(), "首页", remark));
        diyPageService.createDiyPage(DiyPageConvert.INSTANCE.convertCreateVo(diyTemplate.getId(), "我的", remark));
    }

    @Override
    public void updateDiyTemplate(DiyTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateDiyTemplateExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        DiyTemplateDO updateObj = DiyTemplateConvert.INSTANCE.convert(updateReqVO);
        diyTemplateMapper.updateById(updateObj);
    }

    void validateNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        DiyTemplateDO template = diyTemplateMapper.selectByName(name);
        if (template == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的模板
        if (id == null) {
            throw exception(DIY_TEMPLATE_NAME_USED, name);
        }
        if (!template.getId().equals(id)) {
            throw exception(DIY_TEMPLATE_NAME_USED, name);
        }
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
    // TODO @疯狂：事务；
    public void useDiyTemplate(Long id) {
        // 校验存在
        validateDiyTemplateExists(id);
        // TODO @疯狂：要不已使用的情况，抛个业务异常？
        // 已使用的更新为未使用
        DiyTemplateDO used = diyTemplateMapper.selectByUsed(true);
        if (used != null) {
            // 如果 id 相同，说明未发生变化
            if (used.getId().equals(id)) {
                return;
            }
            this.updateUsed(used.getId(), false, null);
        }
        // 更新为已使用
        this.updateUsed(id, true, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDiyTemplateProperty(DiyTemplatePropertyUpdateRequestVO updateReqVO) {
        // 校验存在
        validateDiyTemplateExists(updateReqVO.getId());
        // 更新模板属性
        DiyTemplateDO updateObj = DiyTemplateConvert.INSTANCE.convert(updateReqVO);
        diyTemplateMapper.updateById(updateObj);
    }

    @Override
    public DiyTemplateDO getUsedDiyTemplate() {
        return diyTemplateMapper.selectByUsed(true);
    }

    // TODO @疯狂：挪到 useDiyTemplate 下面，改名 updateTemplateUsed 会不会好点哈；
    /**
     * 更新模板是否使用
     *
     * @param id       模板编号
     * @param used     是否使用
     * @param usedTime 使用时间
     */
    private void updateUsed(Long id, Boolean used, LocalDateTime usedTime) {
        DiyTemplateDO updateObj = new DiyTemplateDO().setId(id)
                .setUsed(used).setUsedTime(usedTime);
        diyTemplateMapper.updateById(updateObj);
    }

}
