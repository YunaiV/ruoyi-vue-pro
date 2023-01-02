package cn.iocoder.yudao.module.mp.service.message;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.message.vo.MpMessagePageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;

/**
 * 粉丝消息表 Service 接口
 *
 * @author 芋道源码
 */
public interface MpMessageService {

    /**
     * 获得粉丝消息表 分页
     *
     * @param pageReqVO 分页查询
     * @return 粉丝消息表 分页
     */
    PageResult<MpMessageDO> getWxFansMsgPage(MpMessagePageReqVO pageReqVO);

}
