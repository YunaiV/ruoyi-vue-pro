package cn.iocoder.yudao.module.crm.dal.mysql.callcenter;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.callcenter.CrmCallcenterConfigDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.callcenter.CrmCallcenterUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 呼叫中心配制 Mapper
 *
 * @author fhqsuhpv
 */
@Mapper
public interface CrmCallcenterConfigMapper extends BaseMapperX<CrmCallcenterConfigDO> {

    default CrmCallcenterConfigDO selectByManufactoryId(Long manufactoryId) {
        return selectOne("manufactory_id", manufactoryId);
    }


}
