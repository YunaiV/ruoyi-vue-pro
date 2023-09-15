package cn.iocoder.yudao.module.pay.service.refund;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;

import java.util.List;

/**
 * 退款订单 Service 接口
 *
 * @author aquan
 */
public interface PayRefundService {

    /**
     * 获得退款订单
     *
     * @param id 编号
     * @return 退款订单
     */
    PayRefundDO getRefund(Long id);

    /**
     * 获得退款订单
     *
     * @param no 外部退款单号
     * @return 退款订单
     */
    PayRefundDO getRefundByNo(String no);

    /**
     * 获得指定应用的退款数量
     *
     * @param appId 应用编号
     * @return 退款数量
     */
    Long getRefundCountByAppId(Long appId);

    /**
     * 获得退款订单分页
     *
     * @param pageReqVO 分页查询
     * @return 退款订单分页
     */
    PageResult<PayRefundDO> getRefundPage(PayRefundPageReqVO pageReqVO);

    /**
     * 获得退款订单列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 退款订单列表
     */
    List<PayRefundDO> getRefundList(PayRefundExportReqVO exportReqVO);

    /**
     * 创建退款申请
     *
     * @param reqDTO 退款申请信息
     * @return 退款单号
     */
    Long createPayRefund(PayRefundCreateReqDTO reqDTO);

    /**
     * 渠道的退款通知
     *
     * @param channelId  渠道编号
     * @param notify     通知
     */
    void notifyRefund(Long channelId, PayRefundRespDTO notify);

    /**
     * 同步渠道退款的退款状态
     *
     * @return 同步到状态的退款数量，包括退款成功、退款失败
     */
    int syncRefund();

}
