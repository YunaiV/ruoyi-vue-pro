package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeepayStyleChainMapper extends BaseMapperX<DeepayStyleChainDO> {

    default DeepayStyleChainDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayStyleChainDO>()
                .eq(DeepayStyleChainDO::getChainCode, chainCode));
    }

    default void updateImaKbId(String chainCode, String imaKbId) {
        update(null, new LambdaUpdateWrapper<DeepayStyleChainDO>()
                .eq(DeepayStyleChainDO::getChainCode, chainCode)
                .set(DeepayStyleChainDO::getImaKbId, imaKbId));
    }

    /**
     * 持久化 Context 决策快照（JSON），用于 AI 决策回溯审计。
     */
    default void updateContextSnapshot(String chainCode, String snapshotJson) {
        update(null, new LambdaUpdateWrapper<DeepayStyleChainDO>()
                .eq(DeepayStyleChainDO::getChainCode, chainCode)
                .set(DeepayStyleChainDO::getContextSnapshot, snapshotJson));
    }

    /**
     * 查询近 7 天销量最高的款式图片（用于 TrendAgent 内部热销兜底）。
     * 通过 deepay_metrics 关联 deepay_style_chain，按 sold_count 降序。
     */
    @Select("SELECT sc.selected_image FROM deepay_metrics m " +
            "JOIN deepay_style_chain sc ON m.chain_code = sc.chain_code " +
            "WHERE m.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "  AND m.sold_count > 0 " +
            "  AND sc.selected_image IS NOT NULL " +
            "ORDER BY m.sold_count DESC " +
            "LIMIT #{limit}")
    List<String> selectTopSellingImages(@Param("limit") int limit);

}


