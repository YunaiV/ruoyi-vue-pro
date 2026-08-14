package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.accountuser.FmsAccountUserUpdateReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountUserDO;

import java.util.List;

/**
 * FMS 账套用户 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsAccountUserService {

    /**
     * 创建账套主管关系
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     */
    void createAccountOwner(Long accountSetId, Long userId);

    /**
     * 获得当前用户的账套关系列表
     *
     * @param userId 用户编号
     * @return 账套关系列表
     */
    List<FmsAccountUserDO> getAccountUserList(Long userId);

    /**
     * 获得账套用户关系
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     * @return 账套用户关系
     */
    FmsAccountUserDO getAccountUser(Long accountSetId, Long userId);

    /**
     * 设置当前用户的默认账套
     *
     * @param accountSetId 账套编号
     * @param userId 用户编号
     */
    void updateAccountSetDefaultStatus(Long accountSetId, Long userId);

    /**
     * 获得账套成员列表
     *
     * @param accountSetId 账套编号
     * @param userId 当前用户编号
     * @return 账套成员列表
     */
    List<FmsAccountUserDO> getAccountUserList(Long accountSetId, Long userId);

    /**
     * 更新账套成员列表
     *
     * @param updateReqVO 更新信息
     * @param userId 当前用户编号
     */
    void updateAccountUserList(FmsAccountUserUpdateReqVO updateReqVO, Long userId);

}
