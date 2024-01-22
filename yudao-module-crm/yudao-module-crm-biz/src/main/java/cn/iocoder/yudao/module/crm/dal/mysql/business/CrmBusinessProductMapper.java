package cn.iocoder.yudao.module.crm.dal.mysql.business;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商机产品 Mapper // TODO @lzxhqs：类注释，和作者之间要有一个空行
 *
 * @author lzxhqs
 */
@Mapper
public interface CrmBusinessProductMapper extends BaseMapperX<CrmBusinessProductDO> {

    default void deleteByBusinessId(Long id) { // TODO @lzxhqs：第一个方法，和类之间最好空一行；
        delete(CrmBusinessProductDO::getBusinessId, id);
    }

    default CrmBusinessProductDO selectByBusinessId(Long id) { // TODO @lzxhqs：id 最好改成 businessId，上面也是；这样一看更容易懂
        return selectOne(CrmBusinessProductDO::getBusinessId, id);
    }
}
