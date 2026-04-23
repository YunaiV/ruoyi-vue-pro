package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper
public interface DeepayMetricsMapper extends BaseMapperX<DeepayMetricsDO> {

    default void markSoldByChainCode(String chainCode) {
        update(null, new LambdaUpdateWrapper<DeepayMetricsDO>()
                .eq(DeepayMetricsDO::getChainCode, chainCode)
                .set(DeepayMetricsDO::getSold, 1));
    }

    /**
     * 查询同品类历史平均成交价（供 AIDecisionAgent 定价参考）。
     * 仅统计已售记录（sold=1）以过滤未成交噪声。
     *
     * @return 平均价格；无数据时返回 null
     */
    default BigDecimal selectAvgPriceByCategory(String category) {
        List<DeepayMetricsDO> list = selectList(new LambdaQueryWrapper<DeepayMetricsDO>()
                .eq(DeepayMetricsDO::getCategory, category)
                .eq(DeepayMetricsDO::getSold, 1)
                .isNotNull(DeepayMetricsDO::getPrice));
        if (list == null || list.isEmpty()) {
            return null;
        }
        BigDecimal sum = list.stream()
                .map(DeepayMetricsDO::getPrice)
                .filter(p -> p != null && p.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long cnt = list.stream()
                .filter(m -> m.getPrice() != null && m.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .count();
        return cnt > 0 ? sum.divide(BigDecimal.valueOf(cnt), 2, RoundingMode.HALF_UP) : null;
    }

    /**
     * 查询指定链码的最新 ROI（DeepayReviewScheduler 复盘决策使用）。
     *
     * @return 最新一条指标记录的 roi；无数据时返回 null
     */
    default BigDecimal selectLatestRoiByChainCode(String chainCode) {
        DeepayMetricsDO latest = selectOne(new LambdaQueryWrapper<DeepayMetricsDO>()
                .eq(DeepayMetricsDO::getChainCode, chainCode)
                .isNotNull(DeepayMetricsDO::getRoi)
                .orderByDesc(DeepayMetricsDO::getId)
                .last("LIMIT 1"));
        return latest != null ? latest.getRoi() : null;
    }

    /**
     * 查询同品类的平均 soldCount（TrendService 趋势热度评分使用）。
     *
     * @return 平均已售件数；无数据时返回 null
     */
    default Double selectAvgSoldCountByCategory(String category) {
        List<DeepayMetricsDO> list = selectList(new LambdaQueryWrapper<DeepayMetricsDO>()
                .eq(DeepayMetricsDO::getCategory, category)
                .isNotNull(DeepayMetricsDO::getSoldCount));
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .mapToInt(m -> m.getSoldCount() != null ? m.getSoldCount() : 0)
                .average()
                .orElse(0.0);
    }

}
