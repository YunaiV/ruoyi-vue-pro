package cn.iocoder.yudao.module.im.dal.mysql.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IM 加群申请记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupRequestMapper extends BaseMapperX<ImGroupRequestDO> {

    default ImGroupRequestDO selectByGroupIdAndUserId(Long groupId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ImGroupRequestDO>()
                .eq(ImGroupRequestDO::getGroupId, groupId)
                .eq(ImGroupRequestDO::getUserId, userId));
    }

    default List<ImGroupRequestDO> selectListByGroupIdsAndHandleResult(Collection<Long> groupIds, Integer handleResult) {
        return selectList(new LambdaQueryWrapperX<ImGroupRequestDO>()
                .in(ImGroupRequestDO::getGroupId, groupIds)
                .eq(ImGroupRequestDO::getHandleResult, handleResult)
                .orderByDesc(ImGroupRequestDO::getId));
    }

    default List<ImGroupRequestDO> selectListByGroupId(Long groupId) {
        return selectList(new LambdaQueryWrapperX<ImGroupRequestDO>()
                .eq(ImGroupRequestDO::getGroupId, groupId)
                .orderByDesc(ImGroupRequestDO::getId));
    }

    default int updateByIdAndHandleResult(Long id, Integer expectedHandleResult, ImGroupRequestDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ImGroupRequestDO>()
                .eq(ImGroupRequestDO::getId, id).eq(ImGroupRequestDO::getHandleResult, expectedHandleResult));
    }

    default PageResult<ImGroupRequestDO> selectPage(ImGroupRequestManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImGroupRequestDO>()
                .eqIfPresent(ImGroupRequestDO::getGroupId, reqVO.getGroupId())
                .eqIfPresent(ImGroupRequestDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ImGroupRequestDO::getInviterUserId, reqVO.getInviterUserId())
                .eqIfPresent(ImGroupRequestDO::getHandleResult, reqVO.getHandleResult())
                .eqIfPresent(ImGroupRequestDO::getAddSource, reqVO.getAddSource())
                .betweenIfPresent(ImGroupRequestDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImGroupRequestDO::getId));
    }

}
