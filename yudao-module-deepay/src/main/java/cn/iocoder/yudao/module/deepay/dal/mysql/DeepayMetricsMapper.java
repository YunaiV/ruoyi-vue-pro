package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.agent.TrendItem;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayMetricsDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DeepayMetricsMapper extends BaseMapperX<DeepayMetricsDO> {

    default void incrementSoldCount(Long id) {
        update(null, new LambdaUpdateWrapper<DeepayMetricsDO>()
                .setSql("sold_count = sold_count + 1")
                .eq(DeepayMetricsDO::getId, id));
    }

    /** 查询同品类历史平均销量（JudgeAgent 动态打分）。 */
    @Select("SELECT AVG(sold_count) FROM deepay_metrics WHERE category = #{category} AND sold_count IS NOT NULL")
    Double selectAvgSoldCountByCategory(String category);

    /** 查询同品类历史平均价格（AIDecisionAgent 动态定价）。 */
    @Select("SELECT AVG(price) FROM deepay_metrics WHERE category = #{category} AND price IS NOT NULL")
    BigDecimal selectAvgPriceByCategory(String category);

    /** 查询全局历史平均销量（DeepayReviewScheduler 动态阈值）。 */
    @Select("SELECT AVG(sold_count) FROM deepay_metrics WHERE sold_count IS NOT NULL")
    Double selectAvgSoldCount();

    /** 查询全局历史销量标准差（DeepayReviewScheduler 动态阈值）。 */
    @Select("SELECT STDDEV(sold_count) FROM deepay_metrics WHERE sold_count IS NOT NULL")
    Double selectStddevSoldCount();

    /** 查询链码最新 ROI（ReviewScheduler 利润决策）。 */
    @Select("SELECT roi FROM deepay_metrics WHERE chain_code = #{chainCode} AND roi IS NOT NULL ORDER BY created_at DESC LIMIT 1")
    BigDecimal selectLatestRoiByChainCode(String chainCode);

    /** 查询链码最新 profit（ReviewScheduler 展示）。 */
    @Select("SELECT profit FROM deepay_metrics WHERE chain_code = #{chainCode} AND profit IS NOT NULL ORDER BY created_at DESC LIMIT 1")
    BigDecimal selectLatestProfitByChainCode(String chainCode);

    /**
     * 查询全局 TOP-N 趋势商品（TrendAgent 备用数据源）。
     *
     * <p>JOIN deepay_product 获取 CDN 图片；deepay_metrics 提供品类、风格、销量。
     * 结果按 sold_count DESC，调用方再做品类过滤 + 风格加权排序。</p>
     *
     * @param limit 最多返回条数（建议 50）
     */
    @Select("SELECT m.chain_code, " +
            "       COALESCE(p.cdn_image_url, '') AS image_url, " +
            "       m.category, " +
            "       COALESCE(m.style, '') AS style, " +
            "       COALESCE(m.sold_count, 0) AS sold_count " +
            "FROM deepay_metrics m " +
            "LEFT JOIN deepay_product p ON p.chain_code = m.chain_code " +
            "WHERE m.sold_count IS NOT NULL " +
            "ORDER BY m.sold_count DESC " +
            "LIMIT #{limit}")
    @Results({
            @Result(property = "chainCode",  column = "chain_code"),
            @Result(property = "imageUrl",   column = "image_url"),
            @Result(property = "category",   column = "category"),
            @Result(property = "style",      column = "style"),
            @Result(property = "soldCount",  column = "sold_count")
    })
    List<TrendItem> selectTopTrend(int limit);

}
