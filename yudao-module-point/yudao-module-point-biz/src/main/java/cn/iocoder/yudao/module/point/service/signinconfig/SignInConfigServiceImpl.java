package cn.iocoder.yudao.module.point.service.signinconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinconfig.SignInConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.point.convert.signinconfig.SignInConfigConvert;
import cn.iocoder.yudao.module.point.dal.mysql.signinconfig.SignInConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;

/**
 * 积分签到规则 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class SignInConfigServiceImpl implements SignInConfigService {

    @Resource
    private SignInConfigMapper signInConfigMapper;

    @Override
    public Integer createSignInConfig(SignInConfigCreateReqVO createReqVO) {
        // 插入
        SignInConfigDO signInConfig = SignInConfigConvert.INSTANCE.convert(createReqVO);
        //判断是否重复插入签到天数
        validateSignInConfigExistsDay(signInConfig.getDay());
        signInConfigMapper.insert(signInConfig);
        // 返回
        return signInConfig.getId();
    }

    @Override
    public void updateSignInConfig(SignInConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateSignInConfigExists(updateReqVO.getId());
        //判断是否重复插入签到天数
        validateSignInConfigSameDayNotSelf(updateReqVO);
        // 判断更新的
        SignInConfigDO updateObj = SignInConfigConvert.INSTANCE.convert(updateReqVO);


        signInConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteSignInConfig(Integer id) {
        // 校验存在
        validateSignInConfigExists(id);
        // 删除
        signInConfigMapper.deleteById(id);
    }

    private void validateSignInConfigExists(Integer id) {
        if (signInConfigMapper.selectById(id) == null) {
            throw exception(SIGN_IN_CONFIG_NOT_EXISTS);
        }
    }
    //根据签到天数判断是否存在一个相同的天数
    private void validateSignInConfigExistsDay(Integer day) {
        if (signInConfigMapper.selectCount(SignInConfigDO::getDay,day)>0) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }

    //更新天数时判断是否有重复的天数，需要去除自己
    private void validateSignInConfigSameDayNotSelf(SignInConfigUpdateReqVO reqVO) {
        if (signInConfigMapper.selectSameDayNotSelf(reqVO)>0) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }


    @Override
    public SignInConfigDO getSignInConfig(Integer id) {
        return signInConfigMapper.selectById(id);
    }

    @Override
    public List<SignInConfigDO> getSignInConfigList(Collection<Integer> ids) {
        return signInConfigMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SignInConfigDO> getSignInConfigPage(SignInConfigPageReqVO pageReqVO) {
        return signInConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SignInConfigDO> getSignInConfigList(SignInConfigExportReqVO exportReqVO) {
        return signInConfigMapper.selectList(exportReqVO);
    }

}
