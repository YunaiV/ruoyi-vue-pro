package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceRespVO;
import cn.iocoder.yudao.module.fms.enums.common.FmsDebitCreditDirectionEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsSubjectTypeEnum;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.initialbalance.FmsInitialBalanceExcelVO;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.number.NumberUtils.zeroIfNull;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.createDataStyle;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.createHeaderStyle;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.createNumberStyle;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.getCellText;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.getDecimal;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.isEmptyRow;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.mergeHeader;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.writeFormula;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.writeNumber;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.writeText;

/**
 * FMS 初始余额 Excel 工具
 *
 * @author 芋道源码
 */
@UtilityClass
public final class FmsInitialBalanceExcelHelper {

    private static final String TEMPLATE_SHEET_NAME = "初始余额模板";

    /**
     * 展开初始余额平铺列表为 Excel 行
     *
     * 父级科目仅在 includeParent 时输出行；启用辅助核算且存在明细的科目按明细逐行输出
     */
    public static List<FmsInitialBalanceExcelVO> toRows(List<FmsInitialBalanceRespVO> results,
            boolean includeParent) {
        Set<Long> parentIds = convertSet(results, FmsInitialBalanceRespVO::getParentId);
        List<FmsInitialBalanceExcelVO> rows = new ArrayList<>();
        results.forEach(result -> {
            if (parentIds.contains(result.getSubjectId())) {
                if (includeParent) {
                    rows.add(buildRow(result, null));
                }
                return;
            }
            if (Boolean.FALSE.equals(result.getAuxiliaryAccounting())
                    || CollUtil.isEmpty(result.getAssistBalances())) {
                rows.add(buildRow(result, null));
                return;
            }
            result.getAssistBalances().forEach(assist -> rows.add(buildRow(result, assist)));
        });
        return rows;
    }

    /**
     * 构建单个科目或辅助核算明细对应的 Excel 行
     */
    private static FmsInitialBalanceExcelVO buildRow(FmsInitialBalanceRespVO result,
                                                     FmsInitialBalanceRespVO.AssistBalance assist) {
        FmsInitialBalanceExcelVO row = new FmsInitialBalanceExcelVO()
                .setSubjectCode(result.getSubjectCode()).setSubjectName(result.getSubjectName())
                .setDirectionName(FmsDebitCreditDirectionEnum.valueOf(result.getBalanceDirection()).getName())
                .setSubjectType(result.getType()).setQuantityAccounting(result.getQuantityAccounting())
                .setAuxiliaryAccounting(result.getAuxiliaryAccounting());
        if (assist == null) {
            copyAmounts(result, row);
        } else {
            // 辅助核算项目拼接为“类别:名称/类别:名称”，与导入解析的格式对应
            Map<Long, String> auxiliaryTypeNameMap = convertMap(result.getAuxiliaryConfigs(),
                    FmsInitialBalanceRespVO.AuxiliaryConfig::getAuxiliaryTypeId,
                    FmsInitialBalanceRespVO.AuxiliaryConfig::getName);
            row.setAuxiliaryItems(String.join("/", convertList(assist.getAuxiliaries(),
                    item -> auxiliaryTypeNameMap.get(item.getTypeId()) + ":" + item.getName())));
            copyAmounts(assist, row);
        }
        return row;
    }

    private static void copyAmounts(FmsInitialBalanceRespVO source, FmsInitialBalanceExcelVO target) {
        target.setOpeningAmount(zeroIfNull(source.getOpeningAmount()))
                .setOpeningQuantity(zeroIfNull(source.getOpeningQuantity()))
                .setYearDebitAmount(zeroIfNull(source.getYearDebitAmount()))
                .setYearDebitQuantity(zeroIfNull(source.getYearDebitQuantity()))
                .setYearCreditAmount(zeroIfNull(source.getYearCreditAmount()))
                .setYearCreditQuantity(zeroIfNull(source.getYearCreditQuantity()))
                .setYearOpeningAmount(zeroIfNull(source.getYearOpeningAmount()))
                .setYearOpeningQuantity(zeroIfNull(source.getYearOpeningQuantity()))
                .setProfitLossAmount(zeroIfNull(source.getProfitLossAmount()))
                .setProfitLossQuantity(zeroIfNull(source.getProfitLossQuantity()));
    }

    private static void copyAmounts(FmsInitialBalanceRespVO.AssistBalance source,
            FmsInitialBalanceExcelVO target) {
        target.setOpeningAmount(zeroIfNull(source.getOpeningAmount()))
                .setOpeningQuantity(zeroIfNull(source.getOpeningQuantity()))
                .setYearDebitAmount(zeroIfNull(source.getYearDebitAmount()))
                .setYearDebitQuantity(zeroIfNull(source.getYearDebitQuantity()))
                .setYearCreditAmount(zeroIfNull(source.getYearCreditAmount()))
                .setYearCreditQuantity(zeroIfNull(source.getYearCreditQuantity()))
                .setYearOpeningAmount(zeroIfNull(source.getYearOpeningAmount()))
                .setYearOpeningQuantity(zeroIfNull(source.getYearOpeningQuantity()))
                .setProfitLossAmount(zeroIfNull(source.getProfitLossAmount()))
                .setProfitLossQuantity(zeroIfNull(source.getProfitLossQuantity()));
    }

    /**
     * 生成导入模板
     *
     * 模板锁定系统生成列，年初余额列写入公式，并通过 protectSheet 防止误改
     */
    public static byte[] writeTemplate(List<FmsInitialBalanceExcelVO> rows,
            boolean january) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            // 1. 写入表头，一月启用时只保留期初余额
            Sheet sheet = workbook.createSheet(TEMPLATE_SHEET_NAME);
            writeHeaders(workbook, sheet, 0, january);
            // 2. 写入科目数据，系统生成列锁定，可填写列解锁
            CellStyle lockedStyle = createDataStyle(workbook, true, IndexedColors.GREY_25_PERCENT);
            CellStyle editableStyle = createDataStyle(workbook, false, IndexedColors.WHITE);
            CellStyle numberLockedStyle = createNumberStyle(workbook, lockedStyle, "#,##0.0000");
            CellStyle amountLockedStyle = createNumberStyle(workbook, lockedStyle, "#,##0.00");
            CellStyle numberEditableStyle = createNumberStyle(workbook, editableStyle, "#,##0.0000");
            CellStyle amountEditableStyle = createNumberStyle(workbook, editableStyle, "#,##0.00");
            for (int index = 0; index < rows.size(); index++) {
                writeDataRow(sheet.createRow(index + 2), rows.get(index), january, true,
                        lockedStyle, editableStyle, numberLockedStyle, amountLockedStyle,
                        numberEditableStyle, amountEditableStyle);
            }
            // 3. 保护工作表并补充填写说明
            sheet.protectSheet("yudao-fms");
            sheet.createFreezePane(4, 2);
            writeInstructions(workbook);
            workbook.write(output);
            return output.toByteArray();
        }
    }

    /**
     * 读取导入文件
     *
     * 按模板表头校验文件合法性，年初余额等公式单元格取公式计算值；不限制导入行数
     */
    public static List<FmsInitialBalanceExcelVO> read(MultipartFile file) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // 1. 校验模板 sheet 和表头
            Sheet sheet = workbook.getSheet(TEMPLATE_SHEET_NAME);
            if (sheet == null || !hasValidHeaders(sheet)) {
                throw new IllegalArgumentException("初始余额模板表头不正确");
            }
            // 2. 逐行解析，跳过空行
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            boolean january = sheet.getRow(1).getLastCellNum() <= 6;
            List<FmsInitialBalanceExcelVO> rows = new ArrayList<>();
            for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isEmptyRow(row, evaluator, Arrays.asList(0, 1, 3, 4, 5))) {
                    continue;
                }
                rows.add(parseRow(row, january, evaluator));
            }
            return rows;
        }
    }

    /**
     * 写入双行表头，数量和金额成组；一月启用时只保留期初余额组
     */
    private static void writeHeaders(Workbook workbook, Sheet sheet, int rowIndex, boolean january) {
        CellStyle style = createHeaderStyle(workbook, IndexedColors.GREY_25_PERCENT);
        int lastColumn = january ? 5 : 13;
        Row firstRow = sheet.createRow(rowIndex);
        Row secondRow = sheet.createRow(rowIndex + 1);
        mergeHeader(sheet, firstRow, secondRow, 0, 0, "科目编码", style);
        mergeHeader(sheet, firstRow, secondRow, 1, 1, "科目名称", style);
        mergeHeader(sheet, firstRow, secondRow, 2, 2, "方向", style);
        mergeHeader(sheet, firstRow, secondRow, 3, 3, "辅助核算项目", style);
        writeGroupHeader(sheet, firstRow, secondRow, 4, "期初余额", style);
        if (!january) {
            writeGroupHeader(sheet, firstRow, secondRow, 6, "本年累计借方", style);
            writeGroupHeader(sheet, firstRow, secondRow, 8, "本年累计贷方", style);
            writeGroupHeader(sheet, firstRow, secondRow, 10, "年初余额", style);
            writeGroupHeader(sheet, firstRow, secondRow, 12, "实际损益发生额", style);
        }
        for (int column = 0; column <= lastColumn; column++) {
            sheet.setColumnWidth(column, column == 1 || column == 3 ? 7_000 : 4_000);
        }
        firstRow.setHeightInPoints(28);
        secondRow.setHeightInPoints(24);
    }

    /**
     * 写入“数量/金额”成组表头，第一行合并组名，第二行拆出数量和金额
     */
    private static void writeGroupHeader(Sheet sheet, Row firstRow, Row secondRow,
                                         int beginColumn, String title, CellStyle style) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), firstRow.getRowNum(), beginColumn, beginColumn + 1));
        Cell groupCell = firstRow.createCell(beginColumn);
        groupCell.setCellValue(title);
        groupCell.setCellStyle(style);
        firstRow.createCell(beginColumn + 1).setCellStyle(style);
        Cell quantityCell = secondRow.createCell(beginColumn);
        quantityCell.setCellValue("数量");
        quantityCell.setCellStyle(style);
        Cell amountCell = secondRow.createCell(beginColumn + 1);
        amountCell.setCellValue("金额");
        amountCell.setCellStyle(style);
    }

    /**
     * 写入一行数据
     *
     * 模板模式下按科目配置解锁可填写单元格（数量列仅数量核算科目可填，损益列仅损益类科目可填），
     * 年初余额列写入“期初 ± 累计借贷”公式
     */
    private static void writeDataRow(Row excelRow, FmsInitialBalanceExcelVO data,
                                     boolean january, boolean template, CellStyle lockedStyle, CellStyle editableStyle,
                                     CellStyle numberLockedStyle, CellStyle amountLockedStyle,
                                     CellStyle numberEditableStyle, CellStyle amountEditableStyle) {
        writeText(excelRow, 0, data.getSubjectCode(), lockedStyle);
        writeText(excelRow, 1, data.getSubjectName(), lockedStyle);
        writeText(excelRow, 2, data.getDirectionName(), lockedStyle);
        writeText(excelRow, 3, data.getAuxiliaryItems(),
                template && Boolean.TRUE.equals(data.getAuxiliaryAccounting()) ? editableStyle : lockedStyle);
        writeNumber(excelRow, 4, data.getOpeningQuantity(),
                template && Boolean.TRUE.equals(data.getQuantityAccounting())
                        ? numberEditableStyle : numberLockedStyle);
        writeNumber(excelRow, 5, data.getOpeningAmount(), template ? amountEditableStyle : amountLockedStyle);
        if (january) {
            return;
        }
        writeNumber(excelRow, 6, data.getYearDebitQuantity(),
                template && Boolean.TRUE.equals(data.getQuantityAccounting())
                        ? numberEditableStyle : numberLockedStyle);
        writeNumber(excelRow, 7, data.getYearDebitAmount(), template ? amountEditableStyle : amountLockedStyle);
        writeNumber(excelRow, 8, data.getYearCreditQuantity(),
                template && Boolean.TRUE.equals(data.getQuantityAccounting())
                        ? numberEditableStyle : numberLockedStyle);
        writeNumber(excelRow, 9, data.getYearCreditAmount(), template ? amountEditableStyle : amountLockedStyle);
        if (template) {
            String quantityFormula = "借".equals(data.getDirectionName())
                    ? "E" + (excelRow.getRowNum() + 1) + "-G" + (excelRow.getRowNum() + 1)
                    + "+I" + (excelRow.getRowNum() + 1)
                    : "E" + (excelRow.getRowNum() + 1) + "+G" + (excelRow.getRowNum() + 1)
                    + "-I" + (excelRow.getRowNum() + 1);
            String amountFormula = "借".equals(data.getDirectionName())
                    ? "F" + (excelRow.getRowNum() + 1) + "-H" + (excelRow.getRowNum() + 1)
                    + "+J" + (excelRow.getRowNum() + 1)
                    : "F" + (excelRow.getRowNum() + 1) + "+H" + (excelRow.getRowNum() + 1)
                    + "-J" + (excelRow.getRowNum() + 1);
            writeFormula(excelRow, 10, quantityFormula, numberLockedStyle);
            writeFormula(excelRow, 11, amountFormula, amountLockedStyle);
        } else {
            writeNumber(excelRow, 10, data.getYearOpeningQuantity(), numberLockedStyle);
            writeNumber(excelRow, 11, data.getYearOpeningAmount(), amountLockedStyle);
        }
        boolean profitLoss = FmsSubjectTypeEnum.PROFIT_LOSS.getType().equals(data.getSubjectType());
        writeNumber(excelRow, 12, profitLoss ? data.getProfitLossQuantity() : null,
                template && profitLoss && Boolean.TRUE.equals(data.getQuantityAccounting())
                        ? numberEditableStyle : numberLockedStyle);
        writeNumber(excelRow, 13, profitLoss ? data.getProfitLossAmount() : null,
                template && profitLoss ? amountEditableStyle : amountLockedStyle);
    }

    /**
     * 解析一行数据，公式单元格通过 evaluator 取计算值
     */
    private static FmsInitialBalanceExcelVO parseRow(Row row, boolean january, FormulaEvaluator evaluator) {
        FmsInitialBalanceExcelVO result = new FmsInitialBalanceExcelVO()
                .setRowNumber(row.getRowNum() + 1)
                .setSubjectCode(getCellText(row, 0, evaluator))
                .setSubjectName(getCellText(row, 1, evaluator))
                .setDirectionName(getCellText(row, 2, evaluator))
                .setAuxiliaryItems(getCellText(row, 3, evaluator))
                .setOpeningQuantity(getDecimal(row, 4, evaluator))
                .setOpeningAmount(getDecimal(row, 5, evaluator));
        if (!january) {
            result.setYearDebitQuantity(getDecimal(row, 6, evaluator))
                    .setYearDebitAmount(getDecimal(row, 7, evaluator))
                    .setYearCreditQuantity(getDecimal(row, 8, evaluator))
                    .setYearCreditAmount(getDecimal(row, 9, evaluator))
                    .setYearOpeningQuantity(getDecimal(row, 10, evaluator))
                    .setYearOpeningAmount(getDecimal(row, 11, evaluator))
                    .setProfitLossQuantity(getDecimal(row, 12, evaluator))
                    .setProfitLossAmount(getDecimal(row, 13, evaluator));
        }
        return result;
    }

    private static boolean hasValidHeaders(Sheet sheet) {
        Row firstRow = sheet.getRow(0);
        Row secondRow = sheet.getRow(1);
        return firstRow != null && secondRow != null
                && "科目编码".equals(getCellText(firstRow, 0, null))
                && "科目名称".equals(getCellText(firstRow, 1, null))
                && "方向".equals(getCellText(firstRow, 2, null))
                && "辅助核算项目".equals(getCellText(firstRow, 3, null))
                && "期初余额".equals(getCellText(firstRow, 4, null))
                && "数量".equals(getCellText(secondRow, 4, null))
                && "金额".equals(getCellText(secondRow, 5, null));
    }

    private static void writeInstructions(Workbook workbook) {
        Sheet sheet = workbook.createSheet("填写说明");
        String[] instructions = {
                "1. 灰色单元格由系统生成，请勿修改",
                "2. 仅末级科目可以导入初始余额",
                "3. 辅助核算项目使用“类别:名称/类别:名称”格式，例如“客户:北京客户/部门:销售部”",
                "4. 数量核算科目可填写数量，金额和数量不能小于 0",
                "5. 年初余额由期初余额和本年累计发生额自动计算"
        };
        for (int index = 0; index < instructions.length; index++) {
            sheet.createRow(index).createCell(0).setCellValue(instructions[index]);
        }
        sheet.setColumnWidth(0, 20_000);
    }

}
