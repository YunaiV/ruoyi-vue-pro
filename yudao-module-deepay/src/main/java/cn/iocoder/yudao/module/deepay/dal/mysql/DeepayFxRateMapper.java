package cn.iocoder.yudao.module.deepay.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayFxRateDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * FX 汇率 Mapper。
 */
@Mapper
public interface DeepayFxRateMapper extends BaseMapperX<DeepayFxRateDO> {

    /**
     * 查询汇率（主键查询，MyBatis-Plus 自动实现）。
     * 等同于 {@code selectById(currency)}。
     */
    default DeepayFxRateDO selectByCurrency(String currency) {
        return selectById(currency);
    }

    /**
     * 存在则更新，不存在则插入（upsert）。
     * 注意：方法名不能与 BaseMapper.insertOrUpdate 冲突，改为 saveOrUpdate。
     */
    default void saveOrUpdate(DeepayFxRateDO fx) {
        DeepayFxRateDO exist = selectById(fx.getCurrency());
        if (exist == null) {
            insert(fx);
        } else {
            updateById(fx);
        }
    }
}
