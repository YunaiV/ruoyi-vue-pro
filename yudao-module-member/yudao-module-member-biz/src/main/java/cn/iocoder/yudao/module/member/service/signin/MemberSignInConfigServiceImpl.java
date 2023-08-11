package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInConfigCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInConfigUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInConfigMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.SIGN_IN_CONFIG_EXISTS;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.SIGN_IN_CONFIG_NOT_EXISTS;

/**
 * 积分签到规则 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberSignInConfigServiceImpl implements MemberSignInConfigService {

    @Resource
    private MemberSignInConfigMapper memberSignInConfigMapper;

    @Override
    public Integer createSignInConfig(MemberSignInConfigCreateReqVO createReqVO) {
        // 判断是否重复插入签到天数
        validateSignInConfigExistsDay(createReqVO.getDay());

        // 插入
        MemberSignInConfigDO signInConfig = MemberSignInConfigConvert.INSTANCE.convert(createReqVO);
        memberSignInConfigMapper.insert(signInConfig);
        // 返回
        return signInConfig.getId();
    }

    @Override
    public void updateSignInConfig(MemberSignInConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateSignInConfigExists(updateReqVO.getId());
        //判断是否重复插入签到天数
        validateSignInConfigSameDayNotSelf(updateReqVO);

        // 判断更新
        MemberSignInConfigDO updateObj = MemberSignInConfigConvert.INSTANCE.convert(updateReqVO);
        memberSignInConfigMapper.updateIfPresent(updateObj);
    }

    @Override
    public void deleteSignInConfig(Integer id) {
        // 校验存在
        validateSignInConfigExists(id);
        // 删除
        memberSignInConfigMapper.deleteById(id);
    }

    private void validateSignInConfigExists(Integer id) {
        if (memberSignInConfigMapper.selectById(id) == null) {
            throw exception(SIGN_IN_CONFIG_NOT_EXISTS);
        }
    }

    //根据签到天数判断是否存在一个相同的天数
    private void validateSignInConfigExistsDay(Integer day) {
        MemberSignInConfigDO configDO = memberSignInConfigMapper.selectByDay(day);
        if (configDO != null) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }

    // 更新天数时判断是否有重复的天数，需要去除自己
    private void validateSignInConfigSameDayNotSelf(MemberSignInConfigUpdateReqVO reqVO) {
        MemberSignInConfigDO configDO = memberSignInConfigMapper.selectByDay(reqVO.getDay());
        if (configDO != null && configDO.getId() != reqVO.getId()) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }

    @Override
    public MemberSignInConfigDO getSignInConfig(Integer id) {
        return memberSignInConfigMapper.selectById(id);
    }

    @Override
    public List <MemberSignInConfigDO> getSignInConfigList() {
        return memberSignInConfigMapper.getList();
    }

}
