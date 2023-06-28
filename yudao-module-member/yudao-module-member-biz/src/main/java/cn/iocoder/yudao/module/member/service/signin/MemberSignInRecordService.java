package cn.iocoder.yudao.module.member.service.signin;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.*;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 用户签到积分 Service 接口
 *
 * @author 芋道源码
 */
public interface MemberSignInRecordService {



    /**
     * 删除用户签到积分
     *
     * @param id 编号
     */
    void deleteSignInRecord(Long id);

    /**
     * 获得用户签到积分
     *
     * @param id 编号
     * @return 用户签到积分
     */
    MemberSignInRecordDO getSignInRecord(Long id);

    /**
     * 获得用户签到积分列表
     *
     * @param ids 编号
     * @return 用户签到积分列表
     */
    List<MemberSignInRecordDO> getSignInRecordList(Collection<Long> ids);

    /**
     * 获得用户签到积分分页
     *
     * @param pageReqVO 分页查询
     * @return 用户签到积分分页
     */
    PageResult<MemberSignInRecordDO> getSignInRecordPage(MemberSignInRecordPageReqVO pageReqVO);

    /**
     * 获得用户签到积分列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用户签到积分列表
     */
    List<MemberSignInRecordDO> getSignInRecordList(MemberSignInRecordExportReqVO exportReqVO);

}
