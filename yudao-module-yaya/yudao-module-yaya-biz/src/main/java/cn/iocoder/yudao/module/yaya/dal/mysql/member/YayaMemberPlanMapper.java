package cn.iocoder.yudao.module.yaya.dal.mysql.member;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberPlanDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YayaMemberPlanMapper extends BaseMapperX<YayaMemberPlanDO> {

    default YayaMemberPlanDO selectByPlanKey(String planKey) {
        return selectOne(YayaMemberPlanDO::getPlanKey, planKey);
    }

    default YayaMemberPlanDO selectActiveByPlanKey(String planKey) {
        return selectOne(new LambdaQueryWrapperX<YayaMemberPlanDO>()
                .eq(YayaMemberPlanDO::getPlanKey, planKey)
                .eq(YayaMemberPlanDO::getActive, 1));
    }

    default List<YayaMemberPlanDO> selectListOrderByPrice() {
        return selectList(new LambdaQueryWrapperX<YayaMemberPlanDO>()
                .orderByAsc(YayaMemberPlanDO::getPriceCent)
                .orderByAsc(YayaMemberPlanDO::getDurationDays));
    }

}
