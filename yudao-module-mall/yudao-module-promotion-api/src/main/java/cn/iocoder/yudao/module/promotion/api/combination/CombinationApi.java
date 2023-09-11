package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationActivityUpdateStockReqDTO;

/**
 * 拼团活动 Api 接口
 *
 * @author HUIHUI
 */
public interface CombinationApi {

    /**
     * 更新活动库存
     *
     * @param reqDTO 请求
     */
    void validateCombination(CombinationActivityUpdateStockReqDTO reqDTO);

}
