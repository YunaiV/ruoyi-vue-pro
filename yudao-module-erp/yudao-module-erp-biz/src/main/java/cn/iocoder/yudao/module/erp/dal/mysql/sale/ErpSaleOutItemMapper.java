package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 销售出库项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSaleOutItemMapper extends BaseMapperX<ErpSaleOutItemDO> {

    default List<ErpSaleOutItemDO> selectListByOutId(Long orderId) {
        return selectList(ErpSaleOutItemDO::getOutId, orderId);
    }

    default List<ErpSaleOutItemDO> selectListByOutIds(Collection<Long> orderIds) {
        return selectList(ErpSaleOutItemDO::getOutId, orderIds);
    }

    default int deleteByOutId(Long orderId) {
        return delete(ErpSaleOutItemDO::getOutId, orderId);
    }

}