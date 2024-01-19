package cn.iocoder.yudao.module.member.service.user;

import cn.iocoder.yudao.framework.common.enums.TerminalEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.user.vo.MemberUserUpdateReqVO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.*;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;

import jakarta.validation.Valid;
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
     * @param mobile     手机号
     * @param registerIp 注册 IP
     * @param terminal   终端 {@link TerminalEnum}
     * @return 用户对象
     */
    MemberUserDO createUserIfAbsent(@Mobile String mobile, String registerIp, Integer terminal);

    /**
     * 创建用户
     * 目的：三方登录时，如果未绑定用户时，自动创建对应用户
     *
     * @param nickname   昵称
     * @param avtar      头像
     * @param registerIp 注册 IP
     * @param terminal   终端 {@link TerminalEnum}
     * @return 用户对象
     */
    MemberUserDO createUser(String nickname, String avtar, String registerIp, Integer terminal);

    /**
     * 更新用户的最后登陆信息
     *
     * @param id      用户编号
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
     * @param reqVO  基本信息
     */
    void updateUser(Long userId, AppMemberUserUpdateReqVO reqVO);

    /**
     * 【会员】修改手机，基于手机验证码
     *
     * @param userId 用户编号
     * @param reqVO  请求信息
     */
    void updateUserMobile(Long userId, AppMemberUserUpdateMobileReqVO reqVO);

    /**
     * 【会员】修改手机，基于微信小程序的授权码
     *
     * @param userId 用户编号
     * @param reqVO 请求信息
     */
    void updateUserMobileByWeixin(Long userId, AppMemberUserUpdateMobileByWeixinReqVO reqVO);

    /**
     * 【会员】修改密码
     *
     * @param userId 用户编号
     * @param reqVO  请求信息
     */
    void updateUserPassword(Long userId, AppMemberUserUpdatePasswordReqVO reqVO);

    /**
     * 【会员】忘记密码
     *
     * @param reqVO 请求信息
     */
    void resetUserPassword(AppMemberUserResetPasswordReqVO reqVO);

    /**
     * 判断密码是否匹配
     *
     * @param rawPassword     未加密的密码
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

    /**
     * 更新用户的等级和经验
     *
     * @param id         用户编号
     * @param levelId    用户等级
     * @param experience 用户经验
     */
    void updateUserLevel(Long id, Long levelId, Integer experience);

    /**
     * 获得指定用户分组下的用户数量
     *
     * @param groupId 用户分组编号
     * @return 用户数量
     */
    Long getUserCountByGroupId(Long groupId);

    /**
     * 获得指定用户等级下的用户数量
     *
     * @param levelId 用户等级编号
     * @return 用户数量
     */
    Long getUserCountByLevelId(Long levelId);

    /**
     * 获得指定会员标签下的用户数量
     *
     * @param tagId 用户标签编号
     * @return 用户数量
     */
    Long getUserCountByTagId(Long tagId);

    /**
     * 更新用户的积分
     *
     * @param userId 用户编号
     * @param point  积分数量
     * @return 更新结果
     */
    boolean updateUserPoint(Long userId, Integer point);

}
