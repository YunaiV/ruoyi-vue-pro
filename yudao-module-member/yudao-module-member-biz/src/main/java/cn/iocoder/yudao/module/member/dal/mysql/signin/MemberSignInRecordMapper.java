package cn.iocoder.yudao.module.member.dal.mysql.signin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户签到积分 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MemberSignInRecordMapper extends BaseMapperX<MemberSignInRecordDO> {

    default PageResult<MemberSignInRecordDO> selectPage(MemberSignInRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberSignInRecordDO>()
                .eqIfPresent(MemberSignInRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MemberSignInRecordDO::getDay, reqVO.getDay())
                .betweenIfPresent(MemberSignInRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberSignInRecordDO::getId));
    }

}
