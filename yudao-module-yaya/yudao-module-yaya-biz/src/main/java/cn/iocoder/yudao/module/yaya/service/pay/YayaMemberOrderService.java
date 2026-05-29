package cn.iocoder.yudao.module.yaya.service.pay;

import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderCreateReqVO;
import cn.iocoder.yudao.module.yaya.controller.app.pay.vo.YayaAppMemberOrderRespVO;

public interface YayaMemberOrderService {

    YayaAppMemberOrderRespVO createMemberOrder(Long memberUserId, YayaAppMemberOrderCreateReqVO reqVO, String userIp);

    void activateEntitlementByPayOrder(Long memberOrderId, Long payOrderId);

}
