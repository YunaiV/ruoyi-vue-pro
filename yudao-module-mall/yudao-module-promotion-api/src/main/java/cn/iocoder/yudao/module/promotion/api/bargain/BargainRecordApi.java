package cn.iocoder.yudao.module.promotion.api.bargain;

import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainValidateJoinRespDTO;

import javax.validation.Valid;

// TODO @芋艿：后面也再撸撸这几个接口

/**
 * 砍价记录 API 接口
 *
 * @author HUIHUI
 */
public interface BargainRecordApi {

    /**
     * 创建砍价记录
     *
     * @param reqDTO 请求 DTO
     */
    void createBargainRecord(@Valid BargainRecordCreateReqDTO reqDTO);

    /**
     * 【下单前】校验是否参与砍价活动
     * <p>
     * 如果校验失败，则抛出业务异常
     *
     * @param userId          用户编号
     * @param bargainRecordId 砍价活动编号
     * @param skuId           SKU 编号
     * @return 砍价信息
     */
    BargainValidateJoinRespDTO validateJoinBargain(Long userId, Long bargainRecordId, Long skuId);

}
