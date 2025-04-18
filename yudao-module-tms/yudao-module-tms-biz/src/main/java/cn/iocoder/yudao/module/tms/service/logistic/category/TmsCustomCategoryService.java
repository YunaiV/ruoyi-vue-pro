package cn.iocoder.yudao.module.tms.service.logistic.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.TmsCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.TmsCustomCategoryItemDO;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.TmsCustomCategoryBO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 海关分类 Service 接口
 *
 * @author 王岽宇
 */
public interface TmsCustomCategoryService {

    /**
     * 创建海关分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomRuleCategory(@Valid TmsCustomCategorySaveReqVO createReqVO);

    /**
     * 更新海关分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomRuleCategory(@Valid TmsCustomCategorySaveReqVO updateReqVO);

    /**
     * 删除海关分类
     *
     * @param id 编号
     */
    void deleteCustomRuleCategory(Long id);

    /**
     * 获得海关分类
     *
     * @param id 编号
     * @return 海关分类
     */
    TmsCustomCategoryDO getCustomRuleCategory(Long id);

    List<TmsCustomCategoryDO> listCustomRuleCategory(Collection<Long> ids);

    //getMap
    default Map<Long, TmsCustomCategoryDO> getCustomRuleCategoryMap(Collection<Long> ids) {
        return convertMap(listCustomRuleCategory(ids), TmsCustomCategoryDO::getId);
    }

    /**
     * 校验有效性
     *
     * @param ids ids
     */
    void validCustomRuleCategory(List<Long> ids);
    /**
     * 获得海关分类分页
     *
     * @param pageReqVO 分页查询
     * @return 海关分类分页
     */
    PageResult<TmsCustomCategoryDO> getCustomRuleCategoryPage(TmsCustomCategoryPageReqVO pageReqVO);

    /**
     * 获得海关分类的list
     *
     * @param pageReqVO 分页查询
     * @return 海关分类list
     */
    List<TmsCustomCategoryDO> getCustomRuleCategoryList(TmsCustomCategoryPageReqVO pageReqVO);


    /**
     * 获得海关分类分页BO PageResult
     */
    PageResult<TmsCustomCategoryBO> getCustomRuleCategoryPageBO(TmsCustomCategoryPageReqVO pageReqVO);

    /**
     * 获得获得海关分类分页BO 单个对象
     */
    TmsCustomCategoryBO getCustomRuleCategoryBO(Long id);


    // ==================== 子表（海关分类子表） ====================

    /**
     * 获得海关分类子表列表
     *
     * @param customCategoryId 分类表id
     * @return 海关分类子表列表
     */
    List<TmsCustomCategoryItemDO> getCustomRuleCategoryItemListByCategoryId(Integer customCategoryId);

}