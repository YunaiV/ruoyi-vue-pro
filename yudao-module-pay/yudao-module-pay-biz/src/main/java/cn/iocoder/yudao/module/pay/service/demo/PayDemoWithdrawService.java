package cn.iocoder.yudao.module.pay.service.demo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.withdraw.PayDemoWithdrawCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoWithdrawDO;

import jakarta.validation.Valid;

/**
 * 示例提现单 Service 接口
 *
 * @author jason
 */
public interface PayDemoWithdrawService {

    /**
     * 创建示例提现单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemoWithdraw(@Valid PayDemoWithdrawCreateReqVO createReqVO);

    /**
     * 获得示例提现单分页
     *
     * @param pageVO 分页查询参数
     */
    PageResult<PayDemoWithdrawDO> getDemoWithdrawPage(PageParam pageVO);

    /**
     * 更新示例提现单的状态
     *
     * @param id 编号
     * @param payTransferId 转账单编号
     */
    void updateDemoWithdrawStatus(Long id, Long payTransferId);

}
