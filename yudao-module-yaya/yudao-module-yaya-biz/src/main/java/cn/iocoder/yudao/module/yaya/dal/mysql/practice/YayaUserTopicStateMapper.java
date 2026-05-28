package cn.iocoder.yudao.module.yaya.dal.mysql.practice;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaUserTopicStateDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface YayaUserTopicStateMapper extends BaseMapperX<YayaUserTopicStateDO> {

    default YayaUserTopicStateDO selectByMemberAndTopic(Long memberUserId, Long topicId) {
        return selectOne(YayaUserTopicStateDO::getMemberUserId, memberUserId,
                YayaUserTopicStateDO::getTopicId, topicId);
    }

    default List<YayaUserTopicStateDO> selectListByMemberAndTopicIds(Long memberUserId, Collection<Long> topicIds) {
        return selectList(new LambdaQueryWrapperX<YayaUserTopicStateDO>()
                .eq(YayaUserTopicStateDO::getMemberUserId, memberUserId)
                .in(YayaUserTopicStateDO::getTopicId, topicIds));
    }

    default int incrementByMemberAndTopic(Long memberUserId, Long topicId, Long attemptId,
                                          LocalDateTime lastPracticedAt, Map<String, Object> metadata) {
        YayaUserTopicStateDO update = new YayaUserTopicStateDO();
        update.setPracticed(true);
        update.setLastAttemptId(attemptId);
        update.setLastPracticedAt(lastPracticedAt);
        update.setMetadata(metadata);
        return update(update, new LambdaUpdateWrapper<YayaUserTopicStateDO>()
                .eq(YayaUserTopicStateDO::getMemberUserId, memberUserId)
                .eq(YayaUserTopicStateDO::getTopicId, topicId)
                .setSql("attempt_count = attempt_count + 1"));
    }

}
