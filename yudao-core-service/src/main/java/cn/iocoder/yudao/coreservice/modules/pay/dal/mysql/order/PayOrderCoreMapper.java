package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayOrderCoreMapper extends BaseMapperX<PayOrderDO> {

    default PayOrderDO selectByAppIdAndMerchantOrderId(Long appId, String merchantOrderId) {
        return selectOne(new QueryWrapper<PayOrderDO>().eq("app_id", appId)
                .eq("merchant_order_id", merchantOrderId));
    }

}
