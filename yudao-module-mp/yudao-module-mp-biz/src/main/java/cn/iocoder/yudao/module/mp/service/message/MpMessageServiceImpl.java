package cn.iocoder.yudao.module.mp.service.message;

import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.mp.controller.admin.message.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.dal.mysql.fansmsg.MpMessageMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 粉丝消息表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MpMessageServiceImpl implements MpMessageService {

    @Resource
    private MpMessageMapper mpMessageMapper;

    @Override
    public PageResult<MpMessageDO> getWxFansMsgPage(MpMessagePageReqVO pageReqVO) {
        return mpMessageMapper.selectPage(pageReqVO);
    }

}
