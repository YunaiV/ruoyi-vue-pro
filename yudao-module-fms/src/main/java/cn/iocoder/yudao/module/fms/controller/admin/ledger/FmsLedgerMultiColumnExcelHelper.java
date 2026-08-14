package cn.iocoder.yudao.module.fms.controller.admin.ledger;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.detail.FmsLedgerDetailRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.ledger.vo.multicolumn.FmsLedgerMultiColumnRespVO;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

/**
 * FMS 多栏账 Excel 构建工具
 *
 * @author 芋道源码
 */
public class FmsLedgerMultiColumnExcelHelper {

    private FmsLedgerMultiColumnExcelHelper() {
    }

    /**
     * 构建多栏账动态表头
     *
     * @param result 多栏账结果
     * @return 动态表头
     */
    public static List<List<String>> buildHead(FmsLedgerMultiColumnRespVO result) {
        List<List<String>> head = new ArrayList<>();
        head.add(Arrays.asList("日期", "日期"));
        head.add(Arrays.asList("凭证字号", "凭证字号"));
        head.add(Arrays.asList("摘要", "摘要"));
        head.add(Arrays.asList("发生额", "借方"));
        head.add(Arrays.asList("发生额", "贷方"));
        head.add(Arrays.asList("余额", "方向"));
        head.add(Arrays.asList("余额", "金额"));
        for (FmsLedgerMultiColumnRespVO.Column column : getColumns(result,
                FmsDebitCreditDirectionEnum.DEBIT.getType())) {
            head.add(Arrays.asList(FmsDebitCreditDirectionEnum.DEBIT.getName(),
                    column.getSubjectCode() + "/" + column.getSubjectName()));
        }
        for (FmsLedgerMultiColumnRespVO.Column column : getColumns(result,
                FmsDebitCreditDirectionEnum.CREDIT.getType())) {
            head.add(Arrays.asList(FmsDebitCreditDirectionEnum.CREDIT.getName(),
                    column.getSubjectCode() + "/" + column.getSubjectName()));
        }
        return head;
    }

    /**
     * 构建多栏账动态数据
     *
     * @param result 多栏账结果
     * @return 动态数据
     */
    public static List<List<Object>> buildData(FmsLedgerMultiColumnRespVO result) {
        List<FmsLedgerMultiColumnRespVO.Column> debitColumns = getColumns(
                result, FmsDebitCreditDirectionEnum.DEBIT.getType());
        List<FmsLedgerMultiColumnRespVO.Column> creditColumns = getColumns(
                result, FmsDebitCreditDirectionEnum.CREDIT.getType());
        List<List<Object>> data = new ArrayList<>();
        for (FmsLedgerDetailRespVO row : CollUtil.emptyIfNull(result.getRows())) {
            List<Object> values = new ArrayList<>(Arrays.asList(row.getAccountDate(), row.getVoucherNumber(),
                    row.getDigest(), row.getDebitAmount(), row.getCreditAmount(),
                    row.getBalanceDirection(), row.getBalance()));
            debitColumns.forEach(column -> values.add(row.getColumnAmounts().get(column.getSubjectId())));
            creditColumns.forEach(column -> values.add(row.getColumnAmounts().get(column.getSubjectId())));
            data.add(values);
        }
        return data;
    }

    private static List<FmsLedgerMultiColumnRespVO.Column> getColumns(
            FmsLedgerMultiColumnRespVO result, Integer balanceDirection) {
        return filterList(CollUtil.emptyIfNull(result.getColumns()),
                column -> balanceDirection.equals(column.getBalanceDirection()));
    }

}
