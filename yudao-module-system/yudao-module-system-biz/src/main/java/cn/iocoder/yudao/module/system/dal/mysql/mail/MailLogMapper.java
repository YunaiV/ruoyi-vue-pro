package cn.iocoder.yudao.module.system.dal.mysql.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.log.MailLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface MailLogMapper extends BaseMapperX<MailLogDO> {

    default PageResult<MailLogDO> selectPage(MailLogPageReqVO pageVO){
        return selectPage(pageVO , new QueryWrapperX<MailLogDO>()
                .eqIfPresent("from", pageVO.getFrom())
                .eqIfPresent("templeCode", pageVO.getTemplateCode())
                .likeIfPresent("title" , pageVO.getTitle())
                .likeIfPresent("content" , pageVO.getContent())
                .eqIfPresent("to", pageVO.getTo())
                .eqIfPresent("sendTime" , pageVO.getSendTime())
                .eqIfPresent("sendStatus" , pageVO.getSendStatus())
                .eqIfPresent("sendResult" , pageVO.getSendResult())
                .orderByDesc("sendTime")
        );
    };

    default List<MailLogDO> selectList(MailLogExportReqVO exportReqVO){
        return selectList(new QueryWrapperX<MailLogDO>()
                .eqIfPresent("from", exportReqVO.getFrom())
                .eqIfPresent("templeCode", exportReqVO.getTemplateCode())
                .likeIfPresent("title" , exportReqVO.getTitle())
                .likeIfPresent("content" , exportReqVO.getContent())
                .eqIfPresent("to", exportReqVO.getTo())
                .eqIfPresent("sendTime" , exportReqVO.getSendTime())
                .eqIfPresent("sendStatus" , exportReqVO.getSendStatus())
                .eqIfPresent("sendResult" , exportReqVO.getSendResult())
                .orderByDesc("sendTime")
        );
    };
}
