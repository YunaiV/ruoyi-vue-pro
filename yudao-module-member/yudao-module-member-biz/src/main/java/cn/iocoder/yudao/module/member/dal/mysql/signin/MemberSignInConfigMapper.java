package cn.iocoder.yudao.module.member.dal.mysql.signin;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 积分签到规则 Mapper
 *
 * @author QingX
 */
@Mapper
public interface MemberSignInConfigMapper extends BaseMapperX<MemberSignInConfigDO> {


    default MemberSignInConfigDO selectByDay(Integer day) {
        return selectOne(MemberSignInConfigDO::getDay, day);
    }

}
