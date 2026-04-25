package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DeepayProductMapper extends BaseMapperX<DeepayProductDO> {

    default DeepayProductDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayProductDO>()
                .eq(DeepayProductDO::getChainCode, chainCode));
    }

    default void updateStatusByChainCode(String chainCode, String status) {
        update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .eq(DeepayProductDO::getChainCode, chainCode)
                .set(DeepayProductDO::getStatus, status));
    }

    /**
     * 原子补货：将指定商品的库存加上 delta。
     * 使用 JDBC 命名参数（#{delta}）确保参数化查询，无 SQL 注入风险。
     *
     * @param id    商品主键
     * @param delta 补货数量（正数）
     */
    @Update("UPDATE deepay_product SET stock = stock + #{delta} WHERE id = #{id}")
    void addStock(@Param("id") Long id, @Param("delta") int delta);

    /** 查询所有在售商品（DeepayReviewScheduler 使用）。 */
    default List<DeepayProductDO> selectBySelling() {
        return selectList(new LambdaQueryWrapper<DeepayProductDO>()
                .eq(DeepayProductDO::getStatus, "SELLING"));
    }

    /**
     * 原子库存扣减并累加销量。
     * 使用 WHERE stock &gt; 0 保证永不出现负库存。
     *
     * @return 受影响行数；0 表示库存不足
     */
    default int incrementSoldCount(Long id) {
        return update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .setSql("sold_count = sold_count + 1, stock = stock - 1")
                .eq(DeepayProductDO::getId, id)
                .gt(DeepayProductDO::getStock, 0));
    }

    /**
     * 状态机守卫更新：只有当前状态等于 expectedStatus 时才执行更新。
     *
     * <p>合法迁移：DRAFT→SELLING, SELLING→STOPPED, SELLING→REDESIGNING</p>
     *
     * @return 受影响行数；0 表示状态不匹配（迁移被拒绝）
     */
    default int updateStatusGuarded(Long id, String newStatus, String expectedStatus) {
        return update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .eq(DeepayProductDO::getId, id)
                .eq(DeepayProductDO::getStatus, expectedStatus)
                .set(DeepayProductDO::getStatus, newStatus));
    }

    /** 无守卫的状态更新（仅 Orchestrator 初始化路径使用）。 */
    default void updateStatus(Long id, String status) {
        update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .set(DeepayProductDO::getStatus, status)
                .eq(DeepayProductDO::getId, id));
    }

    default void updatePrice(Long id, BigDecimal price) {
        update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .set(DeepayProductDO::getPrice, price)
                .eq(DeepayProductDO::getId, id));
    }

    default void updateCostPrice(Long id, BigDecimal costPrice) {
        update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .set(DeepayProductDO::getCostPrice, costPrice)
                .eq(DeepayProductDO::getId, id));
    }

    default void updateCdnImageUrl(Long id, String cdnImageUrl) {
        update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .set(DeepayProductDO::getCdnImageUrl, cdnImageUrl)
                .eq(DeepayProductDO::getId, id));
    }

    default void updateChannel(Long id, String channel) {
        update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .set(DeepayProductDO::getChannel, channel)
                .eq(DeepayProductDO::getId, id));
    }

    default void updateTitleAndDescription(Long id, String title, String description) {
        update(null, new LambdaUpdateWrapper<DeepayProductDO>()
                .set(DeepayProductDO::getTitle, title)
                .set(DeepayProductDO::getDescription, description)
                .eq(DeepayProductDO::getId, id));
    }

    /**
     * 查询指定品类的热销商品（TrendAgent 使用）。
     *
     * <p>SQL：
     * <pre>
     * SELECT p.*
     * FROM deepay_product p
     * JOIN deepay_metrics m ON p.chain_code = m.chain_code
     * WHERE p.category = #{category}
     * ORDER BY m.sold_count DESC
     * LIMIT 10
     * </pre>
     * </p>
     *
     * <p>验收：
     * <ul>
     *   <li>内裤客户 → 只返回内裤商品</li>
     *   <li>外套客户 → 只返回外套商品</li>
     *   <li>无数据 → 返回空列表（由 TrendAgent 触发 fallback）</li>
     * </ul>
     * </p>
     */
    @Select("SELECT p.* " +
            "FROM deepay_product p " +
            "JOIN deepay_metrics m ON p.chain_code = m.chain_code " +
            "WHERE p.category = #{category} " +
            "ORDER BY m.sold_count DESC " +
            "LIMIT 10")
    List<DeepayProductDO> selectHotByCategory(String category);

}
