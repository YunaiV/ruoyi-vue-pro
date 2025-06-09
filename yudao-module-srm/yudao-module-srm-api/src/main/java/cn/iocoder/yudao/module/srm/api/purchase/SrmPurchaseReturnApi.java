package cn.iocoder.yudao.module.srm.api.purchase;

import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseReturnDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.req.SrmReturnSaveReqDTO;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 采购退货单 API 接口
 */
@Validated
public interface SrmPurchaseReturnApi {

    /**
     * 获得采购退货单列表
     *
     * @param ids 采购退货单编号列表
     * @return 采购退货单列表
     */
    List<SrmPurchaseReturnDTO> getPurchaseReturnList(List<Long> ids);


    /**
     * 状态机-变动退货项-退货数量
     *<p>
     * 出库单审核后回调
     */
    void updatePurchaseReturnItemQty(SrmReturnSaveReqDTO reqDTO);
} 