package cn.iocoder.yudao.module.erp.service.logistic.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.vo.ErpCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.category.vo.ErpCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.erp.convert.logistic.category.ErpCustomCategoryConvert;
import cn.iocoder.yudao.module.erp.convert.logistic.category.item.ErpCustomCategoryItemConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.category.ErpCustomCategoryMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.category.item.ErpCustomCategoryItemMapper;
import cn.iocoder.yudao.module.erp.service.logistic.category.item.ErpCustomCategoryItemService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_CATEGORY_NOT_EXISTS;

/**
 * 海关分类 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class ErpCustomCategoryServiceImpl implements ErpCustomCategoryService {
    @Autowired
    private DictDataApi dictDataApi;
    @Resource
    private ErpCustomCategoryMapper customRuleCategoryMapper;
    @Resource
    private ErpCustomCategoryItemMapper customRuleCategoryItemMapper;
    @Autowired
    private ErpCustomCategoryItemService itemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomRuleCategory(ErpCustomCategorySaveReqVO createReqVO) {
        //材质-字典校验
        dictDataApi.validateDictDataList("erp_product_material", List.of(String.valueOf(createReqVO.getMaterial())));
        // 插入
        ErpCustomCategoryDO customRuleCategory = ErpCustomCategoryConvert.INSTANCE.convert(createReqVO);
        customRuleCategoryMapper.insert(customRuleCategory);

        // 插入子表
        List<ErpCustomCategoryItemDO> itemDOS = ErpCustomCategoryItemConvert.INSTANCE.convert(createReqVO.getCustomRuleCategoryItems());
        itemService.createCustomRuleCategoryItemList(customRuleCategory.getId(), itemDOS);
        // 返回
        return customRuleCategory.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomRuleCategory(ErpCustomCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateCustomRuleCategoryExists(updateReqVO.getId());
        //材质-字典校验
        dictDataApi.validateDictDataList("erp_product_material", List.of(String.valueOf(updateReqVO.getMaterial())));
        // 更新
        ErpCustomCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomCategoryDO.class);
        customRuleCategoryMapper.updateById(updateObj);

        // 更新子表
        List<ErpCustomCategoryItemDO> itemDOS = ErpCustomCategoryItemConvert.INSTANCE.convert(updateReqVO.getCustomRuleCategoryItems());
        updateCustomRuleCategoryItemList(updateReqVO.getId(), itemDOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCustomRuleCategory(Long id) {
        // 校验存在
        validateCustomRuleCategoryExists(id);
        // 删除
        customRuleCategoryMapper.deleteById(id);

        // 删除子表
        deleteCustomRuleCategoryItemByCategoryId(id);
    }

    private void validateCustomRuleCategoryExists(Long id) {
        if (customRuleCategoryMapper.selectById(id) == null) {
            throw exception(CUSTOM_RULE_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public ErpCustomCategoryDO getCustomRuleCategory(Long id) {
        return customRuleCategoryMapper.selectById(id);
    }

    @Override
    public PageResult<ErpCustomCategoryDO> getCustomRuleCategoryPage(ErpCustomCategoryPageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new ErpCustomCategoryPageReqVO();
        }
        return customRuleCategoryMapper.selectPage(pageReqVO);
    }

    // ==================== 子表（海关分类子表） ====================

    @Override
    public List<ErpCustomCategoryItemDO> getCustomRuleCategoryItemListByCategoryId(Integer customCategoryId) {
        return customRuleCategoryItemMapper.selectListByCategoryId(customCategoryId);
    }

    private void createCustomRuleCategoryItemList(Long categoryId, List<ErpCustomCategoryItemDO> list) {

        list.forEach(o -> o.setCustomCategoryId(categoryId));
        customRuleCategoryItemMapper.insertBatch(list);
    }

    /**
     * 更新海关分类子表
     * @param categoryId 海关分类id
     * @param list 海关分类子表
     */
    private void updateCustomRuleCategoryItemList(Long categoryId, List<ErpCustomCategoryItemDO> list) {
        deleteCustomRuleCategoryItemByCategoryId(categoryId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createCustomRuleCategoryItemList(categoryId, list);
    }

    private void deleteCustomRuleCategoryItemByCategoryId(Long categoryId) {
        customRuleCategoryItemMapper.deleteByCategoryId(categoryId);
    }

}