package cn.iocoder.yudao.module.mp.service.user;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.user.vo.MpUserPageReqVO;
import cn.iocoder.yudao.module.mp.convert.user.MpUserConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.dal.mysql.accountfans.MpUserMapper;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 微信公众号粉丝 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class MpUserServiceImpl implements MpUserService {

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpAccountService accountService;

    @Resource
    private MpUserMapper mpUserMapper;

    @Override
    public MpUserDO getUser(Long id) {
        return mpUserMapper.selectById(id);
    }

    @Override
    public List<MpUserDO> getUserList(Collection<Long> ids) {
        return mpUserMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MpUserDO> getUserPage(MpUserPageReqVO pageReqVO) {
        return mpUserMapper.selectPage(pageReqVO);
    }

    @Override
    public MpUserDO saveUser(String appId, WxMpUser wxMpUser) {
        // 构建保存的 MpUserDO 对象
        MpAccountDO account = accountService.getAccountFromCache(appId);
        MpUserDO user = MpUserConvert.INSTANCE.convert(account, wxMpUser);

        // 根据情况，插入或更新
        MpUserDO dbUser = mpUserMapper.selectByAppIdAndOpenid(appId, wxMpUser.getOpenId());
        if (dbUser == null) {
            mpUserMapper.insert(user);
        } else {
            user.setId(dbUser.getId());
            mpUserMapper.updateById(user);
        }
        return user;
    }

    @Override
    public void updateUserUnsubscribe(String appId, String openId) {
        MpUserDO dbUser = mpUserMapper.selectByAppIdAndOpenid(appId, openId);
        if (dbUser == null) {
            log.error("[updateUserUnsubscribe][微信公众号粉丝 appId({}) openId({}) 不存在]", appId, openId);
            return;
        }
        mpUserMapper.updateById(new MpUserDO().setId(dbUser.getId()).setSubscribeStatus(CommonStatusEnum.DISABLE.getStatus())
                .setSubscribeTime(LocalDateTime.now()));
    }

}
