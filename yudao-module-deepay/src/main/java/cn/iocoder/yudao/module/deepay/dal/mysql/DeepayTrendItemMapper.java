package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTrendItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 外部趋势款选品表 Mapper（Phase 9 SelectionFeed）。
 */
@Mapper
public interface DeepayTrendItemMapper extends BaseMapperX<DeepayTrendItemDO> {

    /**
     * 按品类查询热度最高的 Top N 趋势款，按 heat_score 降序。
     *
     * @param category 品类（可为 null，表示不限品类）
     * @param limit    返回条数
     * @return 趋势款列表
     */
    default List<DeepayTrendItemDO> selectTopByCategory(String category, int limit) {
        LambdaQueryWrapper<DeepayTrendItemDO> qw = new LambdaQueryWrapper<DeepayTrendItemDO>()
                .orderByDesc(DeepayTrendItemDO::getHeatScore)
                .last("LIMIT " + limit);
        if (StringUtils.hasText(category)) {
            qw.eq(DeepayTrendItemDO::getCategory, category);
        }
        return selectList(qw);
    }

    /**
     * 按来源查询趋势款（source = 1688 / tiktok / shein / brand），按热度降序。
     *
     * @param source 来源标识
     * @param limit  返回条数
     * @return 趋势款列表
     */
    default List<DeepayTrendItemDO> selectTopBySource(String source, int limit) {
        return selectList(new LambdaQueryWrapper<DeepayTrendItemDO>()
                .eq(DeepayTrendItemDO::getSource, source)
                .orderByDesc(DeepayTrendItemDO::getHeatScore)
                .last("LIMIT " + limit));
    }

}
