package cn.iocoder.yudao.module.point.dal.mysql.signinconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.point.dal.dataobject.signinconfig.SignInConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo.*;

/**
 * 积分签到规则 Mapper
 *
 * @author QingX
 */
@Mapper
public interface SignInConfigMapper extends BaseMapperX<SignInConfigDO> {

    default PageResult<SignInConfigDO> selectPage(SignInConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SignInConfigDO>()
                .eqIfPresent(SignInConfigDO::getDay, reqVO.getDay())
                .orderByAsc(SignInConfigDO::getDay));
    }

    default List<SignInConfigDO> selectList(SignInConfigExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SignInConfigDO>()
                .eqIfPresent(SignInConfigDO::getDay, reqVO.getDay())
                .orderByDesc(SignInConfigDO::getId));
    }

    //
    default long selectSameDayNotSelf(SignInConfigUpdateReqVO reqVO){
        return selectCount(new LambdaQueryWrapperX <SignInConfigDO>()
                .ne(SignInConfigDO::getId, reqVO.getId())
                .eq(SignInConfigDO::getDay,reqVO.getDay())
        );
    }

}
