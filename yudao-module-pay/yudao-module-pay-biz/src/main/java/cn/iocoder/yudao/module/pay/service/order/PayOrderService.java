package cn.iocoder.yudao.module.pay.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayNotifyReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.notify.PayOrderNotifyRespDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 支付订单 Service 接口
 *
 * @author aquan
 */
public interface PayOrderService {

    /**
     * 获得支付订单
     *
     * @param id 编号
     * @return 支付订单
     */
    PayOrderDO getOrder(Long id);

    /**
     * 获得支付订单
     * 分页
     *
     * @param pageReqVO 分页查询
     * @return 支付订单
     * 分页
     */
    PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO);

    /**
     * 获得支付订单
     * 列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 支付订单
     * 列表
     */
    List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO);

    /**
     * 根据 ID 集合获取只包含商品名称的订单集合
     *
     * @param idList 订单 ID 集合
     * @return 只包含商品名称的订单集合
     */
    List<PayOrderDO> getOrderSubjectList(Collection<Long> idList);

    /**
     * 根据订单 ID 集合获取订单商品名称Map集合
     *
     * @param idList 订单 ID 集合
     * @return 订单商品 map 集合
     */
    default Map<Long, PayOrderDO> getOrderSubjectMap(Collection<Long> idList) {
        List<PayOrderDO> list = getOrderSubjectList(idList);
        return CollectionUtils.convertMap(list, PayOrderDO::getId);
    }

    /**
     * 创建支付单
     *
     * @param reqDTO 创建请求
     * @return 支付单编号
     */
    Long createPayOrder(@Valid PayOrderCreateReqDTO reqDTO);

    /**
     * 提交支付
     * 此时，会发起支付渠道的调用
     *
     * @param reqVO 提交请求
     * @param userIp 提交 IP
     * @return 提交结果
     */
    PayOrderSubmitRespVO submitPayOrder(@Valid PayOrderSubmitReqVO reqVO,
                                        @NotEmpty(message = "提交 IP 不能为空") String userIp);

    /**
     * 通知支付单成功
     *
     * @param channelId 渠道编号
     * @param notify    通知
     * @param rawNotify 通知数据
     */
    void notifyPayOrder(Long channelId, PayOrderNotifyRespDTO notify, PayNotifyReqDTO rawNotify);

}
