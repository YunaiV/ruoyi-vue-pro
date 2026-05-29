package cn.iocoder.yudao.module.yaya.dal.mysql.practice;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.yaya.dal.dataobject.practice.YayaFavoriteDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface YayaFavoriteMapper extends BaseMapperX<YayaFavoriteDO> {

    default YayaFavoriteDO selectByMemberAndTopic(Long memberUserId, Long topicId) {
        return selectOne(YayaFavoriteDO::getMemberUserId, memberUserId, YayaFavoriteDO::getTopicId, topicId);
    }

    default List<YayaFavoriteDO> selectListByMemberAndTopicIds(Long memberUserId, Collection<Long> topicIds) {
        return selectList(new LambdaQueryWrapperX<YayaFavoriteDO>()
                .eq(YayaFavoriteDO::getMemberUserId, memberUserId)
                .in(YayaFavoriteDO::getTopicId, topicIds));
    }

    @Delete("DELETE FROM yaya_favorite WHERE member_user_id = #{memberUserId} AND topic_id = #{topicId}")
    int deleteByMemberAndTopic(@Param("memberUserId") Long memberUserId, @Param("topicId") Long topicId);

}
