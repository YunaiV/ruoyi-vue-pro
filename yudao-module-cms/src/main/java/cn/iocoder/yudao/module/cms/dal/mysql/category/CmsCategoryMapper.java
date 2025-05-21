package cn.iocoder.yudao.module.cms.dal.mysql.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cms.controller.admin.category.vo.CmsCategoryPageReqVO;
import cn.iocoder.yudao.module.cms.dal.dataobject.category.CmsCategoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CmsCategoryMapper extends BaseMapperX<CmsCategoryDO> {

    default CmsCategoryDO selectBySlug(@Param("slug") String slug, @Param("tenantId") Long tenantId) {
        return selectOne(CmsCategoryDO::getSlug, slug, CmsCategoryDO::getTenantId, tenantId);
    }

    default List<CmsCategoryDO> selectListByParentId(@Param("parentId") Long parentId, @Param("tenantId") Long tenantId) {
        return selectList(new LambdaQueryWrapperX<CmsCategoryDO>()
                .eq(CmsCategoryDO::getParentId, parentId)
                .eq(CmsCategoryDO::getTenantId, tenantId));
    }
    
    default Long selectCountByParentId(@Param("parentId") Long parentId, @Param("tenantId") Long tenantId) {
        return selectCount(new LambdaQueryWrapperX<CmsCategoryDO>()
                .eq(CmsCategoryDO::getParentId, parentId)
                .eq(CmsCategoryDO::getTenantId, tenantId));
    }

    default PageResult<CmsCategoryDO> selectPage(CmsCategoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CmsCategoryDO>()
                .likeIfPresent(CmsCategoryDO::getName, reqVO.getName())
                .orderByDesc(CmsCategoryDO::getCreateTime)); // Default sort
    }
    
    default CmsCategoryDO selectByNameAndTenantId(@Param("name") String name, @Param("tenantId") Long tenantId) {
        return selectOne(CmsCategoryDO::getName, name, CmsCategoryDO::getTenantId, tenantId);
    }

}
