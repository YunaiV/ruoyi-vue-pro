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

    // TODO @qingxia：是不是不用这个方法，直接 updateById 即可
    default int updateIfPresent(MemberSignInConfigDO updateObj){
        LambdaUpdateWrapper <MemberSignInConfigDO> wrapper = new LambdaUpdateWrapper <>();
        wrapper.eq(MemberSignInConfigDO::getId, updateObj.getId())
                .set(updateObj.getDay() != null, MemberSignInConfigDO::getDay, updateObj.getDay())
                .set(updateObj.getPoint() != null, MemberSignInConfigDO::getPoint, updateObj.getPoint())
                .set(updateObj.getIsEnable() != null, MemberSignInConfigDO::getIsEnable, updateObj.getIsEnable());
        return update(null,wrapper);
    }

    // TODO @qingxia：不用这个方法，selectList，业务层自己排序即可
    default List<MemberSignInConfigDO> getList() {
        return selectList(new LambdaQueryWrapperX <MemberSignInConfigDO>().orderByAsc(MemberSignInConfigDO::getDay));
    }

    default MemberSignInConfigDO selectByDay(Integer day) {
        return selectOne(MemberSignInConfigDO::getDay, day);
    }

}
