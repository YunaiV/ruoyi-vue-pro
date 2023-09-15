package cn.iocoder.yudao.module.promotion.api.combination;

import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationActivityUpdateStockReqDTO;

// TODO @puhui999：是不是改成 CombinationActivityApi
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
    // TODO @puhui999：应该是更新哇？还是校验哈；
    void validateCombination(CombinationActivityUpdateStockReqDTO reqDTO);

}
