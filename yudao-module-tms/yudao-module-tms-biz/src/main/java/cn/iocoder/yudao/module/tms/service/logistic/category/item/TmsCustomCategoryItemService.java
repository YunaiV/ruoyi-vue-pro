package cn.iocoder.yudao.module.tms.service.logistic.category.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.TmsCustomCategoryItemPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.TmsCustomCategoryItemSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.TmsCustomCategoryItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 海关分类子表 Service 接口
 *
 * @author 王岽宇
 */
public interface TmsCustomCategoryItemService {

    /**
     * 创建海关分类子表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomRuleCategoryItem(@Valid TmsCustomCategoryItemSaveReqVO createReqVO);

    /**
     * 创建海关分类子表
     *
     * @param categoryId 海关分类id
     * @param list       海关分类子表列表
     */
    void createCustomRuleCategoryItemList(Long categoryId, List<TmsCustomCategoryItemDO> list);

    /**
     * 更新海关分类子表
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomRuleCategoryItem(@Valid TmsCustomCategoryItemSaveReqVO updateReqVO);

    /**
     * 更新海关分类子表
     *
     * @param categoryId 海关分类id
     * @param list       海关分类子表列表
     */
    void updateCustomRuleCategoryItemList(Long categoryId, List<TmsCustomCategoryItemDO> list);

    /**
     * 删除海关分类子表
     *
     * @param id 编号
     */
    void deleteCustomRuleCategoryItem(Long id);

    /**
     * 获得海关分类子表
     *
     * @param id 编号
     * @return 海关分类子表
     */
    TmsCustomCategoryItemDO getCustomRuleCategoryItem(Long id);

    /**
     * 获得海关分类子表分页
     *
     * @param pageReqVO 分页查询
     * @return 海关分类子表分页
     */
    PageResult<TmsCustomCategoryItemDO> getCustomRuleCategoryItemPage(TmsCustomCategoryItemPageReqVO pageReqVO);

    /**
     * 构造categoryId对应的Map
     * <p>
     * categoryId : list
     *
     * @param categoryIds 分类id
     * @return Map<Long, List < TmsCustomCategoryItemDO>>
     */
    default Map<Long, List<TmsCustomCategoryItemDO>> getCustomRuleCategoryItemMap(Collection<Long> categoryIds) {
        List<TmsCustomCategoryItemDO> list = getCustomRuleCategoryItemListByCategoryId(categoryIds);
        return list.stream().collect(Collectors.groupingBy(TmsCustomCategoryItemDO::getCustomCategoryId));
    }

    /**
     * 根据分类id查询
     *
     * @param categoryIds 分类id
     * @return 海关分类子表列表
     */
    List<TmsCustomCategoryItemDO> getCustomRuleCategoryItemListByCategoryId(Collection<Long> categoryIds);
}