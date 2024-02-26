package cn.iocoder.yudao.module.crm.dal.mysql.contact;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactBusinessDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * CRM 联系人商机关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmContactBusinessMapper extends BaseMapperX<CrmContactBusinessDO> {

    default CrmContactBusinessDO selectByContactIdAndBusinessId(Long contactId, Long businessId) {
        return selectOne(CrmContactBusinessDO::getContactId, contactId,
                CrmContactBusinessDO::getBusinessId, businessId);
    }

    default void deleteByContactIdAndBusinessId(Long contactId, Collection<Long> businessIds) {
        delete(new LambdaQueryWrapper<CrmContactBusinessDO>()
                .eq(CrmContactBusinessDO::getContactId, contactId)
                .in(CrmContactBusinessDO::getBusinessId, businessIds));
    }

    default void deleteByBusinessIdAndContactId(Long businessId, List<Long> contactIds) {
        delete(new LambdaQueryWrapper<CrmContactBusinessDO>()
                .eq(CrmContactBusinessDO::getBusinessId, businessId)
                .in(CrmContactBusinessDO::getContactId, contactIds));
    }

    default List<CrmContactBusinessDO> selectListByContactId(Long contactId) {
        return selectList(CrmContactBusinessDO::getContactId, contactId);
    }

    default List<CrmContactBusinessDO> selectListByBusinessId(Long businessId) {
        return selectList(CrmContactBusinessDO::getBusinessId, businessId);
    }

}
