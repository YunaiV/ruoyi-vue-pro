package cn.iocoder.yudao.module.system.dal.mysql.sms;

import cn.iocoder.yudao.framework.mybatis.core.enums.SqlConstants;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsCodeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SmsCodeMapper extends BaseMapperX<SmsCodeDO> {

    /**
     * 获得手机号的最后一个手机验证码
     *
     * @param mobile 手机号
     * @param scene 发送场景，选填
     * @param code 验证码 选填
     * @return 手机验证码
     */
    default SmsCodeDO selectLastByMobile(String mobile, String code, Integer scene) {
        return selectOne(new LambdaQueryWrapperX<SmsCodeDO>()
                .eq(SmsCodeDO::getCode, mobile)
                .eqIfPresent(SmsCodeDO::getScene, scene)
                .eqIfPresent(SmsCodeDO::getCode, code)
                .orderByDesc(SmsCodeDO::getId)
                .last(SqlConstants.LIMIT1));
    }

}
