package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayHotProductDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 爆款表 Mapper（Phase 8 Hot Clone Engine）。
 */
@Mapper
public interface DeepayHotProductMapper extends BaseMapperX<DeepayHotProductDO> {

    /** 按链码查询（用于去重判断）。 */
    default DeepayHotProductDO selectByChainCode(String chainCode) {
        return selectOne(new LambdaQueryWrapper<DeepayHotProductDO>()
                .eq(DeepayHotProductDO::getChainCode, chainCode));
    }

    /** 查询所有爆款（按评分降序，HotCloneAgent 批量处理用）。 */
    default List<DeepayHotProductDO> selectAllHot() {
        return selectList(new LambdaQueryWrapper<DeepayHotProductDO>()
                .orderByDesc(DeepayHotProductDO::getScore));
    }

    /**
     * 查询尚未生成变体的爆款（JOIN 变体表，避免重复克隆）。
     *
     * <p>SQL：
     * <pre>
     * SELECT h.*
     * FROM deepay_hot_product h
     * LEFT JOIN deepay_variant v ON h.chain_code = v.parent_chain_code
     * WHERE v.id IS NULL
     * ORDER BY h.score DESC
     * LIMIT #{limit}
     * </pre>
     * </p>
     */
    @Select("SELECT h.* " +
            "FROM deepay_hot_product h " +
            "LEFT JOIN deepay_variant v ON h.chain_code = v.parent_chain_code " +
            "WHERE v.id IS NULL " +
            "ORDER BY h.score DESC " +
            "LIMIT #{limit}")
    List<DeepayHotProductDO> selectHotWithoutVariants(int limit);

}
