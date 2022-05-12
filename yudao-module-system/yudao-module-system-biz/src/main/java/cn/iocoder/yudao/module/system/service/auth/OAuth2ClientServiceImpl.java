package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.client.OAuth2ClientCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.client.OAuth2ClientPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.client.OAuth2ClientUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.auth.OAuth2ClientConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2ClientDO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.OAuth2ClientMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.OAUTH2_CLIENT_NOT_EXISTS;

/**
 * OAuth2.0 Client Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OAuth2ClientServiceImpl implements OAuth2ClientService {

    @Resource
    private OAuth2ClientMapper oauth2ClientMapper;

    @Override
    public Long createOAuth2Client(OAuth2ClientCreateReqVO createReqVO) {
        // 插入
        OAuth2ClientDO oAuth2Client = OAuth2ClientConvert.INSTANCE.convert(createReqVO);
        oauth2ClientMapper.insert(oAuth2Client);
        // 返回
        return oAuth2Client.getId();
    }

    @Override
    public void updateOAuth2Client(OAuth2ClientUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateOAuth2ClientExists(updateReqVO.getId());
        // 更新
        OAuth2ClientDO updateObj = OAuth2ClientConvert.INSTANCE.convert(updateReqVO);
        oauth2ClientMapper.updateById(updateObj);
    }

    @Override
    public void deleteOAuth2Client(Long id) {
        // 校验存在
        this.validateOAuth2ClientExists(id);
        // 删除
        oauth2ClientMapper.deleteById(id);
    }

    private void validateOAuth2ClientExists(Long id) {
        if (oauth2ClientMapper.selectById(id) == null) {
            throw exception(OAUTH2_CLIENT_NOT_EXISTS);
        }
    }

    @Override
    public OAuth2ClientDO getOAuth2Client(Long id) {
        return oauth2ClientMapper.selectById(id);
    }

    @Override
    public PageResult<OAuth2ClientDO> getOAuth2ClientPage(OAuth2ClientPageReqVO pageReqVO) {
        return oauth2ClientMapper.selectPage(pageReqVO);
    }

    @Override
    public OAuth2ClientDO validOAuthClientFromCache(Long id) {
        return new OAuth2ClientDO().setId(id)
                .setAccessTokenValiditySeconds(60 * 30)
                .setRefreshTokenValiditySeconds(60 * 60 * 24 * 30);
    }

}
