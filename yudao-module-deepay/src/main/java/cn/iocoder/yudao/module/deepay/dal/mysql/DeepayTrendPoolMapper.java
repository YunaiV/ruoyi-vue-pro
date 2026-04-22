package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTrendPoolDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 趋势款池 Mapper — 操作 deepay_trend_pool。
 *
 * <p>核心查询：按品类 + 风格 + 客群过滤，分数降序。</p>
 *
 * <p>验收：
 * <ul>
 *   <li>✔ 内裤客户 → 只返回内裤图（category 过滤）</li>
 *   <li>✔ 男装客户 → 不出少女款（crowd 过滤）</li>
 *   <li>✔ 用户点选后分数动态变化（updateScore）</li>
 * </ul>
 * </p>
 */
@Mapper
public interface DeepayTrendPoolMapper extends BaseMapperX<DeepayTrendPoolDO> {

    /**
     * 按品类 + 风格 + 客群过滤，分数降序取 top N。
     * 三个维度均可选：传 null 则不过滤该维度（逐步放宽查询）。
     *
     * @param category 品类（可为 null）
     * @param style    风格（可为 null）
     * @param crowd    客群（可为 null）
     * @param limit    返回上限
     * @return 匹配的趋势款列表（分数从高到低）
     */
    default List<DeepayTrendPoolDO> selectTopByProfile(
            String category,
            String style,
            String crowd,
            int limit) {
        return selectList(new LambdaQueryWrapper<DeepayTrendPoolDO>()
                .eq(category != null, DeepayTrendPoolDO::getCategory, category)
                .eq(style    != null, DeepayTrendPoolDO::getStyle,    style)
                .eq(crowd    != null, DeepayTrendPoolDO::getCrowd,    crowd)
                .orderByDesc(DeepayTrendPoolDO::getScore)
                .last("LIMIT " + limit));
    }

    /**
     * 仅按品类过滤（放宽风格 + 客群约束，用于兜底）。
     */
    default List<DeepayTrendPoolDO> selectTopByCategory(String category, int limit) {
        return selectList(new LambdaQueryWrapper<DeepayTrendPoolDO>()
                .eq(category != null, DeepayTrendPoolDO::getCategory, category)
                .orderByDesc(DeepayTrendPoolDO::getScore)
                .last("LIMIT " + limit));
    }

    /**
     * 全局 top N（所有品类，最终兜底）。
     */
    default List<DeepayTrendPoolDO> selectTopAll(int limit) {
        return selectList(new LambdaQueryWrapper<DeepayTrendPoolDO>()
                .orderByDesc(DeepayTrendPoolDO::getScore)
                .last("LIMIT " + limit));
    }

    /**
     * 统计总记录数（用于判断是否需要初始化种子数据）。
     */
    default Long selectCount() {
        return selectCount(new LambdaQueryWrapper<>());
    }

    /**
     * 批量插入（种子数据 / 定时抓取）。
     */
    default void insertBatch(List<DeepayTrendPoolDO> list) {
        list.forEach(this::insert);
    }

    /**
     * 按图片 URL 精确查询（FeedbackAgent 更新评分时使用）。
     */
    default DeepayTrendPoolDO selectByImageUrl(String imageUrl) {
        return selectOne(new LambdaQueryWrapper<DeepayTrendPoolDO>()
                .eq(DeepayTrendPoolDO::getImageUrl, imageUrl));
    }

    /**
     * 根据用户反馈更新评分（delta 为正数表示选中，负数表示跳过）。
     * 使用 SQL 表达式保证原子性；同时兜底 score >= 0，不出负分。
     */
    default void updateScore(Long id, int delta) {
        update(null, new LambdaUpdateWrapper<DeepayTrendPoolDO>()
                .eq(DeepayTrendPoolDO::getId, id)
                .setSql("score = GREATEST(0, score + " + delta + ")"));
    }

}
