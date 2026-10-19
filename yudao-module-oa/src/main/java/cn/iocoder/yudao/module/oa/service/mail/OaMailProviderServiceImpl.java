package cn.iocoder.yudao.module.oa.service.mail;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.provider.OaMailProviderSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailProviderDO;
import cn.iocoder.yudao.module.oa.dal.mysql.mail.OaMailProviderMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 企业邮箱服务配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaMailProviderServiceImpl implements OaMailProviderService {

    @Resource
    private OaMailProviderMapper mailProviderMapper;
    @Resource
    @Lazy
    private OaMailAccountService mailAccountService;

    @Override
    public Long createMailProvider(OaMailProviderSaveReqVO reqVO) {
        // 1. 转换邮箱服务配置
        OaMailProviderDO provider = BeanUtils.toBean(reqVO, OaMailProviderDO.class).setId(null);

        // 2. 新增配置
        mailProviderMapper.insert(provider);
        return provider.getId();
    }

    @Override
    public void updateMailProvider(OaMailProviderSaveReqVO reqVO) {
        // 1.1 校验配置存在
        validateMailProviderExists(reqVO.getId());
        // 1.2 转换邮箱服务配置
        OaMailProviderDO provider = BeanUtils.toBean(reqVO, OaMailProviderDO.class);

        // 2. 修改配置
        mailProviderMapper.updateById(provider);
    }

    @Override
    public void deleteMailProvider(Long id) {
        // 1.1 校验配置存在
        validateMailProviderExists(id);
        // 1.2 校验是否被账号引用
        if (mailAccountService.getMailAccountCountByProviderId(id) > 0) {
            throw exception(MAIL_PROVIDER_IN_USE);
        }

        // 2. 删除配置
        mailProviderMapper.deleteById(id);
    }

    @Override
    public OaMailProviderDO validateMailProviderExists(Long id) {
        OaMailProviderDO provider = mailProviderMapper.selectById(id);
        if (provider == null) {
            throw exception(MAIL_PROVIDER_NOT_EXISTS);
        }
        return provider;
    }

    @Override
    public List<OaMailProviderDO> getMailProviderList(Integer status) {
        return mailProviderMapper.selectListByStatus(status);
    }

}
