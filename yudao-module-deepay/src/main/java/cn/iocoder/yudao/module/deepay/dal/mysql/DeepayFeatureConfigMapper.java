package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayFeatureConfigDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Deepay 功能菜单配置 Mapper。
 */
@Mapper
public interface DeepayFeatureConfigMapper extends BaseMapperX<DeepayFeatureConfigDO> {

    /** 查询所有启用的功能（前端 /api/features 使用）。 */
    default List<DeepayFeatureConfigDO> selectEnabled() {
        return selectList(new LambdaQueryWrapper<DeepayFeatureConfigDO>()
                .eq(DeepayFeatureConfigDO::getEnabled, 1)
                .orderByAsc(DeepayFeatureConfigDO::getMenuGroup)
                .orderByAsc(DeepayFeatureConfigDO::getSortOrder));
    }

    /** 查询所有功能（后台管理列表使用，含禁用项）。 */
    default List<DeepayFeatureConfigDO> selectAll() {
        return selectList(new LambdaQueryWrapper<DeepayFeatureConfigDO>()
                .orderByAsc(DeepayFeatureConfigDO::getMenuGroup)
                .orderByAsc(DeepayFeatureConfigDO::getSortOrder));
    }

    /** 按功能唯一键查询。 */
    default DeepayFeatureConfigDO selectByFeatureKey(String featureKey) {
        return selectOne(new LambdaQueryWrapper<DeepayFeatureConfigDO>()
                .eq(DeepayFeatureConfigDO::getFeatureKey, featureKey));
    }

    /** 批量启用/禁用（enabled: 1=启用，0=禁用）。 */
    default void batchUpdateEnabled(List<Long> ids, Integer enabled) {
        update(null, new LambdaUpdateWrapper<DeepayFeatureConfigDO>()
                .in(DeepayFeatureConfigDO::getId, ids)
                .set(DeepayFeatureConfigDO::getEnabled, enabled));
    }

    /** 更新排序。 */
    default void updateSortOrder(Long id, Integer sortOrder) {
        update(null, new LambdaUpdateWrapper<DeepayFeatureConfigDO>()
                .eq(DeepayFeatureConfigDO::getId, id)
                .set(DeepayFeatureConfigDO::getSortOrder, sortOrder));
    }
}
