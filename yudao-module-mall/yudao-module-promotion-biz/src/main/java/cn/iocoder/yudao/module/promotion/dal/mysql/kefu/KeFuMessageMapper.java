package cn.iocoder.yudao.module.promotion.dal.mysql.kefu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
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

    default PageResult<KeFuMessageDO> selectPage(KeFuMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<KeFuMessageDO>()
                .eqIfPresent(KeFuMessageDO::getConversationId, reqVO.getConversationId())
                .orderByDesc(KeFuMessageDO::getId));
    }

    default List<KeFuMessageDO> selectListByConversationIdAndReceiverIdAndReadStatus(Long conversationId, Long receiverId, Boolean readStatus) {
        return selectList(new LambdaQueryWrapper<KeFuMessageDO>()
                .eq(KeFuMessageDO::getConversationId, conversationId)
                .eq(KeFuMessageDO::getReceiverId, receiverId)
                .eq(KeFuMessageDO::getReadStatus, readStatus));
    }

    default void updateReadStstusBatchByIds(Collection<Long> ids, Boolean readStatus) {
        update(new LambdaUpdateWrapper<KeFuMessageDO>()
                .in(KeFuMessageDO::getId, ids)
                .set(KeFuMessageDO::getReadStatus, readStatus));
    }

}