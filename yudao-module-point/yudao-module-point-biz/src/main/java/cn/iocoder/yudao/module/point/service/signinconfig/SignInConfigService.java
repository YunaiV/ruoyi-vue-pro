package cn.iocoder.yudao.module.point.service.signinconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinconfig.SignInConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 积分签到规则 Service 接口
 *
 * @author QingX
 */
public interface SignInConfigService {

    /**
     * 创建积分签到规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createSignInConfig(@Valid SignInConfigCreateReqVO createReqVO);

    /**
     * 更新积分签到规则
     *
     * @param updateReqVO 更新信息
     */
    void updateSignInConfig(@Valid SignInConfigUpdateReqVO updateReqVO);

    /**
     * 删除积分签到规则
     *
     * @param id 编号
     */
    void deleteSignInConfig(Integer id);

    /**
     * 获得积分签到规则
     *
     * @param id 编号
     * @return 积分签到规则
     */
    SignInConfigDO getSignInConfig(Integer id);

    /**
     * 获得积分签到规则列表
     *
     * @param ids 编号
     * @return 积分签到规则列表
     */
    List<SignInConfigDO> getSignInConfigList(Collection<Integer> ids);

    /**
     * 获得积分签到规则分页
     *
     * @param pageReqVO 分页查询
     * @return 积分签到规则分页
     */
    PageResult<SignInConfigDO> getSignInConfigPage(SignInConfigPageReqVO pageReqVO);

    /**
     * 获得积分签到规则列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 积分签到规则列表
     */
    List<SignInConfigDO> getSignInConfigList(SignInConfigExportReqVO exportReqVO);

}
