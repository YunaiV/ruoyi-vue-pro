package cn.iocoder.yudao.module.member.dal.mysql.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.admin.brokerage.record.vo.MemberBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.brokerage.record.MemberBrokerageRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

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

    default List<MemberBrokerageRecordDO> selectListByStatusAndUnfreezeTimeLt(Integer status, LocalDateTime unfreezeTime) {
        return selectList(new LambdaQueryWrapper<MemberBrokerageRecordDO>()
                .eq(MemberBrokerageRecordDO::getStatus, status)
                .lt(MemberBrokerageRecordDO::getUnfreezeTime, unfreezeTime));
    }

    default int updateByIdAndStatus(Integer id, Integer status, MemberBrokerageRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<MemberBrokerageRecordDO>()
                .eq(MemberBrokerageRecordDO::getId, id)
                .eq(MemberBrokerageRecordDO::getStatus, status));
    }

    default MemberBrokerageRecordDO selectByUserIdAndBizTypeAndBizId(Integer bizType, String bizId) {
        return selectOne(MemberBrokerageRecordDO::getBizType, bizType,
                MemberBrokerageRecordDO::getBizId, bizId);
    }
}
