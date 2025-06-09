package cn.iocoder.yudao.module.tms.config.first.mile.impl.action;

import cn.iocoder.yudao.framework.cola.statemachine.Action;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.tms.api.first.mile.dto.TmsFistMileUpdateDTO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileAuditReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import cn.iocoder.yudao.module.tms.service.first.mile.TmsFirstMileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FirstMileAuditActionImpl implements Action<TmsAuditStatus, TmsEventEnum, TmsFirstMileAuditReqVO> {

    @Autowired
    @Lazy
    TmsFirstMileService tmsFirstMileService;
    @Override
    public void execute(TmsAuditStatus from, TmsAuditStatus to, TmsEventEnum event, TmsFirstMileAuditReqVO context) {

        TmsFirstMileDO firstMileDO = tmsFirstMileService.getFirstMile(context.getId());
        TmsFistMileUpdateDTO dto = BeanUtils.toBean(firstMileDO, TmsFistMileUpdateDTO.class, fistMileDTO -> fistMileDTO.setAuditStatus(to.getCode()));
        if (event == TmsEventEnum.AGREE || event == TmsEventEnum.REJECT) {
            //审核通过?不通过，需要有审核意见
            dto.setAuditAdvice(context.getAuditAdvice());
        }
        //反审核的时候，审核意见制空
        if (event == TmsEventEnum.WITHDRAW_REVIEW) {
            dto.setAuditAdvice(null);
        }

        //更新状态
        tmsFirstMileService.updateFirstMileStatus(dto);
    }
}
