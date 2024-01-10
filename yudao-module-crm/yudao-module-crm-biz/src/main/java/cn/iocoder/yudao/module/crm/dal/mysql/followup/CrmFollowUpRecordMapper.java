package cn.iocoder.yudao.module.crm.dal.mysql.followup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 跟进记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmFollowUpRecordMapper extends BaseMapperX<CrmFollowUpRecordDO> {

    default PageResult<CrmFollowUpRecordDO> selectPage(CrmFollowUpRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmFollowUpRecordDO>()
                .eqIfPresent(CrmFollowUpRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(CrmFollowUpRecordDO::getBizId, reqVO.getBizId())
                .eqIfPresent(CrmFollowUpRecordDO::getType, reqVO.getType())
                .betweenIfPresent(CrmFollowUpRecordDO::getNextTime, reqVO.getNextTime())
                .betweenIfPresent(CrmFollowUpRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CrmFollowUpRecordDO::getId));
    }

}