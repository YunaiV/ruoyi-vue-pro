package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInConfigCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInConfigPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInConfigUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

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

    // TODO @xiaqing：这种写的逻辑，最好按照 校验 - 更新这样的顺序写；类似这里，37 要放到 34 前面；updateSignInConfig 也是一样的思路
    @Override
    public Integer createSignInConfig(MemberSignInConfigCreateReqVO createReqVO) {
        // 插入
        MemberSignInConfigDO signInConfig = MemberSignInConfigConvert.INSTANCE.convert(createReqVO);
        // 判断是否重复插入签到天数
        validateSignInConfigExistsDay(signInConfig.getDay());
        memberSignInConfigMapper.insert(signInConfig);
        // 返回
        return signInConfig.getId();
    }

    // TODO @xiaqing：这个逻辑的空行要注意；52 到 53 是没必要的空行；而 49 和 50 之间有个空行会好点，可以区分出是 校验 - 更新这样的逻辑间隔
    @Override
    public void updateSignInConfig(MemberSignInConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateSignInConfigExists(updateReqVO.getId());
        //判断是否重复插入签到天数
        validateSignInConfigSameDayNotSelf(updateReqVO);
        // 判断更新的
        MemberSignInConfigDO updateObj = MemberSignInConfigConvert.INSTANCE.convert(updateReqVO);


        memberSignInConfigMapper.updateById(updateObj);
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

    // TODO @xiaqing：这个唯一判断，也可以参考下别的模块哈；
    //根据签到天数判断是否存在一个相同的天数
    private void validateSignInConfigExistsDay(Integer day) {
        if (memberSignInConfigMapper.selectCount(MemberSignInConfigDO::getDay,day)>0) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }

    // TODO @xiaqing：参考下别的模块，判断唯一，排除自己怎么写的哈；
    // 更新天数时判断是否有重复的天数，需要去除自己
    private void validateSignInConfigSameDayNotSelf(MemberSignInConfigUpdateReqVO reqVO) {
        if (memberSignInConfigMapper.selectSameDayNotSelf(reqVO)>0) {
            throw exception(SIGN_IN_CONFIG_EXISTS);
        }
    }

    @Override
    public MemberSignInConfigDO getSignInConfig(Integer id) {
        return memberSignInConfigMapper.selectById(id);
    }

    @Override
    public PageResult<MemberSignInConfigDO> getSignInConfigPage(MemberSignInConfigPageReqVO pageReqVO) {
        return memberSignInConfigMapper.selectPage(pageReqVO);
    }

}
