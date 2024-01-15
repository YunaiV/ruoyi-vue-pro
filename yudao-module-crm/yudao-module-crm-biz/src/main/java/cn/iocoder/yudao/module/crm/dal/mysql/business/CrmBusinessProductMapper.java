package cn.iocoder.yudao.module.crm.dal.mysql.business;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商机产品 Mapper
 * @author lzxhqs
 */
@Mapper
public interface CrmBusinessProductMapper extends BaseMapperX<CrmBusinessProductDO> {
    default void deleteByBusinessId(Long id) {
        delete(CrmBusinessProductDO::getBusinessId, id);
    }

    default CrmBusinessProductDO selectByBusinessId(Long id) {
        return selectOne(CrmBusinessProductDO::getBusinessId, id);
    }
}
