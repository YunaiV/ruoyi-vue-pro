package cn.iocoder.yudao.module.member.service.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.api.brokerage.dto.BrokerageAddReqDTO;
import cn.iocoder.yudao.module.member.controller.admin.brokerage.record.vo.MemberBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.brokerage.record.MemberBrokerageRecordDO;

import java.util.List;

/**
 * 佣金记录 Service 接口
 *
 * @author owen
 */
public interface MemberBrokerageRecordService {

    /**
     * 获得佣金记录
     *
     * @param id 编号
     * @return 佣金记录
     */
    MemberBrokerageRecordDO getMemberBrokerageRecord(Integer id);

    /**
     * 获得佣金记录分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金记录分页
     */
    PageResult<MemberBrokerageRecordDO> getMemberBrokerageRecordPage(MemberBrokerageRecordPageReqVO pageReqVO);

    /**
     * 增加佣金
     *
     * @param userId 会员ID
     * @param list   请求参数列表
     */
    void addBrokerage(Long userId, List<BrokerageAddReqDTO> list);

    /**
     * 解冻佣金：将待结算的佣金记录，状态修改为已结算
     *
     * @return 解冻佣金的数量
     */
    int unfreezeRecord();

}
