package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.module.member.controller.admin.signin.vo.config.MemberSignInConfigCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.config.MemberSignInConfigUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 积分签到规则 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberSignInConfigServiceImpl implements MemberSignInConfigService {

    @Resource
    private MemberSignInConfigMapper signInConfigMapper;

    @Override
    public Long createSignInConfig(MemberSignInConfigCreateReqVO createReqVO) {
        // 判断是否重复插入签到天数
        validateSignInConfigDayDuplicate(createReqVO.getDay(), null);

        // 插入
        MemberSignInConfigDO signInConfig = MemberSignInConfigConvert.INSTANCE.convert(createReqVO);
        signInConfigMapper.insert(signInConfig);
        // 返回
        return signInConfig.getId();
    }

    @Override
    public void updateSignInConfig(MemberSignInConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateSignInConfigExists(updateReqVO.getId());
        // 判断是否重复插入签到天数
        validateSignInConfigDayDuplicate(updateReqVO.getDay(), updateReqVO.getId());

        // 判断更新
        MemberSignInConfigDO updateObj = MemberSignInConfigConvert.INSTANCE.convert(updateReqVO);
        signInConfigMapper.updateIfPresent(updateObj);
    }

    @Override
    public void deleteSignInConfig(Long id) {
        // 校验存在
        validateSignInConfigExists(id);
        // 删除
        signInConfigMapper.deleteById(id);
    }

    private void validateSignInConfigExists(Long id) {
        if (signInConfigMapper.selectById(id) == null) {
            throw exception(SIGN_IN_CONFIG_NOT_EXISTS);
        }
    }

    /**
     * 校验 day 是否重复
     *
     * @param day 天
     * @param id 编号，只有更新的时候会传递
     */
    private void validateSignInConfigDayDuplicate(Integer day, Long id) {
        MemberSignInConfigDO configDO = signInConfigMapper.selectByDay(day);
        // 1. 新增时，configDO 非空，则说明重复
        if (id == null && configDO != null) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
        // 2. 更新时，如果 configDO 非空，且 id 不相等，则说明重复
        if (id != null && configDO != null && !configDO.getId().equals(id)) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }

    @Override
    public MemberSignInConfigDO getSignInConfig(Long id) {
        return signInConfigMapper.selectById(id);
    }

    @Override
    public List <MemberSignInConfigDO> getSignInConfigList() {
        return signInConfigMapper.getList();
    }

}
