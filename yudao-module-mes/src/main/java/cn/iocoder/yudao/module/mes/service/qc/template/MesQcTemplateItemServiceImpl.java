package cn.iocoder.yudao.module.mes.service.qc.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.item.MesQcTemplateItemPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.template.vo.item.MesQcTemplateItemSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.template.MesQcTemplateItemDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.template.MesQcTemplateItemMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.findFirst;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 质检方案-产品关联 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcTemplateItemServiceImpl implements MesQcTemplateItemService {

    @Resource
    private MesQcTemplateItemMapper templateItemMapper;

    @Resource
    @Lazy
    private MesQcTemplateService templateService;

    @Override
    public Long createTemplateItem(MesQcTemplateItemSaveReqVO createReqVO) {
        // 校验保存参数
        validateTemplateItemSaveData(createReqVO);

        // 插入
        MesQcTemplateItemDO item = BeanUtils.toBean(createReqVO, MesQcTemplateItemDO.class);
        templateItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateTemplateItem(MesQcTemplateItemSaveReqVO updateReqVO) {
        // 校验保存参数
        validateTemplateItemSaveData(updateReqVO);

        // 更新
        MesQcTemplateItemDO updateObj = BeanUtils.toBean(updateReqVO, MesQcTemplateItemDO.class);
        templateItemMapper.updateById(updateObj);
    }

    private void validateTemplateItemSaveData(MesQcTemplateItemSaveReqVO saveReqVO) {
        if (saveReqVO.getId() != null) {
            // 校验存在
            validateTemplateItemExists(saveReqVO.getId());
        }
        // 校验方案存在
        templateService.validateTemplateExists(saveReqVO.getTemplateId());
        // 校验产品在此方案中唯一
        validateTemplateItemNotDuplicate(saveReqVO.getId(), saveReqVO.getTemplateId(), saveReqVO.getItemId());
    }

    @Override
    public void deleteTemplateItem(Long id) {
        // 校验存在
        validateTemplateItemExists(id);
        // 删除
        templateItemMapper.deleteById(id);
    }

    private void validateTemplateItemExists(Long id) {
        if (templateItemMapper.selectById(id) == null) {
            throw exception(QC_TEMPLATE_ITEM_NOT_EXISTS);
        }
    }

    private void validateTemplateItemNotDuplicate(Long id, Long templateId, Long itemId) {
        MesQcTemplateItemDO existing = templateItemMapper.selectByTemplateIdAndItemId(templateId, itemId);
        if (existing == null) {
            return;
        }
        if (ObjUtil.notEqual(existing.getId(), id)) {
            throw exception(QC_TEMPLATE_ITEM_DUPLICATE);
        }
    }

    @Override
    public MesQcTemplateItemDO getTemplateItem(Long id) {
        return templateItemMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcTemplateItemDO> getTemplateItemPage(MesQcTemplateItemPageReqVO pageReqVO) {
        return templateItemMapper.selectPage(pageReqVO);
    }

    @Override
    public void deleteTemplateItemByTemplateId(Long templateId) {
        templateItemMapper.deleteByTemplateId(templateId);
    }

    @Override
    public MesQcTemplateItemDO getRequiredTemplateByItemIdAndType(Long itemId, Integer qcType) {
        // 1. 查出 itemId 关联的所有 templateItem
        List<MesQcTemplateItemDO> templateItems = templateItemMapper.selectListByItemId(itemId);
        if (CollUtil.isEmpty(templateItems)) {
            throw exception(QC_NO_TEMPLATE);
        }
        // 2. 筛选 types 包含 qcType 且状态为开启的模板
        List<MesQcTemplateDO> templates = templateService.getTemplateList(
                convertSet(templateItems, MesQcTemplateItemDO::getTemplateId));
        MesQcTemplateDO matchedTemplate = findFirst(templates,
                t -> CommonStatusEnum.isEnable(t.getStatus()) && CollUtil.contains(t.getTypes(), qcType));
        if (matchedTemplate == null) {
            throw exception(QC_NO_TEMPLATE);
        }
        // 3. 返回对应的 templateItem
        return findFirst(templateItems,
                item -> Objects.equals(item.getTemplateId(), matchedTemplate.getId()));
    }

}
