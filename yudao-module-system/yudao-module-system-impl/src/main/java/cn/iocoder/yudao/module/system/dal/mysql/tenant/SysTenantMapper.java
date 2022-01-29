package cn.iocoder.yudao.module.system.dal.mysql.tenant;

import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.TenantPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 租户 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SysTenantMapper extends BaseMapperX<SysTenantDO> {

    default PageResult<SysTenantDO> selectPage(TenantPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SysTenantDO>()
                .likeIfPresent(SysTenantDO::getName, reqVO.getName())
                .likeIfPresent(SysTenantDO::getContactName, reqVO.getContactName())
                .likeIfPresent(SysTenantDO::getContactMobile, reqVO.getContactMobile())
                .eqIfPresent(SysTenantDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SysTenantDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(SysTenantDO::getId));
    }

    default List<SysTenantDO> selectList(TenantExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SysTenantDO>()
                .likeIfPresent(SysTenantDO::getName, reqVO.getName())
                .likeIfPresent(SysTenantDO::getContactName, reqVO.getContactName())
                .likeIfPresent(SysTenantDO::getContactMobile, reqVO.getContactMobile())
                .eqIfPresent(SysTenantDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SysTenantDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(SysTenantDO::getId));
    }

    default SysTenantDO selectByName(String name) {
        return selectOne(SysTenantDO::getName, name);
    }
}
