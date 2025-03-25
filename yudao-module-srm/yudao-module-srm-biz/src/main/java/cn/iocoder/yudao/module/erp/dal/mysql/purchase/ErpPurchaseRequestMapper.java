package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP采购申请单 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpPurchaseRequestMapper extends BaseMapperX<ErpPurchaseRequestDO> {

    default PageResult<ErpPurchaseRequestDO> selectPage(ErpPurchaseRequestPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpPurchaseRequestDO>()
            .eqIfPresent(ErpPurchaseRequestDO::getNo, reqVO.getNo())
            .eqIfPresent(ErpPurchaseRequestDO::getApplicantId, reqVO.getApplicantId())
            .eqIfPresent(ErpPurchaseRequestDO::getApplicationDeptId, reqVO.getApplicationDeptId())
            //supplierId 供应商编号
            .eqIfPresent(ErpPurchaseRequestDO::getSupplierId, reqVO.getSupplierId())
            .betweenIfPresent(ErpPurchaseRequestDO::getRequestTime, reqVO.getRequestTime())
            .eqIfPresent(ErpPurchaseRequestDO::getAuditorId, reqVO.getAuditorId()) //审核者
            .betweenIfPresent(ErpPurchaseRequestDO::getAuditTime, reqVO.getAuditTime())
            .betweenIfPresent(ErpPurchaseRequestDO::getCreateTime, reqVO.getCreateTime())
            //状态
            .eqIfPresent(ErpPurchaseRequestDO::getStatus, reqVO.getStatus())
            .eqIfPresent(ErpPurchaseRequestDO::getOffStatus, reqVO.getOffStatus())
            .eqIfPresent(ErpPurchaseRequestDO::getOrderStatus, reqVO.getOrderStatus())
            .eqIfPresent(ErpPurchaseRequestDO::getInStatus, reqVO.getInStatus())
            .eqIfPresent(ErpPurchaseRequestDO::getTag, reqVO.getTag())
            //
            .likeIfPresent(ErpPurchaseRequestDO::getDelivery, reqVO.getDelivery())
            .likeIfPresent(ErpPurchaseRequestDO::getReviewComment, reqVO.getReviewComment())
            .orderByDesc(ErpPurchaseRequestDO::getId));
    }

    default ErpPurchaseRequestDO selectByNo(String no) {
        return selectOne(ErpPurchaseRequestDO::getNo, no);
    }

}