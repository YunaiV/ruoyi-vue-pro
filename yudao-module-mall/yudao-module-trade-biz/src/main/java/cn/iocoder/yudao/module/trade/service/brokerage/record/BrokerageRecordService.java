package cn.iocoder.yudao.module.trade.service.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.BrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.BrokerageRecordDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import cn.iocoder.yudao.module.trade.service.brokerage.bo.UserBrokerageSummaryBO;

import javax.validation.Valid;
import java.util.List;

/**
 * 佣金记录 Service 接口
 *
 * @author owen
 */
public interface BrokerageRecordService {

    /**
     * 获得佣金记录
     *
     * @param id 编号
     * @return 佣金记录
     */
    BrokerageRecordDO getBrokerageRecord(Integer id);

    /**
     * 获得佣金记录分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金记录分页
     */
    PageResult<BrokerageRecordDO> getBrokerageRecordPage(BrokerageRecordPageReqVO pageReqVO);

    /**
     * 增加佣金
     *
     * @param userId  会员编号
     * @param bizType 业务类型
     * @param list    请求参数列表
     */
    void addBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, @Valid List<BrokerageAddReqBO> list);

    /**
     * 取消佣金：将佣金记录，状态修改为已失效
     *
     * @param userId  会员编号
     * @param bizType 业务类型
     * @param bizId   业务编号
     */
    void cancelBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId);

    /**
     * 解冻佣金：将待结算的佣金记录，状态修改为已结算
     *
     * @return 解冻佣金的数量
     */
    int unfreezeRecord();

    /**
     * 汇总用户佣金
     *
     * @param userId  用户编号
     * @param bizType 业务类型
     * @param status  佣金状态
     * @return 用户佣金汇总
     */
    UserBrokerageSummaryBO summaryByUserIdAndBizTypeAndStatus(Long userId, Integer bizType, Integer status);
}
