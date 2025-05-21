package cn.iocoder.yudao.module.cms.dal.mysql.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cms.controller.admin.tag.vo.CmsTagPageReqVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.tag.CmsTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CmsTagMapper extends BaseMapperX<CmsTagDO> {

    default CmsTagDO selectBySlug(@Param("slug") String slug, @Param("tenantId") Long tenantId) {
        return selectOne(CmsTagDO::getSlug, slug, CmsTagDO::getTenantId, tenantId);
    }

    default CmsTagDO selectByName(@Param("name") String name, @Param("tenantId") Long tenantId) {
        return selectOne(CmsTagDO::getName, name, CmsTagDO::getTenantId, tenantId);
    }

    default PageResult<CmsTagDO> selectPage(CmsTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CmsTagDO>()
                .likeIfPresent(CmsTagDO::getName, reqVO.getName())
                .likeIfPresent(CmsTagDO::getSlug, reqVO.getSlug())
                .orderByDesc(CmsTagDO::getCreateTime)); // Default sort
    }
}
