package cn.iocoder.yudao.module.member.dal.mysql.user;

import cn.iocoder.yudao.coreservice.modules.member.dal.dataobject.user.MbrUserDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * MbrUserDO Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MbrUserMapper extends BaseMapperX<MbrUserDO> {

    default MbrUserDO selectByMobile(String mobile) {
        return selectOne(MbrUserDO::getMobile, mobile);
    }

}
