package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
                .eqIfPresent(ErpPurchaseRequestDO::getApplicant, reqVO.getApplicant())
                .eqIfPresent(ErpPurchaseRequestDO::getApplicationDept, reqVO.getApplicationDept())
                .betweenIfPresent(ErpPurchaseRequestDO::getRequestTime, reqVO.getRequestTime())
                .eqIfPresent(ErpPurchaseRequestDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErpPurchaseRequestDO::getOffStatus, reqVO.getOffStatus())
                .eqIfPresent(ErpPurchaseRequestDO::getOrderStatus, reqVO.getOrderStatus())
                .eqIfPresent(ErpPurchaseRequestDO::getAuditor, reqVO.getAuditor())
                .betweenIfPresent(ErpPurchaseRequestDO::getAuditTime, reqVO.getAuditTime())
                .betweenIfPresent(ErpPurchaseRequestDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpPurchaseRequestDO::getId));
    }

    default ErpPurchaseRequestDO selectByNo(String no) {
        return selectOne(ErpPurchaseRequestDO::getNo, no);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpPurchaseRequestDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpPurchaseRequestDO>()
                .eq(ErpPurchaseRequestDO::getId, id).eq(ErpPurchaseRequestDO::getStatus, status));
    }
}