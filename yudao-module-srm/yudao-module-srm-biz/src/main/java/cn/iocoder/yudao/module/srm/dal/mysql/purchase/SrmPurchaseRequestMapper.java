package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.SrmPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.bo.SrmPurchaseRequestBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP采购申请单 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface SrmPurchaseRequestMapper extends BaseMapperX<SrmPurchaseRequestDO> {

    default MPJLambdaWrapperX<SrmPurchaseRequestDO> queryWrapper(SrmPurchaseRequestPageReqVO reqVO) {
        return new MPJLambdaWrapperX<SrmPurchaseRequestDO>().selectAll(SrmPurchaseRequestDO.class).eqIfPresent(SrmPurchaseRequestDO::getNo, reqVO.getNo())
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
            .eqIfPresent(SrmPurchaseRequestDO::getOrderStatus, reqVO.getOrderStatus()).eqIfPresent(SrmPurchaseRequestDO::getInStatus, reqVO.getInStatus())
            .eqIfPresent(SrmPurchaseRequestDO::getTag, reqVO.getTag())
            //
            .likeIfPresent(SrmPurchaseRequestDO::getDelivery, reqVO.getDelivery())
            .likeIfPresent(SrmPurchaseRequestDO::getReviewComment, reqVO.getReviewComment()).orderByDesc(SrmPurchaseRequestDO::getId);
    }

    //getBoWrapper
    default MPJLambdaWrapper<SrmPurchaseRequestDO> getBoWrapper(SrmPurchaseRequestPageReqVO reqVO) {
        return queryWrapper(reqVO).innerJoin(SrmPurchaseRequestItemsDO.class, SrmPurchaseRequestItemsDO::getRequestId, SrmPurchaseRequestDO::getId,
                on -> on.eqIfExists(SrmPurchaseRequestItemsDO::getProductId, reqVO.getProductId())
                    .likeIfExists(SrmPurchaseRequestItemsDO::getBarCode, reqVO.getBarCode())
                    .likeIfExists(SrmPurchaseRequestItemsDO::getProductUnitName, reqVO.getProductUnitName())
                    .likeIfExists(SrmPurchaseRequestItemsDO::getProductName, reqVO.getProductName())).selectAll(SrmPurchaseRequestItemsDO.class)
            .selectAsClass(SrmPurchaseRequestItemsDO.class, SrmPurchaseRequestBO.class);
    }

    default PageResult<SrmPurchaseRequestDO> selectPage(SrmPurchaseRequestPageReqVO reqVO) {
        return selectPage(reqVO, getBoWrapper(reqVO));
    }

    default SrmPurchaseRequestDO selectByNo(String no) {
        return selectOne(SrmPurchaseRequestDO::getNo, no);
    }

}