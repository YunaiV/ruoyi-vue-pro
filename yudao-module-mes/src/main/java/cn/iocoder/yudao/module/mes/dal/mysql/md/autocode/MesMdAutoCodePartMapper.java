package cn.iocoder.yudao.module.mes.dal.mysql.md.autocode;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 编码规则组成 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesMdAutoCodePartMapper extends BaseMapperX<MesMdAutoCodePartDO> {

    default List<MesMdAutoCodePartDO> selectListByRuleId(Long ruleId) {
        return selectList(new LambdaQueryWrapperX<MesMdAutoCodePartDO>()
                .eq(MesMdAutoCodePartDO::getRuleId, ruleId)
                .orderByAsc(MesMdAutoCodePartDO::getSort));
    }

    default List<MesMdAutoCodePartDO> selectFixedCharPartList() {
        return selectList(MesMdAutoCodePartDO::getType, MesMdAutoCodePartTypeEnum.FIXED_CHAR.getType());
    }

    default void deleteByRuleId(Long ruleId) {
        delete(MesMdAutoCodePartDO::getRuleId, ruleId);
    }

}
