package cn.iocoder.yudao.module.promotion.dal.mysql.kefu;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessageListReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.kefu.KeFuMessageDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 客服消息 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface KeFuMessageMapper extends BaseMapperX<KeFuMessageDO> {

    /**
     * 获得消息列表
     * 1. 第一次查询时，不带时间，默认查询最新的十条消息
     * 2. 第二次查询时，带时间，查询历史消息
     *
     * @param reqVO 列表请求
     * @return 消息列表
     */
    default List<KeFuMessageDO> selectList(KeFuMessageListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<KeFuMessageDO>()
                .eqIfPresent(KeFuMessageDO::getConversationId, reqVO.getConversationId())
                .ltIfPresent(KeFuMessageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(KeFuMessageDO::getCreateTime)
                .last("limit 10")); // TODO @puhui999：使用 limitN 哈。然后 10 通过 reqVO 传递。
    }

    default List<KeFuMessageDO> selectListByConversationIdAndUserTypeAndReadStatus(Long conversationId, Integer userType,
                                                                                   Boolean readStatus) {
        return selectList(new LambdaQueryWrapper<KeFuMessageDO>()
                .eq(KeFuMessageDO::getConversationId, conversationId)
                .ne(KeFuMessageDO::getSenderType, userType) // 管理员：查询出未读的会员消息，会员：查询出未读的客服消息
                .eq(KeFuMessageDO::getReadStatus, readStatus));
    }

    default void updateReadStatusBatchByIds(Collection<Long> ids, KeFuMessageDO keFuMessageDO) {
        update(keFuMessageDO, new LambdaUpdateWrapper<KeFuMessageDO>()
                .in(KeFuMessageDO::getId, ids));
    }

}