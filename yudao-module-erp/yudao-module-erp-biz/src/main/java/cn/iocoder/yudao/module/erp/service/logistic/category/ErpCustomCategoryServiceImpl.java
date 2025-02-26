package cn.iocoder.yudao.module.erp.service.logistic.category;

import cn.hutool.core.collection.CollUtil;
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

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
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
        Long categoryId = customRuleCategory.getId();
        // 插入子表
        List<ErpCustomCategoryItemDO> itemDOS = ErpCustomCategoryItemConvert.INSTANCE.convert(createReqVO.getCustomRuleCategoryItems());
        itemService.createCustomRuleCategoryItemList(categoryId, itemDOS);
        //同步
//        this.syncCustomRuleCategory(categoryId);
        return categoryId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomRuleCategory(ErpCustomCategorySaveReqVO updateReqVO) {
        // 校验存在
        Long categoryId = updateReqVO.getId();
        validateCustomRuleCategoryExists(categoryId);
        //材质-字典校验
        dictDataApi.validateDictDataList("erp_product_material", List.of(String.valueOf(updateReqVO.getMaterial())));
        // 更新
        ErpCustomCategoryDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomCategoryDO.class);
        customRuleCategoryMapper.updateById(updateObj);
        // 更新子表
        List<ErpCustomCategoryItemDO> itemDOS = ErpCustomCategoryItemConvert.INSTANCE.convert(updateReqVO.getCustomRuleCategoryItems());
        List<Long> itemIds = updateCustomRuleCategoryItemList(categoryId, itemDOS);
        //同步
//        this.syncCustomRuleCategoryItem(itemIds);
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

    /**
     * 获得海关分类的list
     *
     * @param pageReqVO 分页查询
     * @return 海关分类list
     */
    @Override
    public List<ErpCustomCategoryDO> getCustomRuleCategoryList(ErpCustomCategoryPageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new ErpCustomCategoryPageReqVO();
        }
        return customRuleCategoryMapper.getCustomRuleCategoryList(pageReqVO);
    }

    // ==================== 子表（海关分类子表） ====================

    @Override
    public List<ErpCustomCategoryItemDO> getCustomRuleCategoryItemListByCategoryId(Integer customCategoryId) {
        return customRuleCategoryItemMapper.selectListByCategoryId(Long.valueOf(customCategoryId));
    }

    private void createCustomRuleCategoryItemList(Long categoryId, List<ErpCustomCategoryItemDO> list) {

        list.forEach(o -> o.setCustomCategoryId(categoryId));
        customRuleCategoryItemMapper.insertBatch(list);
    }

    /**
     * 更新海关分类子表
     *
     * @param categoryId  海关分类id
     * @param itemsDOList 海关分类子表
     */
    private List<Long> updateCustomRuleCategoryItemList(Long categoryId, List<ErpCustomCategoryItemDO> itemsDOList) {
        List<ErpCustomCategoryItemDO> oldList = customRuleCategoryItemMapper.selectListByCategoryId(categoryId);
        List<List<ErpCustomCategoryItemDO>> diffedList = diffList(oldList, itemsDOList,
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        List<Long> itemIds = new ArrayList<>();
        //批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffedList.get(0))) {
            diffedList.get(0).forEach(o -> o.setCustomCategoryId(categoryId));
            customRuleCategoryItemMapper.insertBatch(diffedList.get(0));
            itemIds.addAll(diffedList.get(0).stream().map(ErpCustomCategoryItemDO::getId).toList());
        }
        if (CollUtil.isNotEmpty(diffedList.get(1))) {
            customRuleCategoryItemMapper.updateBatch(diffedList.get(1));
            itemIds.addAll(diffedList.get(1).stream().map(ErpCustomCategoryItemDO::getId).toList());
        }
        if (CollUtil.isNotEmpty(diffedList.get(2))) {
            customRuleCategoryItemMapper.deleteBatchIds(convertList(diffedList.get(2), ErpCustomCategoryItemDO::getId));
        }
        return itemIds;
    }

    private void deleteCustomRuleCategoryItemByCategoryId(Long categoryId) {
        customRuleCategoryItemMapper.deleteByCategoryId(categoryId);
    }

//    //同步海关规则方法 categoryId
//    private void syncCustomRuleCategory(Long categoryId) {
//        List<ErpCustomRuleBO> ruleBOS = customRuleMapper.selectBOListEqCountryCodeByCategoryId(new ErpCustomRulePageReqVO(), categoryId);
//        //获得变更的海关规则ids
//        List<Long> ids = ruleBOS.stream().map(ErpCustomRuleBO::getId).toList();
//        erpCustomRuleService.syncErpCustomRule(ids);
//    }
//
//    //同步海关规则方法 categoryItemId
//    private void syncCustomRuleCategoryItem(List<Long> categoryItemId) {
//        List<ErpCustomRuleBO> ruleBOS = customRuleMapper.selectBOListEqCountryCodeByItemId(new ErpCustomRulePageReqVO(), categoryItemId);
//        //获得变更的海关规则ids
//        List<Long> ids = ruleBOS.stream().map(ErpCustomRuleBO::getId).toList();
//        erpCustomRuleService.syncErpCustomRule(ids);
//    }
}