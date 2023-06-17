package cn.iocoder.yudao.module.point.dal.mysql.signinrecord;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.point.dal.dataobject.signinrecord.SignInRecordDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo.*;

/**
 * 用户签到积分 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface SignInRecordMapper extends BaseMapperX<SignInRecordDO> {

    default PageResult<SignInRecordDO> selectPage(SignInRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SignInRecordDO>()
                .eqIfPresent(SignInRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SignInRecordDO::getDay, reqVO.getDay())
                .betweenIfPresent(SignInRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SignInRecordDO::getId));
    }

    default List<SignInRecordDO> selectList(SignInRecordExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<SignInRecordDO>()
                .eqIfPresent(SignInRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(SignInRecordDO::getDay, reqVO.getDay())
                .betweenIfPresent(SignInRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SignInRecordDO::getId));
    }

}
