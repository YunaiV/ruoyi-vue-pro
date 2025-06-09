package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;

/**
 * ERP 采购退货 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SrmPurchaseReturnMapper extends BaseMapperX<SrmPurchaseReturnDO> {


    default SrmPurchaseReturnDO selectByNo(String no) {
        return selectOne(SrmPurchaseReturnDO::getCode, no);
    }

//    default List<SrmPurchaseReturnDO> selectListByOrderId(Long orderId) {
//        return selectList(SrmPurchaseReturnDO::getOrderId, orderId);
//    }

    //通过ids查询,如果ids是空，则返回空集合
    default List<SrmPurchaseReturnDO> selectListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<SrmPurchaseReturnDO>().in(SrmPurchaseReturnDO::getId, ids));
    }
}