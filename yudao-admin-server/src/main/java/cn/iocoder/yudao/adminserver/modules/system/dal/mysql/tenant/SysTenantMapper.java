package cn.iocoder.yudao.adminserver.modules.system.dal.mysql.tenant;

import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.tenant.vo.SysTenantPageReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 租户 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SysTenantMapper extends BaseMapperX<SysTenantDO> {

    default PageResult<SysTenantDO> selectPage(SysTenantPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysTenantDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("contact_name", reqVO.getContactName())
                .likeIfPresent("contact_mobile", reqVO.getContactMobile())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default List<SysTenantDO> selectList(SysTenantExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysTenantDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("contact_name", reqVO.getContactName())
                .likeIfPresent("contact_mobile", reqVO.getContactMobile())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default SysTenantDO selectByName(String name) {
        return selectOne("name", name);
    }
}
