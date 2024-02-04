package cn.iocoder.yudao.module.erp.dal.mysql.sale;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSalesOrderItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 销售订单明细 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ErpSalesOrderItemMapper extends BaseMapperX<ErpSalesOrderItemDO> {

    default List<ErpSalesOrderItemDO> selectListById(Long id) {
        return selectList(ErpSalesOrderItemDO::getId, id);
    }

    default int deleteById(Long id) {
        return delete(ErpSalesOrderItemDO::getId, id);
    }

}