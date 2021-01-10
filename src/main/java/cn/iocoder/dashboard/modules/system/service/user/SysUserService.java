package cn.iocoder.dashboard.modules.system.service.user;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;

/**
 * 用户 Service 接口
 *
 * @author 芋道源码
 */
public interface SysUserService {
//    /**
//     * 根据条件分页查询用户列表
//     *
//     * @param user 用户信息
//     * @return 用户信息集合信息
//     */
//    public List<SysUser> selectUserList(SysUser user);

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
//     * 根据用户ID查询用户所属角色组
//     *
//     * @param userName 用户名
//     * @return 结果
//     */
//    public String selectUserRoleGroup(String userName);
//
//    /**
//     * 根据用户ID查询用户所属岗位组
//     *
//     * @param userName 用户名
//     * @return 结果
//     */
//    public String selectUserPostGroup(String userName);
//
//    /**
//     * 校验用户名称是否唯一
//     *
//     * @param userName 用户名称
//     * @return 结果
//     */
//    public String checkUserNameUnique(String userName);
//
//    /**
//     * 校验手机号码是否唯一
//     *
//     * @param user 用户信息
//     * @return 结果
//     */
//    public String checkPhoneUnique(SysUser user);
//
//    /**
//     * 校验email是否唯一
//     *
//     * @param user 用户信息
//     * @return 结果
//     */
//    public String checkEmailUnique(SysUser user);
//
//    /**
//     * 校验用户是否允许操作
//     *
//     * @param user 用户信息
//     */
//    public void checkUserAllowed(SysUser user);
//
//    /**
//     * 新增用户信息
//     *
//     * @param user 用户信息
//     * @return 结果
//     */
//    public int insertUser(SysUser user);
//
//    /**
//     * 修改用户信息
//     *
//     * @param user 用户信息
//     * @return 结果
//     */
//    public int updateUser(SysUser user);
//
//    /**
//     * 修改用户状态
//     *
//     * @param user 用户信息
//     * @return 结果
//     */
//    public int updateUserStatus(SysUser user);
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
//     * 通过用户ID删除用户
//     *
//     * @param userId 用户ID
//     * @return 结果
//     */
//    public int deleteUserById(Long userId);
//
//    /**
//     * 批量删除用户信息
//     *
//     * @param userIds 需要删除的用户ID
//     * @return 结果
//     */
//    public int deleteUserByIds(Long[] userIds);
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
