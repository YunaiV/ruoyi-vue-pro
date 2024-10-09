package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.PurchaseRequestDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP采购申请单 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface PurchaseRequestMapper extends BaseMapperX<PurchaseRequestDO> {

    default PageResult<PurchaseRequestDO> selectPage(PurchaseRequestPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PurchaseRequestDO>()
                .eqIfPresent(PurchaseRequestDO::getSerial, reqVO.getSerial())
                .eqIfPresent(PurchaseRequestDO::getNum, reqVO.getNum())
                .eqIfPresent(PurchaseRequestDO::getApplicant, reqVO.getApplicant())
                .eqIfPresent(PurchaseRequestDO::getApplicationDept, reqVO.getApplicationDept())
                .betweenIfPresent(PurchaseRequestDO::getDate, reqVO.getDate())
                .eqIfPresent(PurchaseRequestDO::getApplicationStatus, reqVO.getApplicationStatus())
                .eqIfPresent(PurchaseRequestDO::getOffStatus, reqVO.getOffStatus())
                .eqIfPresent(PurchaseRequestDO::getOrderStatus, reqVO.getOrderStatus())
                .eqIfPresent(PurchaseRequestDO::getAuditor, reqVO.getAuditor())
                .betweenIfPresent(PurchaseRequestDO::getAuditTime, reqVO.getAuditTime())
                .betweenIfPresent(PurchaseRequestDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PurchaseRequestDO::getId));
    }

}