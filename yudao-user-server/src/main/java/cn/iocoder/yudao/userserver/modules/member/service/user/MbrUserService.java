package cn.iocoder.yudao.userserver.modules.member.service.user;

import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserInfoRespVO;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.userserver.modules.member.controller.user.vo.MbrUserUpdateMobileReqVO;

import java.io.InputStream;

/**
 * 前台用户 Service 接口
 *
 * @author 芋道源码
 */
public interface MbrUserService {

    /**
     * 通过手机查询用户
     *
     * @param mobile 手机
     * @return 用户对象
     */
    MbrUserDO getUserByMobile(String mobile);

    /**
     * 基于手机号创建用户。
     * 如果用户已经存在，则直接进行返回
     *
     * @param mobile 手机号
     * @param registerIp 注册 IP
     * @return 用户对象
     */
    MbrUserDO createUserIfAbsent(@Mobile String mobile, String registerIp);

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
    MbrUserDO getUser(Long id);

    /**
     * 修改用户昵称
     * @param userId 用户id
     * @param nickname 用户新昵称
     */
    void updateNickname(Long userId, String nickname);

    /**
     * 修改用户头像
     * @param userId 用户id
     * @param inputStream 头像文件
     * @return 头像url
     */
    String updateAvatar(Long userId, InputStream inputStream);

    /**
     * 根据用户id，获取用户头像与昵称
     *
     * @param userId 用户id
     * @return 用户响应实体类
     */
    MbrUserInfoRespVO getUserInfo(Long userId);

    /**
     * 修改手机
     * @param userId 用户id
     * @param reqVO 请求实体
     */
    void updateMobile(Long userId, MbrUserUpdateMobileReqVO reqVO);

}
