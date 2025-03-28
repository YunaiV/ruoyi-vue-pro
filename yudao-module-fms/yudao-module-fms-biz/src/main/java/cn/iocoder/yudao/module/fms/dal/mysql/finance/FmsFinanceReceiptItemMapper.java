package cn.iocoder.yudao.module.fms.dal.mysql.finance;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.FmsFinanceReceiptItemDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ERP 收款单项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsFinanceReceiptItemMapper extends BaseMapperX<FmsFinanceReceiptItemDO> {

    default List<FmsFinanceReceiptItemDO> selectListByReceiptId(Long receiptId) {
        return selectList(FmsFinanceReceiptItemDO::getReceiptId, receiptId);
    }

    default List<FmsFinanceReceiptItemDO> selectListByReceiptIds(Collection<Long> receiptIds) {
        return selectList(FmsFinanceReceiptItemDO::getReceiptId, receiptIds);
    }

    default BigDecimal selectReceiptPriceSumByBizIdAndBizType(Long bizId, Integer bizType) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<FmsFinanceReceiptItemDO>()
            .select("SUM(receipt_price) AS receiptPriceSum")
            .eq("biz_id", bizId)
            .eq("biz_type", bizType));
        // 获得数量
        if (CollUtil.isEmpty(result)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(MapUtil.getDouble(result.get(0), "receiptPriceSum", 0D));
    }

}