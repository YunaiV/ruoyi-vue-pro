package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDemandForecastDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeepayDemandForecastMapper extends BaseMapperX<DeepayDemandForecastDO> {

    /** 查询最新预测（按 created_at 降序） */
    default DeepayDemandForecastDO selectLatestByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayDemandForecastDO>()
                .eq(DeepayDemandForecastDO::getChainCode, chainCode)
                .orderByDesc(DeepayDemandForecastDO::getCreatedAt)
                .last("LIMIT 1"));
    }

    /** 查询同品类近 30 天平均销量（用于预测模型） */
    @Select("SELECT AVG(m.sold_count) FROM deepay_metrics m " +
            "WHERE m.category = #{category} " +
            "  AND m.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "  AND m.sold_count IS NOT NULL")
    Double selectAvgSalesByCategory(String category);

}
