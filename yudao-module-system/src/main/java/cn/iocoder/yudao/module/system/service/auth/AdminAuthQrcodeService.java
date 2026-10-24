package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthQrcodeCreateRespVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthQrcodePollReqVO;
import cn.iocoder.yudao.module.system.enums.auth.AuthQrcodeStatusEnum;

/**
 * 管理后台扫码登录二维码 Service
 *
 * @author 芋道源码
 */
public interface AdminAuthQrcodeService {

    /**
     * 生成登录二维码及仅供电脑领取的凭证
     *
     * @return 二维码信息
     */
    AuthQrcodeCreateRespVO createAuthQrcode();

    /**
     * 手机确认登录二维码
     *
     * @param qrcodeId 二维码编号
     * @param userId 当前登录的用户编号
     */
    void confirmAuthQrcode(String qrcodeId, Long userId);

    /**
     * 轮询并一次性领取登录二维码对应的用户
     *
     * @param reqVO 二维码编号及领取凭证
     * @return 二维码状态及确认的用户编号，等待确认时用户编号为空
     */
    Pair<AuthQrcodeStatusEnum, Long> pollAuthQrcode(AuthQrcodePollReqVO reqVO);

}
