package cn.iocoder.yudao.module.member.service.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserUpdateReqVO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppMemberUserUpdatePasswordReqVO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppMemberUserUpdateReqVO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.AppMemberUserUpdateMobileReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 会员用户 Service 接口
 *
 * @author 芋道源码
 */
public interface MemberUserService {

    /**
     * 通过手机查询用户
     *
     * @param mobile 手机
     * @return 用户对象
     */
    MemberUserDO getUserByMobile(String mobile);

    /**
     * 基于用户昵称，模糊匹配用户列表
     *
     * @param nickname 用户昵称，模糊匹配
     * @return 用户信息的列表
     */
    List<MemberUserDO> getUserListByNickname(String nickname);

    /**
     * 基于手机号创建用户。
     * 如果用户已经存在，则直接进行返回
     *
     * @param mobile 手机号
     * @param registerIp 注册 IP
     * @return 用户对象
     */
    MemberUserDO createUserIfAbsent(@Mobile String mobile, String registerIp);

    /**
     * 更新用户的最后登陆信息
     *
     * @param id 用户编号
     * @param loginIp 登陆 IP
     */
    void updateUserLogin(Long id, String loginIp);

    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    MemberUserDO getUser(Long id);

    /**
     * 通过用户 ID 查询用户们
     *
     * @param ids 用户 ID
     * @return 用户对象信息数组
     */
    List<MemberUserDO> getUserList(Collection<Long> ids);

    /**
     * 【会员】修改基本信息
     *
     * @param userId 用户编号
     * @param reqVO 基本信息
     */
    void updateUser(Long userId, AppMemberUserUpdateReqVO reqVO);

    /**
     * 【会员】修改手机
     *
     * @param userId 用户编号
     * @param reqVO 请求信息
     */
    void updateUserMobile(Long userId, AppMemberUserUpdateMobileReqVO reqVO);

    /**
     * 【会员】修改密码
     *
     * @param userId 用户编号
     * @param reqVO 请求信息
     */
    void updateUserPassword(Long userId, AppMemberUserUpdatePasswordReqVO reqVO);

    /**
     * 判断密码是否匹配
     *
     * @param rawPassword 未加密的密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean isPasswordMatch(String rawPassword, String encodedPassword);

    /**
     * 【管理员】更新会员用户
     *
     * @param updateReqVO 更新信息
     */
    void updateUser(@Valid MemberUserUpdateReqVO updateReqVO);

    /**
     * 【管理员】获得会员用户分页
     *
     * @param pageReqVO 分页查询
     * @return 会员用户分页
     */
    PageResult<MemberUserDO> getUserPage(MemberUserPageReqVO pageReqVO);

}
