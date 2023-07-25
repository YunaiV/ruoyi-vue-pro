package cn.iocoder.yudao.module.member.dal.mysql.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInConfigPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInConfigUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分签到规则 Mapper
 *
 * @author QingX
 */
@Mapper
public interface MemberSignInConfigMapper extends BaseMapperX<MemberSignInConfigDO> {

    default PageResult<MemberSignInConfigDO> selectPage(MemberSignInConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberSignInConfigDO>()
                .eqIfPresent(MemberSignInConfigDO::getDay, reqVO.getDay())
                .orderByAsc(MemberSignInConfigDO::getDay));
    }

    //
    default long selectSameDayNotSelf(MemberSignInConfigUpdateReqVO reqVO){
        return selectCount(new LambdaQueryWrapperX <MemberSignInConfigDO>()
                .ne(MemberSignInConfigDO::getId, reqVO.getId())
                .eq(MemberSignInConfigDO::getDay,reqVO.getDay())
        );
    }

}
