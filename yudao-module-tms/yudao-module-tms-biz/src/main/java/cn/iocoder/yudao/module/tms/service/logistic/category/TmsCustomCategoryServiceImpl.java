package cn.iocoder.yudao.module.tms.service.logistic.category;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.convert.logistic.category.TmsCustomCategoryConvert;
import cn.iocoder.yudao.module.tms.convert.logistic.category.item.TmsCustomCategoryItemConvert;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.TmsCustomCategoryItemDO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.TmsCustomCategoryMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.item.TmsCustomCategoryItemMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule.TmsCustomRuleMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.TmsCustomCategoryBO;
import cn.iocoder.yudao.module.tms.service.logistic.category.item.TmsCustomCategoryItemService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.tms.enums.DictValue.PRODUCT_MATERIAL;

/**
 * 海关分类 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class TmsCustomCategoryServiceImpl implements TmsCustomCategoryService {
    @Autowired
    private DictDataApi dictDataApi;
    @Resource
    private TmsCustomCategoryMapper customRuleCategoryMapper;
    @Resource
    private TmsCustomCategoryItemMapper customRuleCategoryItemMapper;
    @Autowired
    private TmsCustomRuleMapper tmsCustomRuleMapper;
    @Autowired
    private TmsCustomCategoryItemService itemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomRuleCategory(TmsCustomCategorySaveReqVO createReqVO) {
        //材质-字典校验
        dictDataApi.validateDictDataList(PRODUCT_MATERIAL.getName(), List.of(String.valueOf(createReqVO.getMaterial())));
        validateCustomRuleCategoryNotExists(createReqVO);
        // 插入
        TmsCustomCategoryDO customRuleCategory = TmsCustomCategoryConvert.INSTANCE.convert(createReqVO);
        customRuleCategoryMapper.insert(customRuleCategory);
        Long categoryId = customRuleCategory.getId();
        // 插入子表
        List<TmsCustomCategoryItemDO> itemDOS = TmsCustomCategoryItemConvert.INSTANCE.convert(createReqVO.getCustomRuleCategoryItems());
        itemService.createCustomRuleCategoryItemList(categoryId, itemDOS);
        //同步
//        this.syncCustomRuleCategory(categoryId);
        return categoryId;
    }

    //海关分类重复性校验
    private void validateCustomRuleCategoryNotExists(TmsCustomCategorySaveReqVO createReqVO) {
        Collection<TmsCustomCategoryDO> categoryList =
            customRuleCategoryMapper.getCustomRuleByMaterialAndDeclaredType(
                createReqVO.getMaterial(), createReqVO.getDeclaredType());

        if (CollectionUtils.isNotEmpty(categoryList)) {
            throw exception(ErrorCodeConstants.CUSTOM_RULE_CATEGORY_EXISTS, createReqVO.getDeclaredType());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomRuleCategory(TmsCustomCategorySaveReqVO updateReqVO) {
        // 校验存在
        Long categoryId = updateReqVO.getId();
        validateCustomRuleCategoryExists(categoryId);
        //材质-字典校验
        dictDataApi.validateDictDataList(PRODUCT_MATERIAL.getName(), List.of(String.valueOf(updateReqVO.getMaterial())));
        // 更新
        TmsCustomCategoryDO updateObj = BeanUtils.toBean(updateReqVO, TmsCustomCategoryDO.class);
        customRuleCategoryMapper.updateById(updateObj);
        // 更新子表
        List<TmsCustomCategoryItemDO> itemDOS = TmsCustomCategoryItemConvert.INSTANCE.convert(updateReqVO.getCustomRuleCategoryItems());
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
            throw exception(ErrorCodeConstants.CUSTOM_RULE_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public TmsCustomCategoryDO getCustomRuleCategory(Long id) {
        return customRuleCategoryMapper.selectById(id);
    }

    //get list方法
    public List<TmsCustomCategoryDO> listCustomRuleCategory(Collection<Long> ids) {
        return customRuleCategoryMapper.selectByIds(ids);
    }

    @Override
    public void validCustomRuleCategory(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<TmsCustomCategoryDO> list = customRuleCategoryMapper.selectBatchIds(ids);
        Map<Long, TmsCustomCategoryDO> map = convertMap(list, TmsCustomCategoryDO::getId);
        for (Long id : ids) {
            TmsCustomCategoryDO aDo = map.get(id);
            ThrowUtil.ifEmptyThrow(aDo, ErrorCodeConstants.CUSTOM_RULE_CATEGORY_NOT_EXISTS);
        }
    }

    @Override
    public PageResult<TmsCustomCategoryDO> getCustomRuleCategoryPage(TmsCustomCategoryPageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new TmsCustomCategoryPageReqVO();
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
    public List<TmsCustomCategoryDO> getCustomRuleCategoryList(TmsCustomCategoryPageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new TmsCustomCategoryPageReqVO();
        }
        return customRuleCategoryMapper.getCustomRuleCategoryList(pageReqVO);
    }

    @Override
    public PageResult<TmsCustomCategoryBO> getCustomRuleCategoryPageBO(TmsCustomCategoryPageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new TmsCustomCategoryPageReqVO();
        }
        return customRuleCategoryMapper.selectPageBO(pageReqVO);
    }

    @Override
    public TmsCustomCategoryBO getCustomRuleCategoryBO(Long id) {
        if (id == null) {
            return null;
        }
        return customRuleCategoryMapper.getTmsCustomCategoryBOById(id);
    }
    // ==================== 子表（海关分类子表） ====================

    @Override
    public List<TmsCustomCategoryItemDO> getCustomRuleCategoryItemListByCategoryId(Integer customCategoryId) {
        return customRuleCategoryItemMapper.selectListByCategoryId(Long.valueOf(customCategoryId));
    }

    private void createCustomRuleCategoryItemList(Long categoryId, List<TmsCustomCategoryItemDO> list) {

        list.forEach(o -> o.setCustomCategoryId(categoryId));
        customRuleCategoryItemMapper.insertBatch(list);
    }

    /**
     * 更新海关分类子表
     *
     * @param categoryId  海关分类id
     * @param itemsDOList 海关分类子表
     */
    private List<Long> updateCustomRuleCategoryItemList(Long categoryId, List<TmsCustomCategoryItemDO> itemsDOList) {
        List<TmsCustomCategoryItemDO> oldList = customRuleCategoryItemMapper.selectListByCategoryId(categoryId);
        List<List<TmsCustomCategoryItemDO>> diffedList = diffList(oldList, itemsDOList,
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        List<Long> itemIds = new ArrayList<>();
        //批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffedList.get(0))) {
            diffedList.get(0).forEach(o -> o.setCustomCategoryId(categoryId));
            customRuleCategoryItemMapper.insertBatch(diffedList.get(0));
            itemIds.addAll(diffedList.get(0).stream().map(TmsCustomCategoryItemDO::getId).toList());
        }
        if (CollUtil.isNotEmpty(diffedList.get(1))) {
            customRuleCategoryItemMapper.updateBatch(diffedList.get(1));
            itemIds.addAll(diffedList.get(1).stream().map(TmsCustomCategoryItemDO::getId).toList());
        }
        if (CollUtil.isNotEmpty(diffedList.get(2))) {
            customRuleCategoryItemMapper.deleteByIds(convertList(diffedList.get(2), TmsCustomCategoryItemDO::getId));
        }
        return itemIds;
    }

    private void deleteCustomRuleCategoryItemByCategoryId(Long categoryId) {
        customRuleCategoryItemMapper.deleteByCategoryId(categoryId);
    }

//    //同步海关规则方法 categoryId
//    private void syncCustomRuleCategory(Long categoryId) {
//        List<TmsCustomRuleBO> ruleBOS = customRuleMapper.selectBOListEqCountryCodeByCategoryId(new TmsCustomRulePageReqVO(), categoryId);
//        //获得变更的海关规则ids
//        List<Long> ids = ruleBOS.stream().map(TmsCustomRuleBO::getId).toList();
//        erpCustomRuleService.syncErpCustomRule(ids);
//    }
//
//    //同步海关规则方法 categoryItemId
//    private void syncCustomRuleCategoryItem(List<Long> categoryItemId) {
//        List<TmsCustomRuleBO> ruleBOS = customRuleMapper.selectBOListEqCountryCodeByItemId(new TmsCustomRulePageReqVO(), categoryItemId);
//        //获得变更的海关规则ids
//        List<Long> ids = ruleBOS.stream().map(TmsCustomRuleBO::getId).toList();
//        erpCustomRuleService.syncErpCustomRule(ids);
//    }
}