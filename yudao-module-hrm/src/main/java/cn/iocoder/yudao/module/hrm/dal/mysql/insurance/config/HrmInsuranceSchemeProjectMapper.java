package cn.iocoder.yudao.module.hrm.dal.mysql.insurance.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeProjectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmInsuranceSchemeProjectMapper extends BaseMapperX<HrmInsuranceSchemeProjectDO> {

    default List<HrmInsuranceSchemeProjectDO> selectListBySchemeId(Long schemeId) {
        return selectList(new LambdaQueryWrapperX<HrmInsuranceSchemeProjectDO>()
                .eq(HrmInsuranceSchemeProjectDO::getSchemeId, schemeId)
                .orderByAsc(HrmInsuranceSchemeProjectDO::getType)
                .orderByAsc(HrmInsuranceSchemeProjectDO::getId));
    }

    default List<HrmInsuranceSchemeProjectDO> selectListBySchemeIds(Collection<Long> schemeIds) {
        return selectList(new LambdaQueryWrapperX<HrmInsuranceSchemeProjectDO>()
                .in(HrmInsuranceSchemeProjectDO::getSchemeId, schemeIds)
                .orderByAsc(HrmInsuranceSchemeProjectDO::getSchemeId)
                .orderByAsc(HrmInsuranceSchemeProjectDO::getType)
                .orderByAsc(HrmInsuranceSchemeProjectDO::getId));
    }

    default void deleteBySchemeId(Long schemeId) {
        delete(HrmInsuranceSchemeProjectDO::getSchemeId, schemeId);
    }

}
