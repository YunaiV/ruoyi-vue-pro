package cn.iocoder.yudao.module.crm.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * crm 数据权限 mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CrmPermissionMapper extends BaseMapperX<CrmPermissionDO> {

    default CrmPermissionDO selectByCrmTypeAndCrmDataId(Integer crmType, Long crmDataId) {
        return selectOne(new LambdaQueryWrapperX<CrmPermissionDO>()
                .eq(CrmPermissionDO::getCrmType, crmType).eq(CrmPermissionDO::getCrmDataId, crmDataId));
    }

}
