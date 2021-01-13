package cn.iocoder.dashboard.modules.system.service.user;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;

import java.util.List;

/**
 * 用户 Service 接口
 *
 * @author 芋道源码
 */
public interface SysUserService {

    /**
     * 通过用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象信息
     */
    SysUserDO getUserByUserName(String username);

    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    SysUserDO getUser(Long id);

    /**
     * 获得用户分页列表
     *
     * @param reqVO 分页条件
     * @return 分页列表
     */
    PageResult<SysUserDO> pageUsers(SysUserPageReqVO reqVO);

    /**
     * 获得用户列表
     *
     * @param reqVO 列表请求
     * @return 用户列表
     */
    List<SysUserDO> listUsers(SysUserExportReqVO reqVO);

    /**
     * 创建用户
     *
     * @param reqVO 用户信息
     * @return 用户编号
     */
    Long createUser(SysUserCreateReqVO reqVO);

    /**
     * 修改用户
     *
     * @param reqVO 用户信息
     */
    void updateUser(SysUserUpdateReqVO reqVO);

    /**
     * 删除用户
     *
     * @param id 用户编号
     */
    void deleteUser(Long id);

    /**
     * 修改密码
     *
     * @param id 用户编号
     * @param password 密码
     */
    void updateUserPassword(Long id, String password);

    /**
     * 修改密码
     *
     * @param id 用户编号
     * @param status 状态
     */
    void updateUserStatus(Long id, Integer status);

//
//    /**
//     * 修改用户基本信息
//     *
//     * @param user 用户信息
//     * @return 结果
//     */
//    public int updateUserProfile(SysUser user);
//
//    /**
//     * 修改用户头像
//     *
//     * @param userName 用户名
//     * @param avatar 头像地址
//     * @return 结果
//     */
//    public boolean updateUserAvatar(String userName, String avatar);
//
//    /**
//     * 重置用户密码
//     *
//     * @param user 用户信息
//     * @return 结果
//     */
//    public int resetPwd(SysUser user);
//
//    /**
//     * 重置用户密码
//     *
//     * @param userName 用户名
//     * @param password 密码
//     * @return 结果
//     */
//    public int resetUserPwd(String userName, String password);

//
//    /**
//     * 导入用户数据
//     *
//     * @param userList 用户数据列表
//     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
//     * @param operName 操作用户
//     * @return 结果
//     */
//    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName);
}
