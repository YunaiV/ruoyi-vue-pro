package cn.iocoder.yudao.module.crm.service.callcenter;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterCallReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.callcenter.CrmCallcenterUserDO;
import org.springframework.http.ResponseEntity;


/**
 * 外各 Service 接口
 *
 * @author fhqsuhpv
 */
public interface CrmCallCenterService {


    /**
     * 向外呼叫
     *
     * @param callReqVO
     * @param userId
     * @return
     */
    CommonResult<ResponseEntity<String>> call(CrmCallcenterCallReqVO callReqVO, Long userId);

    CrmCallcenterUserDO getCallCenterUser(String phone);

}
