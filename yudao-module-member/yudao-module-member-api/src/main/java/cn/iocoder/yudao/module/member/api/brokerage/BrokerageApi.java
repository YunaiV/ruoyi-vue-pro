package cn.iocoder.yudao.module.member.api.brokerage;

import cn.iocoder.yudao.module.member.api.brokerage.dto.BrokerageAddReqDTO;

import java.util.List;

/**
 * 佣金 API 接口
 *
 * @author owen
 */
public interface BrokerageApi {

    /**
     * 增加佣金
     *
     * @param userId 会员ID
     * @param list   请求参数列表
     */
    void addBrokerage(Long userId, List<BrokerageAddReqDTO> list);

    /**
     * 取消佣金
     *
     * @param userId 会员ID
     * @param bizId  业务编号
     */
    void cancelBrokerage(Long userId, String bizId);

}
