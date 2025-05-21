package cn.iocoder.yudao.module.cms.service.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.CmsCategoryCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.CmsCategoryPageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.CmsCategoryUpdateReqVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.category.CmsCategoryDO;

import javax.validation.Valid;
import java.util.List;

public interface CmsCategoryService {

    /**
     * Create a category
     *
     * @param createReqVO Create request VO
     * @return Category ID
     */
    Long createCategory(@Valid CmsCategoryCreateReqVO createReqVO);

    /**
     * Update a category
     *
     * @param updateReqVO Update request VO
     */
    void updateCategory(@Valid CmsCategoryUpdateReqVO updateReqVO);

    /**
     * Delete a category
     *
     * @param id Category ID
     */
    void deleteCategory(Long id);

    /**
     * Get a category by ID
     *
     * @param id Category ID
     * @return Category DO
     */
    CmsCategoryDO getCategory(Long id);

    /**
     * Get a category by slug
     *
     * @param slug Category slug
     * @return Category DO
     */
    CmsCategoryDO getCategoryBySlug(String slug);
    
    /**
     * Get category list based on filter parameters.
     * Used for /list-simple endpoint.
     *
     * @param reqVO Filter parameters (name, etc.)
     * @return List of categories
     */
    List<CmsCategoryDO> getCategoryList(CmsCategoryPageReqVO reqVO);


    /**
     * Get category page
     *
     * @param pageReqVO Page request VO
     * @return Page of categories
     */
    PageResult<CmsCategoryDO> getCategoryPage(CmsCategoryPageReqVO pageReqVO);

}
