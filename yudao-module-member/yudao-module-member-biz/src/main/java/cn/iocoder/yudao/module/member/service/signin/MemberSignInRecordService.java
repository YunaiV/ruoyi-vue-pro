package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.record.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.signin.vo.AppMemberSignInRecordRespVO;
import cn.iocoder.yudao.module.member.controller.app.signin.vo.AppMemberSignInSummaryRespVO;
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


    MemberSignInRecordDO create(Long userId);

    /**
     *
     *功能描述: 根据用户id获取个人签到信息
     * @param userId
     * @return
     * @author xiaqing
     * @date 2023-09-15 14:21:01
     */
    AppMemberSignInSummaryRespVO getUserSummary(Long userId);


}
