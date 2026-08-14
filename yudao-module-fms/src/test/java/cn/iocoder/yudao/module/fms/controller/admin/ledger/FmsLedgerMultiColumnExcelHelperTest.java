package cn.iocoder.yudao.module.fms.controller.admin.ledger;

import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.multicolumn.FmsLedgerMultiColumnRespVO;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FmsLedgerMultiColumnExcelHelperTest {

    @Test
    public void testBuildHeadAndData() {
        // mock 数据
        FmsLedgerMultiColumnRespVO.Column debitColumn = new FmsLedgerMultiColumnRespVO.Column()
                .setSubjectId(101L).setSubjectCode("660201").setSubjectName("办公费")
                .setBalanceDirection(FmsDebitCreditDirectionEnum.DEBIT.getType());
        FmsLedgerMultiColumnRespVO.Column creditColumn = new FmsLedgerMultiColumnRespVO.Column()
                .setSubjectId(102L).setSubjectCode("2202").setSubjectName("应付账款")
                .setBalanceDirection(FmsDebitCreditDirectionEnum.CREDIT.getType());
        Map<Long, BigDecimal> columnAmounts = new LinkedHashMap<>();
        columnAmounts.put(debitColumn.getSubjectId(), new BigDecimal("80.00"));
        columnAmounts.put(creditColumn.getSubjectId(), new BigDecimal("30.00"));
        FmsLedgerDetailRespVO row = new FmsLedgerDetailRespVO()
                .setAccountDate(LocalDate.of(2026, 8, 31)).setVoucherNumber("记-1")
                .setDigest("期末余额").setDebitAmount(BigDecimal.ZERO).setCreditAmount(BigDecimal.ZERO)
                .setBalanceDirection("借").setBalance(new BigDecimal("50.00"))
                .setColumnAmounts(columnAmounts);
        FmsLedgerMultiColumnRespVO result = new FmsLedgerMultiColumnRespVO()
                .setColumns(Arrays.asList(creditColumn, debitColumn))
                .setRows(Collections.singletonList(row));

        // 调用
        List<List<String>> head = FmsLedgerMultiColumnExcelHelper.buildHead(result);
        List<List<Object>> data = FmsLedgerMultiColumnExcelHelper.buildData(result);

        // 断言
        assertEquals(9, head.size());
        assertEquals(Arrays.asList("借", "660201/办公费"), head.get(7));
        assertEquals(Arrays.asList("贷", "2202/应付账款"), head.get(8));
        assertEquals(1, data.size());
        assertEquals(new BigDecimal("80.00"), data.get(0).get(7));
        assertEquals(new BigDecimal("30.00"), data.get(0).get(8));
    }

}
