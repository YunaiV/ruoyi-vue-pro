package cn.iocoder.yudao.module.member.dal.mysql.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分记录 Mapper
 *
 * @author QingX
 */
@Mapper
public interface MemberPointRecordMapper extends BaseMapperX<MemberPointRecordDO> {

    default PageResult<MemberPointRecordDO> selectPage(MemberPointRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberPointRecordDO>()
                .eqIfPresent(MemberPointRecordDO::getBizId, reqVO.getBizId())
                .eqIfPresent(MemberPointRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MemberPointRecordDO::getType, reqVO.getType())
                .eqIfPresent(MemberPointRecordDO::getTitle, reqVO.getTitle())
                .eqIfPresent(MemberPointRecordDO::getStatus, reqVO.getStatus())
                .orderByDesc(MemberPointRecordDO::getId));
    }

}
