package cn.iocoder.yudao.userserver.modules.system.dal.mysql.sms;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.userserver.modules.system.dal.dataobject.sms.SysSmsCodeDO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SysSmsCodeMapper extends BaseMapperX<SysSmsCodeDO> {

    /**
     * 获得手机号的最后一个手机验证码
     *
     * @param mobile 手机号
     * @param scene 发送场景，选填
     * @param code 验证码 选填
     * @return 手机验证码
     */
    default SysSmsCodeDO selectLastByMobile(String mobile,String code,Integer scene) {
        return selectOne(new QueryWrapperX<SysSmsCodeDO>()
                .eq("mobile", mobile)
                .eqIfPresent("scene", scene)
                .eqIfPresent("code", code)
                .orderByDesc("id")
                .last("LIMIT 1"));
    }
}
