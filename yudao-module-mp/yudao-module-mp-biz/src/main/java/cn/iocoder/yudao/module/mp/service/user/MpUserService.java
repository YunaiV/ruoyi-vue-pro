package cn.iocoder.yudao.module.mp.service.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.user.vo.MpUserPageReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import me.chanjar.weixin.mp.bean.result.WxMpUser;

import java.util.Collection;
import java.util.List;

/**
 * 微信公众号粉丝 Service 接口
 *
 * @author 芋道源码
 */
public interface MpUserService {

    /**
     * 获得微信公众号粉丝
     *
     * @param id 编号
     * @return 微信公众号粉丝
     */
    MpUserDO getUser(Long id);

    /**
     * 使用 appId + openId，获得微信公众号粉丝
     *
     * @param appId 微信公众号 appId
     * @param openId 微信公众号 openId
     * @return 微信公众号粉丝
     */
    MpUserDO getUser(String appId, String openId);

    /**
     * 获得微信公众号粉丝列表
     *
     * @param ids 编号
     * @return 微信公众号粉丝列表
     */
    List<MpUserDO> getUserList(Collection<Long> ids);

    /**
     * 获得微信公众号粉丝分页
     *
     * @param pageReqVO 分页查询
     * @return 微信公众号粉丝分页
     */
    PageResult<MpUserDO> getUserPage(MpUserPageReqVO pageReqVO);

    /**
     * 保存微信公众号粉丝
     *
     * 新增或更新，根据是否存在数据库中
     *
     * @param appId 微信公众号 appId
     * @param wxMpUser 微信公众号粉丝的信息
     * @return 微信公众号粉丝
     */
    MpUserDO saveUser(String appId, WxMpUser wxMpUser);

    /**
     * 更新微信公众号粉丝，取消关注
     *
     * @param appId 微信公众号 appId
     * @param openId 微信公众号粉丝的 openid
     */
    void updateUserUnsubscribe(String appId, String openId);

}
