package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthQrcodeCreateRespVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthQrcodePollReqVO;
import cn.iocoder.yudao.module.system.dal.redis.auth.AdminAuthQrcodeRedisDAO;
import cn.iocoder.yudao.module.system.enums.auth.AuthQrcodeStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 管理后台扫码登录二维码 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class AdminAuthQrcodeServiceImpl implements AdminAuthQrcodeService {

    /**
     * 手机端识别登录二维码的前缀，不包含电脑领取凭证
     */
    private static final String QRCODE_PREFIX = "YuDaoLogin:";

    @Resource
    private AdminAuthQrcodeRedisDAO qrcodeRedisDAO;

    @Override
    public AuthQrcodeCreateRespVO createAuthQrcode() {
        String qrcodeId = IdUtil.simpleUUID();
        String secret = IdUtil.simpleUUID();
        qrcodeRedisDAO.create(qrcodeId, DigestUtil.sha256Hex(secret));
        return new AuthQrcodeCreateRespVO().setQrcodeId(qrcodeId).setSecret(secret)
                .setQrContent(QRCODE_PREFIX + qrcodeId).setExpiresIn((int) AdminAuthQrcodeRedisDAO.EXPIRE_TIME.toSeconds());
    }

    @Override
    public void confirmAuthQrcode(String qrcodeId, Long userId) {
        qrcodeRedisDAO.confirm(qrcodeId, userId);
    }

    @Override
    public Pair<AuthQrcodeStatusEnum, Long> pollAuthQrcode(AuthQrcodePollReqVO reqVO) {
        return qrcodeRedisDAO.poll(reqVO.getQrcodeId(), DigestUtil.sha256Hex(reqVO.getSecret()));
    }

}
