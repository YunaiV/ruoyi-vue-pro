package cn.iocoder.yudao.module.tms.service.logistic.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.ErpCustomCategoryBO;
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
public interface ErpCustomCategoryService {

    /**
     * 创建海关分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomRuleCategory(@Valid ErpCustomCategorySaveReqVO createReqVO);

    /**
     * 更新海关分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomRuleCategory(@Valid ErpCustomCategorySaveReqVO updateReqVO);

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
    ErpCustomCategoryDO getCustomRuleCategory(Long id);

    List<ErpCustomCategoryDO> listCustomRuleCategory(Collection<Long> ids);

    //getMap
    default Map<Long, ErpCustomCategoryDO> getCustomRuleCategoryMap(Collection<Long> ids) {
        return convertMap(listCustomRuleCategory(ids), ErpCustomCategoryDO::getId);
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
    PageResult<ErpCustomCategoryDO> getCustomRuleCategoryPage(ErpCustomCategoryPageReqVO pageReqVO);

    /**
     * 获得海关分类的list
     *
     * @param pageReqVO 分页查询
     * @return 海关分类list
     */
    List<ErpCustomCategoryDO> getCustomRuleCategoryList(ErpCustomCategoryPageReqVO pageReqVO);


    /**
     * 获得海关分类分页BO PageResult
     */
    PageResult<ErpCustomCategoryBO> getCustomRuleCategoryPageBO(ErpCustomCategoryPageReqVO pageReqVO);

    /**
     * 获得获得海关分类分页BO 单个对象
     */
    ErpCustomCategoryBO getCustomRuleCategoryBO(Long id);


    // ==================== 子表（海关分类子表） ====================

    /**
     * 获得海关分类子表列表
     *
     * @param customCategoryId 分类表id
     * @return 海关分类子表列表
     */
    List<ErpCustomCategoryItemDO> getCustomRuleCategoryItemListByCategoryId(Integer customCategoryId);

}