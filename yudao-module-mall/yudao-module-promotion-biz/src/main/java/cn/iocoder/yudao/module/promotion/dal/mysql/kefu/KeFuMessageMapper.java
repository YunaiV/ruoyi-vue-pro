package cn.iocoder.yudao.module.promotion.dal.mysql.kefu;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.controller.admin.kefu.vo.message.KeFuMessagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.kefu.vo.message.AppKeFuMessagePageReqVO;
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
                .orderByDesc(KeFuMessageDO::getCreateTime));
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

    default PageResult<KeFuMessageDO> selectPage(AppKeFuMessagePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<KeFuMessageDO>()
                .eqIfPresent(KeFuMessageDO::getConversationId, pageReqVO.getConversationId())
                .orderByDesc(KeFuMessageDO::getCreateTime));
    }

}