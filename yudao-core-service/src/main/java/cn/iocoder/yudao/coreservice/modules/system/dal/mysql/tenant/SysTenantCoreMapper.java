package cn.iocoder.yudao.coreservice.modules.system.dal.mysql.tenant;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.tenant.SysTenantDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysTenantCoreMapper extends BaseMapperX<SysTenantDO> {
}
