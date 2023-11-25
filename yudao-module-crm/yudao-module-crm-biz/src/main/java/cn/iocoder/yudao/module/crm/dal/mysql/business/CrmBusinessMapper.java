package cn.iocoder.yudao.module.crm.dal.mysql.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 商机 Mapper
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessMapper extends BaseMapperX<CrmBusinessDO> {

    default PageResult<CrmBusinessDO> selectPage(CrmBusinessPageReqVO reqVO, Collection<Long> ids) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmBusinessDO>()
                .in(CrmBusinessDO::getId, ids)
                .likeIfPresent(CrmBusinessDO::getName, reqVO.getName())
                .orderByDesc(CrmBusinessDO::getId));
    }

    default List<CrmBusinessDO> selectList(CrmBusinessExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CrmBusinessDO>()
                .likeIfPresent(CrmBusinessDO::getName, reqVO.getName())
                .eqIfPresent(CrmBusinessDO::getStatusTypeId, reqVO.getStatusTypeId())
                .eqIfPresent(CrmBusinessDO::getStatusId, reqVO.getStatusId())
                .betweenIfPresent(CrmBusinessDO::getContactNextTime, reqVO.getContactNextTime())
                .eqIfPresent(CrmBusinessDO::getCustomerId, reqVO.getCustomerId())
                .betweenIfPresent(CrmBusinessDO::getDealTime, reqVO.getDealTime())
                .eqIfPresent(CrmBusinessDO::getPrice, reqVO.getPrice())
                .eqIfPresent(CrmBusinessDO::getDiscountPercent, reqVO.getDiscountPercent())
                .eqIfPresent(CrmBusinessDO::getProductPrice, reqVO.getProductPrice())
                .eqIfPresent(CrmBusinessDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(CrmBusinessDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(CrmBusinessDO::getEndStatus, reqVO.getEndStatus())
                .eqIfPresent(CrmBusinessDO::getEndRemark, reqVO.getEndRemark())
                .betweenIfPresent(CrmBusinessDO::getContactLastTime, reqVO.getContactLastTime())
                .eqIfPresent(CrmBusinessDO::getFollowUpStatus, reqVO.getFollowUpStatus())
                .orderByDesc(CrmBusinessDO::getId));
    }

}
