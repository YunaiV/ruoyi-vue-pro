package cn.iocoder.yudao.module.trade.service.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.TradeBrokerageRecordDO;
import cn.iocoder.yudao.module.trade.service.brokerage.record.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.TradeBrokerageRecordPageReqVO;

import java.util.List;

/**
 * 佣金记录 Service 接口
 *
 * @author owen
 */
public interface TradeBrokerageRecordService {

    /**
     * 获得佣金记录
     *
     * @param id 编号
     * @return 佣金记录
     */
    TradeBrokerageRecordDO getBrokerageRecord(Integer id);

    /**
     * 获得佣金记录分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金记录分页
     */
    PageResult<TradeBrokerageRecordDO> getBrokerageRecordPage(TradeBrokerageRecordPageReqVO pageReqVO);

    // TODO @疯狂：是不是 bizType 得加下？方便未来拓展哈；
    /**
     * 增加佣金
     *
     * @param userId 会员编号
     * @param list   请求参数列表
     */
    void addBrokerage(Long userId, List<BrokerageAddReqBO> list);

    // TODO @疯狂：是不是 bizType 得加下？方便未来拓展哈；
    /**
     * 取消佣金：将佣金记录，状态修改为已失效
     *
     * @param userId 会员编号
     * @param bizId 业务编号
     */
    void cancelBrokerage(Long userId, String bizId);

    /**
     * 解冻佣金：将待结算的佣金记录，状态修改为已结算
     *
     * @return 解冻佣金的数量
     */
    int unfreezeRecord();

}
