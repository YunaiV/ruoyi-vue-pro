package cn.iocoder.yudao.module.system.dal.mysql.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.message.NotifyMessagePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyMessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内信 Mapper
 *
 * @author xrcoder
 */
@Mapper
public interface NotifyMessageMapper extends BaseMapperX<NotifyMessageDO> {

    default PageResult<NotifyMessageDO> selectPage(NotifyMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NotifyMessageDO>()
                .likeIfPresent(NotifyMessageDO::getTitle, reqVO.getTitle())
                .eqIfPresent(NotifyMessageDO::getReadStatus, reqVO.getReadStatus())
                .betweenIfPresent(NotifyMessageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(NotifyMessageDO::getId));
    }
}
