package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;

import javax.validation.Valid;

/**
 * 用户积分记录 Service 接口
 *
 * @author QingX
 */
public interface MemberPointRecordService {

    /**
     * 获得用户积分记录分页
     *
     * @param pageReqVO 分页查询
     * @return 用户积分记录分页
     */
    PageResult<MemberPointRecordDO> getRecordPage(MemberPointRecordPageReqVO pageReqVO);

}
