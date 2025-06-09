package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 采购入库 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseInMapper extends BaseMapperX<SrmPurchaseInDO> {





    default int updateByIdAndStatus(Long id, Integer status, SrmPurchaseInDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<SrmPurchaseInDO>().eq(SrmPurchaseInDO::getId, id).eq(SrmPurchaseInDO::getAuditStatus, status));
    }

    default SrmPurchaseInDO selectByNo(String no) {
        return selectOne(SrmPurchaseInDO::getCode, no);
    }

    //    default List<SrmPurchaseInDO> selectListByOrderId(Long orderId) {
    //        return selectList(SrmPurchaseInDO::getOrderId, orderId);
    //    }

}