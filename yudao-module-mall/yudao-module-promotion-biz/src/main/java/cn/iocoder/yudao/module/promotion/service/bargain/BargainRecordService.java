package cn.iocoder.yudao.module.promotion.service.bargain;


import cn.iocoder.yudao.module.promotion.api.bargain.dto.BargainValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.record.AppBargainRecordCreateReqVO;

/**
 * 砍价记录 service 接口
 *
 * @author HUIHUI
 */
public interface BargainRecordService {

    /**
     * 【会员】创建砍价记录（参与参加活动）
     *
     * @param userId 用户编号
     * @param reqVO 创建信息
     * @return 砍价记录编号
     */
    Long createBargainRecord(Long userId, AppBargainRecordCreateReqVO reqVO);

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
