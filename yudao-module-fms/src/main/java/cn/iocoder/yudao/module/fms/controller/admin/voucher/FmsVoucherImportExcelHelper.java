package cn.iocoder.yudao.module.fms.controller.admin.voucher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.voucher.vo.FmsVoucherImportTemplateVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.addComment;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.createHeaderStyle;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.getCellText;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.getDecimal;
import static cn.iocoder.yudao.framework.excel.core.util.PoiExcelUtils.isEmptyRow;

/**
 * FMS 凭证导入 Excel 辅助类
 *
 * @author 芋道源码
 */
@UtilityClass
public final class FmsVoucherImportExcelHelper {

    private static final String DATA_SHEET_NAME = "凭证模板";
    private static final String SUBJECT_SHEET_NAME = "科目数据";
    private static final String AUXILIARY_SHEET_NAME = "辅助核算项目数据";
    private static final List<String> BASE_HEADERS = Arrays.asList(
            "日期", "凭证字", "凭证号", "附件数", "摘要", "科目编码", "科目名称", "借方金额", "贷方金额");

    private static final List<String> REQUIRED_HEADERS = Arrays.asList(
            "日期", "凭证字", "凭证号", "摘要", "科目编码", "借方金额", "贷方金额");

    /**
     * 读取凭证导入文件
     *
     * 按模板表头校验文件合法性，并根据动态辅助核算类别列解析数据
     *
     * @param file 导入文件
     * @param auxiliaryTypes 辅助核算类别数组
     * @return 凭证导入数据数组
     */
    public static List<FmsVoucherImportExcelVO> read(MultipartFile file,
            List<FmsAuxiliaryTypeDO> auxiliaryTypes) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            // 1. 校验模板 sheet 和表头
            Sheet sheet = workbook.getSheet(DATA_SHEET_NAME);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("凭证模板工作表不存在");
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> headerIndexes = getHeaderIndexes(headerRow);
            if (!headerIndexes.keySet().containsAll(REQUIRED_HEADERS)) {
                throw new IllegalArgumentException("凭证模板表头不完整");
            }
            // 2. 构建动态辅助核算类别映射
            Map<String, Long> auxiliaryTypeMap = new LinkedHashMap<>();
            auxiliaryTypes.forEach(type -> auxiliaryTypeMap.put(type.getName(), type.getId()));
            // 3. 逐行解析，跳过空行
            List<FmsVoucherImportExcelVO> rows = new ArrayList<>();
            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isEmptyRow(row, null, headerIndexes.values())) {
                    continue;
                }
                rows.add(parseRow(row, headerIndexes, auxiliaryTypeMap));
            }
            return rows;
        }
    }

    /**
     * 生成凭证导入模板
     *
     * 模板根据账套辅助核算类别动态增加列，并提供凭证字下拉选项和基础数据工作表
     *
     * @param templateData 模板基础数据
     * @return Excel 文件字节数组
     */
    public static byte[] writeTemplate(FmsVoucherImportTemplateVO templateData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            // 1. 构建动态表头和示例数据
            List<String> headers = new ArrayList<>(BASE_HEADERS);
            templateData.getAuxiliaryTypes().forEach(type -> headers.add(type.getName()));
            headers.add("数量");
            headers.add("单价");
            Sheet dataSheet = workbook.createSheet(DATA_SHEET_NAME);
            writeHeader(workbook, dataSheet, headers);
            writeExampleRow(dataSheet, templateData);
            // 2. 增加凭证字下拉选项和填写说明
            addVoucherWordValidation(dataSheet, templateData.getVoucherWords(), headers.indexOf("凭证字"));
            addComment(dataSheet.getRow(0).getCell(headers.indexOf("数量")),
                    "选填，科目启用数量核算时可填写");
            addComment(dataSheet.getRow(0).getCell(headers.indexOf("单价")),
                    "选填，与数量同时填写时，数量乘单价应等于分录金额");
            // 3. 写入科目和辅助核算基础数据
            writeSubjectSheet(workbook, templateData.getSubjects(), templateData.getAuxiliaryTypes());
            writeAuxiliarySheet(workbook, templateData.getAuxiliaryTypes(), templateData.getAuxiliaryItems());
            workbook.write(output);
            return output.toByteArray();
        }
    }

    /**
     * 生成凭证导入错误文件
     *
     * @param errorRows 导入错误数据数组
     * @return Excel 文件字节数组
     */
    public static byte[] writeErrorFile(List<FmsVoucherImportExcelVO> errorRows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            // 1. 收集原始文件表头
            Sheet sheet = workbook.createSheet("错误数据");
            Set<String> sourceHeaders = new LinkedHashSet<>();
            errorRows.forEach(row -> sourceHeaders.addAll(row.getSourceValues().keySet()));
            List<String> headers = new ArrayList<>();
            headers.add("错误信息");
            headers.addAll(sourceHeaders);
            writeHeader(workbook, sheet, headers);
            // 2. 写入错误信息和原始数据
            for (int index = 0; index < errorRows.size(); index++) {
                FmsVoucherImportExcelVO errorRow = errorRows.get(index);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(String.join("；", errorRow.getErrors()));
                int columnIndex = 1;
                for (String header : sourceHeaders) {
                    row.createCell(columnIndex++).setCellValue(
                            StrUtil.nullToEmpty(errorRow.getSourceValues().get(header)));
                }
            }
            setColumnWidths(sheet, headers.size());
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private static FmsVoucherImportExcelVO parseRow(Row row, Map<String, Integer> headerIndexes,
            Map<String, Long> auxiliaryTypeMap) {
        FmsVoucherImportExcelVO result = new FmsVoucherImportExcelVO().setRowNumber(row.getRowNum() + 1);
        headerIndexes.forEach((header, index) -> result.getSourceValues().put(header, getCellText(row, index, null)));
        result.setVoucherTime(getDateTime(row, headerIndexes.get("日期")));
        result.setVoucherWordName(getCellText(row, headerIndexes.get("凭证字"), null));
        result.setVoucherNumber(getInteger(row, headerIndexes.get("凭证号")));
        result.setAttachmentCount(getInteger(row, headerIndexes.get("附件数")));
        result.setDigest(getCellText(row, headerIndexes.get("摘要"), null));
        result.setSubjectCode(getCellText(row, headerIndexes.get("科目编码"), null));
        result.setSubjectName(getCellText(row, headerIndexes.get("科目名称"), null));
        result.setDebitAmount(getDecimalOrNull(row, headerIndexes.get("借方金额")));
        result.setCreditAmount(getDecimalOrNull(row, headerIndexes.get("贷方金额")));
        result.setQuantity(getDecimalOrNull(row, headerIndexes.get("数量")));
        result.setUnitPrice(getDecimalOrNull(row, headerIndexes.get("单价")));
        auxiliaryTypeMap.forEach((name, id) -> {
            if (headerIndexes.containsKey(name)) {
                result.getAuxiliaryCodes().put(id, getCellText(row, headerIndexes.get(name), null));
            }
        });
        return result;
    }

    private static Map<String, Integer> getHeaderIndexes(Row headerRow) {
        if (headerRow == null) {
            return Collections.emptyMap();
        }
        Map<String, Integer> indexes = new LinkedHashMap<>();
        for (Cell cell : headerRow) {
            String header = getCellText(headerRow, cell.getColumnIndex(), null);
            if (StrUtil.isNotBlank(header)) {
                indexes.put(header, cell.getColumnIndex());
            }
        }
        return indexes;
    }

    private static Integer getInteger(Row row, Integer columnIndex) {
        BigDecimal value = getDecimalOrNull(row, columnIndex);
        if (value == null) {
            return null;
        }
        try {
            return value.intValueExact();
        } catch (ArithmeticException ignored) {
            return null;
        }
    }

    private static BigDecimal getDecimalOrNull(Row row, Integer columnIndex) {
        try {
            return getDecimal(row, columnIndex, null);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static LocalDateTime getDateTime(Row row, Integer columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().atStartOfDay();
        }
        String value = getCellText(row, columnIndex, null);
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTimeUtils.parse(value).toLocalDate().atStartOfDay();
        } catch (DateTimeException ignored) {
            return null;
        }
    }

    private static void writeHeader(Workbook workbook, Sheet sheet, List<String> headers) {
        CellStyle style = createHeaderStyle(workbook, IndexedColors.LIGHT_BLUE);
        Row row = sheet.createRow(0);
        for (int index = 0; index < headers.size(); index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(headers.get(index));
            cell.setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
        setColumnWidths(sheet, headers.size());
    }

    private static void writeExampleRow(Sheet sheet, FmsVoucherImportTemplateVO templateData) {
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(LocalDate.now().toString());
        if (CollUtil.isNotEmpty(templateData.getVoucherWords())) {
            row.createCell(1).setCellValue(templateData.getVoucherWords().get(0).getName());
        }
        row.createCell(2).setCellValue(1);
        row.createCell(3).setCellValue(0);
        row.createCell(4).setCellValue("示例摘要，导入前请删除示例行");
    }

    private static void writeSubjectSheet(Workbook workbook, List<FmsSubjectDO> subjects,
            List<FmsAuxiliaryTypeDO> auxiliaryTypes) {
        Sheet sheet = workbook.createSheet(SUBJECT_SHEET_NAME);
        List<String> headers = Arrays.asList("科目编码", "科目名称", "数量核算", "计量单位", "辅助核算");
        writeHeader(workbook, sheet, headers);
        Map<Long, String> auxiliaryTypeNameMap = new LinkedHashMap<>();
        auxiliaryTypes.forEach(type -> auxiliaryTypeNameMap.put(type.getId(), type.getName()));
        for (int index = 0; index < subjects.size(); index++) {
            FmsSubjectDO subject = subjects.get(index);
            Row row = sheet.createRow(index + 1);
            row.createCell(0).setCellValue(subject.getCode());
            row.createCell(1).setCellValue(subject.getName());
            row.createCell(2).setCellValue(Boolean.TRUE.equals(subject.getQuantityAccounting()) ? "是" : "否");
            row.createCell(3).setCellValue(StrUtil.nullToEmpty(subject.getQuantityUnit()));
            List<String> auxiliaryNames = new ArrayList<>();
            if (subject.getAuxiliaryTypeIds() != null) {
                subject.getAuxiliaryTypeIds().forEach(
                        auxiliaryTypeId -> auxiliaryNames.add(auxiliaryTypeNameMap.get(auxiliaryTypeId)));
            }
            row.createCell(4).setCellValue(String.join("、", auxiliaryNames));
        }
        setColumnWidths(sheet, headers.size());
    }

    private static void writeAuxiliarySheet(Workbook workbook, List<FmsAuxiliaryTypeDO> auxiliaryTypes,
            List<FmsAuxiliaryItemDO> auxiliaryItems) {
        Sheet sheet = workbook.createSheet(AUXILIARY_SHEET_NAME);
        List<String> headers = Arrays.asList("辅助核算类别", "编码", "名称");
        writeHeader(workbook, sheet, headers);
        Map<Long, String> typeNameMap = new LinkedHashMap<>();
        auxiliaryTypes.forEach(type -> typeNameMap.put(type.getId(), type.getName()));
        for (int index = 0; index < auxiliaryItems.size(); index++) {
            FmsAuxiliaryItemDO item = auxiliaryItems.get(index);
            Row row = sheet.createRow(index + 1);
            row.createCell(0).setCellValue(StrUtil.nullToEmpty(typeNameMap.get(item.getAuxiliaryTypeId())));
            row.createCell(1).setCellValue(item.getCode());
            row.createCell(2).setCellValue(item.getName());
        }
        setColumnWidths(sheet, headers.size());
    }

    private static void addVoucherWordValidation(Sheet sheet, List<FmsVoucherWordDO> voucherWords,
            int columnIndex) {
        if (CollUtil.isEmpty(voucherWords)) {
            return;
        }
        String[] values = voucherWords.stream().map(FmsVoucherWordDO::getName).toArray(String[]::new);
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        DataValidation validation = helper.createValidation(
                constraint, new CellRangeAddressList(1,
                        sheet.getWorkbook().getSpreadsheetVersion().getLastRowIndex(), columnIndex, columnIndex));
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    private static void setColumnWidths(Sheet sheet, int columnCount) {
        for (int index = 0; index < columnCount; index++) {
            sheet.setColumnWidth(index, index == 4 ? 8_000 : 4_500);
        }
    }

}
