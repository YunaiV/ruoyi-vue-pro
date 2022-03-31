package cn.iocoder.yudao.module.system.dal.mysql.dept;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.UserPostDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface UserPostMapper extends BaseMapperX<UserPostDO> {

    default List<Long> selectIdList(Long id) {
        return selectList(new LambdaQueryWrapperX<UserPostDO>()
                .eq(UserPostDO::getUserId, id)
                .select(UserPostDO::getPostId)
        )
                .stream()
                .map(UserPostDO::getPostId)
                .collect(Collectors.toList());

    }

    default void insertList(Long userId, Collection<Long> createPostIds) {
        List<UserPostDO> list = createPostIds
                .stream()
                .map(postId -> {
                    UserPostDO entity = new UserPostDO();
                    entity.setUserId(userId);
                    entity.setPostId(postId);
                    return entity;
                })
                .collect(Collectors.toList());
        insertBatch(list);
    }

    default void deleteByUserAndPost(Long userId, Collection<Long> deletePostIds) {
        delete(new LambdaQueryWrapperX<UserPostDO>()
                .eq(UserPostDO::getUserId, userId)
                .in(UserPostDO::getPostId, deletePostIds));
    }

    default List<Long> getUserIdByPostIds(Collection<Long> postIds) {
        return selectList(new LambdaQueryWrapperX<UserPostDO>()
                .in(UserPostDO::getPostId, postIds))
                .stream()
                .map(UserPostDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }
}
