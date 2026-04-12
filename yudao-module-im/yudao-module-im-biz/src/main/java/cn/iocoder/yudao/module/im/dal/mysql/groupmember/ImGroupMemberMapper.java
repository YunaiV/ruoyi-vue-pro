package cn.iocoder.yudao.module.im.dal.mysql.groupmember;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.groupmember.vo.ImGroupMemberPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 群成员 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMemberMapper extends BaseMapperX<ImGroupMemberDO> {

    default PageResult<ImGroupMemberDO> selectPage(ImGroupMemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eqIfPresent(ImGroupMemberDO::getGroupId, reqVO.getGroupId())
                .eqIfPresent(ImGroupMemberDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ImGroupMemberDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ImGroupMemberDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImGroupMemberDO::getId));
    }

    default List<ImGroupMemberDO> selectListByGroupId(Long groupId) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>().eq(ImGroupMemberDO::getGroupId, groupId));
    }

    default ImGroupMemberDO selectByGroupIdAndUserId(Long groupId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ImGroupMemberDO>()
                .eq(ImGroupMemberDO::getGroupId, groupId)
                .eq(ImGroupMemberDO::getUserId, userId));
    }

    default List<ImGroupMemberDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<ImGroupMemberDO>().eq(ImGroupMemberDO::getUserId, userId));
    }
}