package cn.iocoder.yudao.module.tms.service.logistic.category.item;

import cn.iocoder.yudao.framework.common.enums.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.category.item.ErpCustomCategoryItemMapper;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.ErrorCodeConstants.CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS;

/**
 * 海关分类子表 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class ErpCustomCategoryItemServiceImpl implements ErpCustomCategoryItemService {
    @Autowired
    private DictDataApi dictDataApi;

    @Resource
    private ErpCustomCategoryItemMapper customRuleCategoryItemMapper;

    @Override
    public Long createCustomRuleCategoryItem(ErpCustomCategoryItemSaveReqVO createReqVO) {
        dictDataApi.validateDictDataList(DictTypeConstants.COUNTRY_CODE, List.of(String.valueOf(createReqVO.getCountryCode())));
        // 插入
        ErpCustomCategoryItemDO customRuleCategoryItem = BeanUtils.toBean(createReqVO, ErpCustomCategoryItemDO.class);
        customRuleCategoryItemMapper.insert(customRuleCategoryItem);
        // 返回
        return customRuleCategoryItem.getId();
    }

    @Override
    public void createCustomRuleCategoryItemList(Long categoryId, List<ErpCustomCategoryItemDO> list) {
        list.forEach(o -> o.setCustomCategoryId(categoryId));
        customRuleCategoryItemMapper.insertBatch(list);
    }

    @Override
    public void updateCustomRuleCategoryItem(ErpCustomCategoryItemSaveReqVO updateReqVO) {
        dictDataApi.validateDictDataList(DictTypeConstants.COUNTRY_CODE, List.of(String.valueOf(updateReqVO.getCountryCode())));
        // 校验存在
        validateCustomRuleCategoryItemExists(updateReqVO.getId());
        // 更新
        ErpCustomCategoryItemDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomCategoryItemDO.class);
        customRuleCategoryItemMapper.updateById(updateObj);
    }

    @Override
    public void updateCustomRuleCategoryItemList(Long categoryId, List<ErpCustomCategoryItemDO> list) {
        deleteCustomRuleCategoryItemByCategoryId(categoryId);
        list.forEach(o -> o.setId(null).setUpdater(null).setUpdateTime(null)); // 解决更新情况下：1）id 冲突；2）updateTime 不更新
        createCustomRuleCategoryItemList(categoryId, list);
    }

    @Override
    public void deleteCustomRuleCategoryItem(Long id) {
        // 校验存在
        validateCustomRuleCategoryItemExists(id);
        // 删除
        customRuleCategoryItemMapper.deleteById(id);
    }

    private void validateCustomRuleCategoryItemExists(Long id) {
        if (customRuleCategoryItemMapper.selectById(id) == null) {
            throw exception(CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public ErpCustomCategoryItemDO getCustomRuleCategoryItem(Long id) {
        return customRuleCategoryItemMapper.selectById(id);
    }

    @Override
    public PageResult<ErpCustomCategoryItemDO> getCustomRuleCategoryItemPage(ErpCustomCategoryItemPageReqVO pageReqVO) {
        return customRuleCategoryItemMapper.selectPage(pageReqVO);
    }


    public void deleteCustomRuleCategoryItemByCategoryId(Long categoryId) {
        customRuleCategoryItemMapper.deleteByCategoryId(categoryId);
    }

    /**
     * 根据分类id查询
     *
     * @param categoryIds 分类id,为空就返回所有
     * @return 海关分类子表列表
     */
    @Override
    public List<ErpCustomCategoryItemDO> getCustomRuleCategoryItemListByCategoryId(Collection<Long> categoryIds) {
        return customRuleCategoryItemMapper.selectListByCategoryId(categoryIds);
    }
}