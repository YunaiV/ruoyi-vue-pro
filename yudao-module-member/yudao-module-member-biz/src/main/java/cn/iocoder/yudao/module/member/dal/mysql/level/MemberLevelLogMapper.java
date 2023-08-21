package cn.iocoder.yudao.module.member.dal.mysql.level;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.level.vo.log.MemberLevelLogPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface MemberLevelLogMapper extends BaseMapperX<MemberLevelLogDO> {

    default PageResult<MemberLevelLogDO> selectPage(MemberLevelLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberLevelLogDO>()
                .eqIfPresent(MemberLevelLogDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MemberLevelLogDO::getLevelId, reqVO.getLevelId())
                .betweenIfPresent(MemberLevelLogDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberLevelLogDO::getId));
    }

}
