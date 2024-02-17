package cn.iocoder.yudao.module.crm.dal.mysql.bi;

import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.CrmBiRanKRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.CrmBiRankReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CRM BI 排行榜 Mapper
 *
 * @author anhaohao
 */
@Mapper
public interface CrmBiRankingMapper {

    /**
     * 查询合同金额排行榜
     *
     * @param rankReqVO 参数
     * @return 合同金额排行榜
     */
    List<CrmBiRanKRespVO> selectContractPriceRank(CrmBiRankReqVO rankReqVO);

    /**
     * 查询回款金额排行榜
     *
     * @param rankReqVO 参数
     * @return 回款金额排行榜
     */
    List<CrmBiRanKRespVO> selectReceivablePriceRank(CrmBiRankReqVO rankReqVO);

    /**
     * 查询签约合同数量排行榜
     *
     * @param rankReqVO 参数
     * @return 签约合同数量排行榜
     */
    List<CrmBiRanKRespVO> selectContractCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 查询产品销量排行榜
     *
     * @param rankReqVO 参数
     * @return 产品销量排行榜
     */
    List<CrmBiRanKRespVO> selectProductSalesRank(CrmBiRankReqVO rankReqVO);

    /**
     * 查询新增客户数排行榜
     *
     * @param rankReqVO 参数
     * @return 新增客户数排行榜
     */
    List<CrmBiRanKRespVO> selectCustomerCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 查询联系人数量排行榜
     *
     * @param rankReqVO 参数
     * @return 联系人数量排行榜
     */
    List<CrmBiRanKRespVO> selectContactsCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 查询跟进次数排行榜
     *
     * @param rankReqVO 参数
     * @return 跟进次数排行榜
     */
    List<CrmBiRanKRespVO> selectFollowCountRank(CrmBiRankReqVO rankReqVO);

    /**
     * 查询跟进客户数排行榜
     *
     * @param rankReqVO 参数
     * @return 跟进客户数排行榜
     */
    List<CrmBiRanKRespVO> selectFollowCustomerCountRank(CrmBiRankReqVO rankReqVO);

}
