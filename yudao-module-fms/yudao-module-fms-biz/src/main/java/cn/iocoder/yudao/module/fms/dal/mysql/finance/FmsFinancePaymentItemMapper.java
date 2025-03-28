package cn.iocoder.yudao.module.fms.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinancePaymentItemDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 付款单项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsFinancePaymentItemMapper extends BaseMapperX<FmsFinancePaymentItemDO> {

    default List<FmsFinancePaymentItemDO> selectListByPaymentId(Long paymentId) {
        return selectList(FmsFinancePaymentItemDO::getPaymentId, paymentId);
    }

    default List<FmsFinancePaymentItemDO> selectListByPaymentIds(Collection<Long> paymentIds) {
        return selectList(FmsFinancePaymentItemDO::getPaymentId, paymentIds);
    }

    default BigDecimal selectPaymentPriceSumByBizIdAndBizType(Long bizId, Integer bizType) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<FmsFinancePaymentItemDO>()
            .select("SUM(payment_price) AS paymentPriceSum")
            .eq("biz_id", bizId)
            .eq("biz_type", bizType));
        // 获得数量
        if (CollUtil.isEmpty(result)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(MapUtil.getDouble(result.get(0), "paymentPriceSum", 0D));
    }

}