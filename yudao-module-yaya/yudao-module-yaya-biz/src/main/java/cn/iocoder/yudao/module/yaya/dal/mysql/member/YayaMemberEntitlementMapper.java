package cn.iocoder.yudao.module.yaya.dal.mysql.member;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.yaya.dal.dataobject.member.YayaMemberEntitlementDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface YayaMemberEntitlementMapper extends BaseMapperX<YayaMemberEntitlementDO> {

    default YayaMemberEntitlementDO selectActiveByMemberUserId(Long memberUserId, LocalDateTime now) {
        return selectOne(new LambdaQueryWrapperX<YayaMemberEntitlementDO>()
                .eq(YayaMemberEntitlementDO::getMemberUserId, memberUserId)
                .eq(YayaMemberEntitlementDO::getStatus, "active")
                .le(YayaMemberEntitlementDO::getStartsAt, now)
                .gt(YayaMemberEntitlementDO::getEndsAt, now)
                .orderByDesc(YayaMemberEntitlementDO::getEndsAt)
                .last("LIMIT 1"));
    }

    default YayaMemberEntitlementDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(YayaMemberEntitlementDO::getIdempotencyKey, idempotencyKey);
    }

}
