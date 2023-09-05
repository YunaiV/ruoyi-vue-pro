package cn.iocoder.yudao.module.member.api.brokerage;

import cn.iocoder.yudao.module.member.api.brokerage.dto.BrokerageAddReqDTO;
import cn.iocoder.yudao.module.member.service.brokerage.record.MemberBrokerageRecordService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 佣金 API 实现类
 *
 * @author owen
 */
@Service
@Validated
public class BrokerageApiImpl implements BrokerageApi {

    @Resource
    private MemberBrokerageRecordService memberBrokerageRecordService;

    @Override
    public void addBrokerage(Long userId, List<BrokerageAddReqDTO> list) {
        memberBrokerageRecordService.addBrokerage(userId, list);
    }

    @Override
    public void cancelBrokerage(Long userId, String bizId) {

    }
}
