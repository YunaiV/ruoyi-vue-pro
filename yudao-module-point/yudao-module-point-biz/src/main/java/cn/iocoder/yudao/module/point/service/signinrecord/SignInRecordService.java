package cn.iocoder.yudao.module.point.service.signinrecord;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinrecord.SignInRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 用户签到积分 Service 接口
 *
 * @author 芋道源码
 */
public interface SignInRecordService {

    /**
     * 创建用户签到积分
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSignInRecord(@Valid SignInRecordCreateReqVO createReqVO);

    /**
     * 更新用户签到积分
     *
     * @param updateReqVO 更新信息
     */
    void updateSignInRecord(@Valid SignInRecordUpdateReqVO updateReqVO);

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
    SignInRecordDO getSignInRecord(Long id);

    /**
     * 获得用户签到积分列表
     *
     * @param ids 编号
     * @return 用户签到积分列表
     */
    List<SignInRecordDO> getSignInRecordList(Collection<Long> ids);

    /**
     * 获得用户签到积分分页
     *
     * @param pageReqVO 分页查询
     * @return 用户签到积分分页
     */
    PageResult<SignInRecordDO> getSignInRecordPage(SignInRecordPageReqVO pageReqVO);

    /**
     * 获得用户签到积分列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用户签到积分列表
     */
    List<SignInRecordDO> getSignInRecordList(SignInRecordExportReqVO exportReqVO);

}
