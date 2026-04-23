package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignCostDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper
public interface DeepayDesignCostMapper extends BaseMapperX<DeepayDesignCostDO> {

    default DeepayDesignCostDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayDesignCostDO>()
                .eq(DeepayDesignCostDO::getChainCode, chainCode)
                .orderByDesc(DeepayDesignCostDO::getId)
                .last("limit 1"));
    }

    /** 查询同品类的历史平均成本（供 PricingStrategyAgent 定价参考）。 */
    default BigDecimal avgTotalCostByCategory(String category) {
        // 直接从 design_cost 表聚合；若无数据返回 null
        List<DeepayDesignCostDO> all = selectList(new LambdaQueryWrapper<>());
        if (all.isEmpty()) return null;
        BigDecimal sum = all.stream()
                .map(DeepayDesignCostDO::getTotalCost)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long cnt = all.stream()
                .filter(v -> v.getTotalCost() != null && v.getTotalCost().compareTo(BigDecimal.ZERO) > 0)
                .count();
        return cnt > 0 ? sum.divide(BigDecimal.valueOf(cnt), 2, RoundingMode.HALF_UP) : null;
    }
}
