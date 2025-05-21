package cn.iocoder.yudao.module.cms.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagCreateReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagPageReqVO;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagUpdateReqVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.tag.CmsTagDO;

import javax.validation.Valid;
import java.util.List;

public interface CmsTagService {

    /**
     * Create a tag
     *
     * @param createReqVO Create request VO
     * @return Tag ID
     */
    Long createTag(@Valid CmsTagCreateReqVO createReqVO);

    /**
     * Update a tag
     *
     * @param updateReqVO Update request VO
     */
    void updateTag(@Valid CmsTagUpdateReqVO updateReqVO);

    /**
     * Delete a tag
     *
     * @param id Tag ID
     */
    void deleteTag(Long id);

    /**
     * Get a tag by ID
     *
     * @param id Tag ID
     * @return Tag DO
     */
    CmsTagDO getTag(Long id);

    /**
     * Get a tag by slug
     *
     * @param slug Tag slug
     * @return Tag DO
     */
    CmsTagDO getTagBySlug(String slug);
    
    /**
     * Get tag list based on filter parameters.
     *
     * @param reqVO Filter parameters (name, slug, etc.)
     * @return List of tags
     */
    List<CmsTagDO> getTagList(CmsTagPageReqVO reqVO);


    /**
     * Get tag page
     *
     * @param pageReqVO Page request VO
     * @return Page of tags
     */
    PageResult<CmsTagDO> getTagPage(CmsTagPageReqVO pageReqVO);

}
