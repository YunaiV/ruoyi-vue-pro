package cn.iocoder.yudao.module.crm.service.statistics;

import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product.CrmStatisticsProductCategoryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product.CrmStatisticsProductReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product.CrmStatisticsProductSalesRespVO;

import java.util.List;

/**
 * CRM 产品分析 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmStatisticsProductService {

    /**
     * 获取产品销售情况统计
     *
     * @param reqVO 请求参数
     * @return 产品销售情况
     */
    List<CrmStatisticsProductSalesRespVO> getProductSalesList(CrmStatisticsProductReqVO reqVO);

    /**
     * 获取产品分类销售分析
     *
     * @param reqVO 请求参数
     * @return 产品分类销售分析
     */
    List<CrmStatisticsProductCategoryRespVO> getProductCategorySummary(CrmStatisticsProductReqVO reqVO);

}
