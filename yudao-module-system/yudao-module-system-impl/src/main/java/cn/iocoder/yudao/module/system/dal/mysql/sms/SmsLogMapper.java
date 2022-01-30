package cn.iocoder.yudao.module.system.dal.mysql.sms;

import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SmsLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SmsLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SmsLogMapper extends BaseMapperX<SysSmsLogDO> {

    default PageResult<SysSmsLogDO> selectPage(SmsLogPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysSmsLogDO>()
                .eqIfPresent("channel_id", reqVO.getChannelId())
                .eqIfPresent("template_id", reqVO.getTemplateId())
                .likeIfPresent("mobile", reqVO.getMobile())
                .eqIfPresent("send_status", reqVO.getSendStatus())
                .betweenIfPresent("send_time", reqVO.getBeginSendTime(), reqVO.getEndSendTime())
                .eqIfPresent("receive_status", reqVO.getReceiveStatus())
                .betweenIfPresent("receive_time", reqVO.getBeginReceiveTime(), reqVO.getEndReceiveTime())
                .orderByDesc("id"));
    }

    default List<SysSmsLogDO> selectList(SmsLogExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysSmsLogDO>()
                .eqIfPresent("channel_id", reqVO.getChannelId())
                .eqIfPresent("template_id", reqVO.getTemplateId())
                .likeIfPresent("mobile", reqVO.getMobile())
                .eqIfPresent("send_status", reqVO.getSendStatus())
                .betweenIfPresent("send_time", reqVO.getBeginSendTime(), reqVO.getEndSendTime())
                .eqIfPresent("receive_status", reqVO.getReceiveStatus())
                .betweenIfPresent("receive_time", reqVO.getBeginReceiveTime(), reqVO.getEndReceiveTime())
                .orderByDesc("id"));
    }

}
