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

}
