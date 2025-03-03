package cn.iocoder.yudao.module.erp.config.purchase.impl.action;


import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import com.alibaba.cola.statemachine.Action;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ActionOffImpl  implements Action<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> {
    @Resource
    private ErpPurchaseRequestMapper mapper;

    @Override
    @Transactional
    public void execute(ErpOffStatus from, ErpOffStatus to, ErpEventEnum event, ErpPurchaseRequestDO context) {
        ErpPurchaseRequestDO aDo = mapper.selectById(context.getId());
        aDo.setOffStatus(to.getCode());
        mapper.updateById(aDo);
        //ErpPurchaseRequestDO主表开启事件，则主表的子表都开启

        //ErpPurchaseRequestDO主表关闭事件，则子表的状态都关闭
        log.info("开关状态机触发({})事件：将对象{},由状态 {}->{}", event.getDesc(), JSONUtil.toJsonStr(context), from.getDesc(), to.getDesc());
    }
}