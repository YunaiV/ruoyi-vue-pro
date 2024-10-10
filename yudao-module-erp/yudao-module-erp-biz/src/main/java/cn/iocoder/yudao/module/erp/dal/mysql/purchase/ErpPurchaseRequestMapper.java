package cn.iocoder.yudao.module.erp.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
                .eqIfPresent(ErpPurchaseRequestDO::getNum, reqVO.getNum())
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
    /**
    * @Author Wqh
    * @Description 获取单据日期数据库中编号最大的数字
    * @Date 10:26 2024/10/10
    * @Param [minDateTime, maxDateTime]
    * @return java.lang.Integer
    **/
    default Integer getMaxSerialNum(LocalDateTime minDateTime, LocalDateTime maxDateTime){
        //获取该单据日期里数据库中编号最大的数字
        ErpPurchaseRequestDO erpPurchaseRequestDO = selectOne(new LambdaQueryWrapperX<ErpPurchaseRequestDO>()
                .select(ErpPurchaseRequestDO::getNum)
                .ge(ErpPurchaseRequestDO::getRequestTime, DateUtils.formatLocalDateTime(minDateTime))
                .le(ErpPurchaseRequestDO::getRequestTime, DateUtils.formatLocalDateTime(maxDateTime))
                .orderByDesc(ErpPurchaseRequestDO::getNum)
                .last("limit 1"));
        return erpPurchaseRequestDO == null ? 0 : erpPurchaseRequestDO.getNum();
    }
}