package cn.iocoder.yudao.module.jl.service.user;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.jl.entity.user.User;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 用户信息 Service 接口
 *
 */
public interface UserService {

    /**
     * 创建用户信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUser(@Valid UserCreateReqVO createReqVO);

    /**
     * 更新用户信息
     *
     * @param updateReqVO 更新信息
     */
    void updateUser(@Valid UserUpdateReqVO updateReqVO);

    /**
     * 删除用户信息
     *
     * @param id 编号
     */
    void deleteUser(Long id);

    /**
     * 获得用户信息
     *
     * @param id 编号
     * @return 用户信息
     */
    Optional<User> getUser(Long id);

    /**
     * 获得用户信息列表
     *
     * @param ids 编号
     * @return 用户信息列表
     */
    List<User> getUserList(Collection<Long> ids);

    /**
     * 获得用户信息分页
     *
     * @param pageReqVO 分页查询
     * @return 用户信息分页
     */
    PageResult<User> getUserPage(UserPageReqVO pageReqVO, UserPageOrder orderV0);

    /**
     * 获得用户信息列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 用户信息列表
     */
    List<User> getUserList(UserExportReqVO exportReqVO);

}
