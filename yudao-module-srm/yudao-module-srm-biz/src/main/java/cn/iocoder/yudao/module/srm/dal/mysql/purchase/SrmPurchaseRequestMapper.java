package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP采购申请单 Mapper
 *
 * @author wdy
 */
@Mapper
public interface SrmPurchaseRequestMapper extends BaseMapperX<SrmPurchaseRequestDO> {

    default MPJLambdaWrapperX<SrmPurchaseRequestDO> queryWrapper(SrmPurchaseRequestPageReqVO reqVO) {
        return new MPJLambdaWrapperX<SrmPurchaseRequestDO>().selectAll(SrmPurchaseRequestDO.class).eqIfPresent(SrmPurchaseRequestDO::getCode, reqVO.getCode())
            .eqIfPresent(SrmPurchaseRequestDO::getApplicantId, reqVO.getApplicantId())
            .eqIfPresent(SrmPurchaseRequestDO::getApplicationDeptId, reqVO.getApplicationDeptId())
            //supplierId 供应商编号
            .eqIfPresent(SrmPurchaseRequestDO::getSupplierId, reqVO.getSupplierId())
            .betweenIfPresent(SrmPurchaseRequestDO::getBillTime, reqVO.getBillTime())
            .eqIfPresent(SrmPurchaseRequestDO::getAuditorId, reqVO.getAuditorId()) //审核者
            .betweenIfPresent(SrmPurchaseRequestDO::getAuditTime, reqVO.getAuditTime())
            .betweenIfPresent(SrmPurchaseRequestDO::getCreateTime, reqVO.getCreateTime())
            //状态
            .eqIfPresent(SrmPurchaseRequestDO::getAuditStatus, reqVO.getAuditStatus()).eqIfPresent(SrmPurchaseRequestDO::getOffStatus, reqVO.getOffStatus())
            .eqIfPresent(SrmPurchaseRequestDO::getOrderStatus, reqVO.getOrderStatus()).eqIfPresent(SrmPurchaseRequestDO::getInboundStatus, reqVO.getInboundStatus())
            .eqIfPresent(SrmPurchaseRequestDO::getTag, reqVO.getTag())
            //
            .likeIfPresent(SrmPurchaseRequestDO::getDelivery, reqVO.getDelivery())
            .likeIfPresent(SrmPurchaseRequestDO::getAuditAdvice, reqVO.getAuditAdvice()).orderByDesc(SrmPurchaseRequestDO::getId);
    }

//需要分页主表	主表单独查 + 子表用 IN 批量查
    default PageResult<SrmPurchaseRequestDO> selectPage(SrmPurchaseRequestPageReqVO reqVO) {
        return selectPage(reqVO, queryWrapper(reqVO));
    }

    default SrmPurchaseRequestDO selectByNo(String no) {
        return selectOne(SrmPurchaseRequestDO::getCode, no);
    }

}