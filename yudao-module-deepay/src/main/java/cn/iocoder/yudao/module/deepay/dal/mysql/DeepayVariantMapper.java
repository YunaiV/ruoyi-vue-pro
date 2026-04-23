package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayVariantDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 爆款变体表 Mapper（Phase 8 Hot Clone Engine）。
 */
@Mapper
public interface DeepayVariantMapper extends BaseMapperX<DeepayVariantDO> {

    /** 按父链码查询所有变体（用于计数和展示）。 */
    default List<DeepayVariantDO> selectByParentChainCode(String parentChainCode) {
        return selectList(new LambdaQueryWrapper<DeepayVariantDO>()
                .eq(DeepayVariantDO::getParentChainCode, parentChainCode)
                .orderByAsc(DeepayVariantDO::getVariantCode));
    }

    /** 按变体编码查询（去重判断）。 */
    default DeepayVariantDO selectByVariantCode(String variantCode) {
        return selectOne(new LambdaQueryWrapper<DeepayVariantDO>()
                .eq(DeepayVariantDO::getVariantCode, variantCode));
    }

    /** 统计指定爆款已生成的变体数量。 */
    default long countByParentChainCode(String parentChainCode) {
        return selectCount(new LambdaQueryWrapper<DeepayVariantDO>()
                .eq(DeepayVariantDO::getParentChainCode, parentChainCode));
    }

}
