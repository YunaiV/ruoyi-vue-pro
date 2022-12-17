package cn.iocoder.yudao.module.system.dal.mysql.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.log.NotifyLogPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 站内信 Mapper
 *
 * @author xrcoder
 */
@Mapper
public interface NotifyMessageMapper extends BaseMapperX<NotifyMessageDO> {

    default PageResult<NotifyMessageDO> selectPage(NotifyMessagePageReqVO reqVO, Long userId, Integer userType) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NotifyMessageDO>()
                .likeIfPresent(NotifyMessageDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NotifyMessageDO::getReadStatus, reqVO.getReadStatus())
                .betweenIfPresent(NotifyMessageDO::getCreateTime, reqVO.getCreateTime())
                .eq(NotifyMessageDO::getUserId, userId)
                .eq(NotifyMessageDO::getUserType, userType)
                .orderByDesc(NotifyMessageDO::getId));
    }

    default PageResult<NotifyMessageDO> selectSendPage(NotifyLogPageReqVO reqVO, Long userId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NotifyMessageDO>()
                .likeIfPresent(NotifyMessageDO::getTitle, reqVO.getTitle())
                .betweenIfPresent(NotifyMessageDO::getSendTime, reqVO.getSendTime())
                .eqIfPresent(NotifyMessageDO::getTemplateCode, reqVO.getTemplateCode())
                .eq(NotifyMessageDO::getSendUserId, userId)
                .orderByDesc(NotifyMessageDO::getId));
    }

    default List<NotifyMessageDO> selectList(NotifyMessagePageReqVO reqVO, Integer size,  Long userId, Integer userType) {
        return selectList(new LambdaQueryWrapperX<NotifyMessageDO>()
                .likeIfPresent(NotifyMessageDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NotifyMessageDO::getReadStatus, reqVO.getReadStatus())
                .betweenIfPresent(NotifyMessageDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(NotifyMessageDO::getUserId, userId)
                .eqIfPresent(NotifyMessageDO::getUserType, userType)
                .orderByDesc(NotifyMessageDO::getId)
                .last("limit " + size));
    }

    default Long selectUnreadCountByUserIdAndUserType(Long userId, Integer userType) {
        return selectCount(new LambdaQueryWrapperX<NotifyMessageDO>()
                .eq(NotifyMessageDO::getReadStatus, false)
                .eq(NotifyMessageDO::getUserId, userId)
                .eq(NotifyMessageDO::getUserType, userType));
    }

    default List<NotifyMessageDO> selectUnreadListByUserIdAndUserType(Long userId, Integer userType) {
        return selectList(new LambdaQueryWrapperX<NotifyMessageDO>()
                .eq(NotifyMessageDO::getReadStatus, false)
                .eq(NotifyMessageDO::getUserId, userId)
                .eq(NotifyMessageDO::getUserType, userType));
    }

}
