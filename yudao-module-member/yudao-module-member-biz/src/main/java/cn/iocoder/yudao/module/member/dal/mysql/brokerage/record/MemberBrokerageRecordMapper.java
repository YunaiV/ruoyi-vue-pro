package cn.iocoder.yudao.module.member.dal.mysql.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.brokerage.record.vo.MemberBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.brokerage.record.MemberBrokerageRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 佣金记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface MemberBrokerageRecordMapper extends BaseMapperX<MemberBrokerageRecordDO> {

    default PageResult<MemberBrokerageRecordDO> selectPage(MemberBrokerageRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberBrokerageRecordDO>()
                .eqIfPresent(MemberBrokerageRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MemberBrokerageRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(MemberBrokerageRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MemberBrokerageRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MemberBrokerageRecordDO::getId));
    }

}
