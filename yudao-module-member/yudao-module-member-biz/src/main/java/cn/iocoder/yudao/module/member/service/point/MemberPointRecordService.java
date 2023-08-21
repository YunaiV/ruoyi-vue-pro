package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;

/**
 * 用户积分记录 Service 接口
 *
 * @author QingX
 */
public interface MemberPointRecordService {

    /**
     * 【管理员】获得积分记录分页
     *
     * @param pageReqVO 分页查询
     * @return 签到记录分页
     */
    PageResult<MemberPointRecordDO> getPointRecordPage(MemberPointRecordPageReqVO pageReqVO);

    /**
     * 【会员】获得积分记录分页
     *
     * @param userId 用户编号
     * @param pageVO 分页查询
     * @return 签到记录分页
     */
    PageResult<MemberPointRecordDO> getPointRecordPage(Long userId, PageParam pageVO);

}
