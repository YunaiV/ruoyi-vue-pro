package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;

/**
 * 用户签到积分 Service 接口
 *
 * @author 芋道源码
 */
public interface MemberSignInRecordService {



    /**
     * 获得用户签到积分分页
     *
     * @param pageReqVO 分页查询
     * @return 用户签到积分分页
     */
    PageResult<MemberSignInRecordDO> getSignInRecordPage(MemberSignInRecordPageReqVO pageReqVO);


}
