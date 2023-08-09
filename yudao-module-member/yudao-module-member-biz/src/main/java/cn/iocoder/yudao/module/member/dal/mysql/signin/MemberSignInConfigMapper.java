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

    /**
     * 描述    :选择性更新对象属性，如果不为空则更新。
     * Author :xiaqing
     * Date   :2023-08-08 23:38:48
     */
    default int updateIfPresent(MemberSignInConfigDO updateObj){
        LambdaUpdateWrapper <MemberSignInConfigDO> wrapper = new LambdaUpdateWrapper <>();
        wrapper.eq(MemberSignInConfigDO::getId, updateObj.getId())
                .set(updateObj.getDay() != null, MemberSignInConfigDO::getDay, updateObj.getDay())
                .set(updateObj.getPoint() != null, MemberSignInConfigDO::getPoint, updateObj.getPoint())
                .set(updateObj.getIsEnable() != null, MemberSignInConfigDO::getIsEnable, updateObj.getIsEnable());
        return update(null,wrapper);
    }

    default List <MemberSignInConfigDO> getList(){
        return selectList(new LambdaQueryWrapperX <MemberSignInConfigDO>().orderByAsc(MemberSignInConfigDO::getDay));
    }

    /**
     * 描述    :根据天数查询对应记录
     * Date   :2023-08-09 00:07:11
     */
    default MemberSignInConfigDO selectByDay(Integer day){
        return selectOne(new LambdaQueryWrapperX <MemberSignInConfigDO>()
                .eq(MemberSignInConfigDO::getDay,day));

    }

}
