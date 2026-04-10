package cn.iocoder.yudao.module.mes.service.wm.transaction;

import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 库存事务流水 Service 接口
 *
 * 校验 → 库存台账更新 → 事务流水插入。
 */
public interface MesWmTransactionService {

    /**
     * 创建库存事务（含校验 + 库存更新 + 插入流水）
     *
     * @param reqDTO 事务数据
     * @return 事务流水编号
     */
    Long createTransaction(@Valid MesWmTransactionSaveReqDTO reqDTO);

    /**
     * 批量创建库存事务
     *
     * @param reqDTOs 事务数据列表
     */
    void createTransactionList(List<MesWmTransactionSaveReqDTO> reqDTOs);

}
