package cn.iocoder.yudao.userserver.modules.member.dal.mysql.user;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.userserver.modules.member.dal.dataobject.user.MbrUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MbrUserDO Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MbrUserMapper extends BaseMapperX<MbrUserDO> {

    default MbrUserDO selectByMobile(String mobile) {
        return selectOne("mobile", mobile);
    }

}
