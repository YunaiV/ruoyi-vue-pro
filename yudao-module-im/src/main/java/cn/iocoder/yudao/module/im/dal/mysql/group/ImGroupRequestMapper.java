package cn.iocoder.yudao.module.im.dal.mysql.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.ImGroupRequestManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupRequestDO;
import cn.iocoder.yudao.module.im.enums.group.ImGroupRequestHandleResultEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 加群申请记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupRequestMapper extends BaseMapperX<ImGroupRequestDO> {

    /**
     * 取最新一条未处理申请；inviterUserId 为 null 表示主动申请
     * <p>
     * 主动申请、不同邀请人发起的邀请申请彼此互不覆盖，复用键含 inviterUserId
     */
    // TODO @AI：一般合理的，是不是可以覆盖？？？？
    default ImGroupRequestDO selectLatestPendingByGroupIdAndUserIdAndInviter(
            Long groupId, Long userId, Long inviterUserId) {
        LambdaQueryWrapperX<ImGroupRequestDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ImGroupRequestDO::getGroupId, groupId)
                .eq(ImGroupRequestDO::getUserId, userId)
                .eq(ImGroupRequestDO::getHandleResult, ImGroupRequestHandleResultEnum.UNHANDLED.getResult());
        if (inviterUserId == null) {
            wrapper.isNull(ImGroupRequestDO::getInviterUserId);
        } else {
            wrapper.eq(ImGroupRequestDO::getInviterUserId, inviterUserId);
        }
        return selectOne(wrapper.orderByDesc(ImGroupRequestDO::getId).last("LIMIT 1"));
    }

    /**
     * 拉取「我相关」的加群申请列表（含我主动申请、我被邀请待审）；游标分页
     */
    // TODO @AI：selectListByMy；保持风格一致；
    default List<ImGroupRequestDO> selectMyList(Long userId, Long lastRequestId, int limit) {
        return selectList(new LambdaQueryWrapperX<ImGroupRequestDO>()
                .eq(ImGroupRequestDO::getUserId, userId)
                .ltIfPresent(ImGroupRequestDO::getId, lastRequestId)
                .orderByDesc(ImGroupRequestDO::getId)
                .last("LIMIT " + limit));
    }

    // TODO @AI：类似下面的注释，减少 mapper 的业务型；
    default List<ImGroupRequestDO> selectPendingListByGroupId(Long groupId, Long lastRequestId, int limit) {
        return selectList(new LambdaQueryWrapperX<ImGroupRequestDO>()
                .eq(ImGroupRequestDO::getGroupId, groupId)
                .eq(ImGroupRequestDO::getHandleResult, ImGroupRequestHandleResultEnum.UNHANDLED.getResult())
                .ltIfPresent(ImGroupRequestDO::getId, lastRequestId)
                .orderByDesc(ImGroupRequestDO::getId)
                .last("LIMIT " + limit));
    }

    // TODO @AI：应该 bygroupidandhandleResult，减少不必要的条件
    default Long selectPendingCountByGroupId(Long groupId) {
        return selectCount(new LambdaQueryWrapperX<ImGroupRequestDO>()
                .eq(ImGroupRequestDO::getGroupId, groupId)
                .eq(ImGroupRequestDO::getHandleResult, ImGroupRequestHandleResultEnum.UNHANDLED.getResult()));
    }

    default int updateByIdAndHandleResult(Long id, Integer handleResult, ImGroupRequestDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ImGroupRequestDO>()
                .eq(ImGroupRequestDO::getId, id).eq(ImGroupRequestDO::getHandleResult, handleResult));
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
