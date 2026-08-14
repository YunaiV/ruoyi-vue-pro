package cn.iocoder.yudao.framework.excel.core.util;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * 原生 POI 的 Excel 工具类
 *
 * 全局 {@link ExcelUtils}（FastExcel）无法表达单元格锁定、公式、动态列等复杂模板时，各模块基于本类实现自定义 Excel 读写
 *
 * @author 芋道源码
 */
@UtilityClass
public class PoiExcelUtils {

    /**
     * 读取公式计算值必须 DataFormatter + FormulaEvaluator，hutool 没有更简的等价写法
     */
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    // ==================== 读取 ====================

    /**
     * 读取单元格文本，公式单元格取公式计算值
     *
     * @param row 行
     * @param column 列
     * @param evaluator 公式计算器，不需要求值时传 null
     * @return 文本，空单元格返回空字符串
     */
    public static String getCellText(Row row, Integer column, FormulaEvaluator evaluator) {
        if (row == null || column == null || row.getCell(column) == null) {
            return "";
        }
        return evaluator == null
                ? DATA_FORMATTER.formatCellValue(row.getCell(column))
                : DATA_FORMATTER.formatCellValue(row.getCell(column), evaluator);
    }

    /**
     * 读取单元格数字，兼容千分位文本
     *
     * @param row 行
     * @param column 列
     * @param evaluator 公式计算器，不需要求值时传 null
     * @return 数字，空单元格返回 null；格式不正确时抛出 {@link NumberFormatException}
     */
    public static BigDecimal getDecimal(Row row, Integer column, FormulaEvaluator evaluator) {
        String value = getCellText(row, column, evaluator).replace(",", "");
        return StrUtil.isBlank(value) ? null : NumberUtil.toBigDecimal(value);
    }

    /**
     * 判断指定列是否全部为空
     *
     * @param row 行
     * @param evaluator 公式计算器，不需要求值时传 null
     * @param columns 列数组
     * @return 是否全部为空
     */
    public static boolean isEmptyRow(Row row, FormulaEvaluator evaluator, Collection<Integer> columns) {
        if (row == null) {
            return true;
        }
        for (Integer column : columns) {
            if (StrUtil.isNotBlank(getCellText(row, column, evaluator))) {
                return false;
            }
        }
        return true;
    }

    // ==================== 写入 ====================

    /**
     * 写入文本单元格，null 按空字符串处理
     */
    public static void writeText(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(StrUtil.nullToEmpty(value));
        cell.setCellStyle(style);
    }

    /**
     * 写入数字单元格，null 时留空
     */
    public static void writeNumber(Row row, int column, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        cell.setCellStyle(style);
    }

    /**
     * 写入公式单元格
     */
    public static void writeFormula(Row row, int column, String formula, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellFormula(formula);
        cell.setCellStyle(style);
    }

    /**
     * 写入跨两行的合并表头
     */
    public static void mergeHeader(Sheet sheet, Row firstRow, Row secondRow,
                                   int beginColumn, int endColumn, String title, CellStyle style) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow.getRowNum(), secondRow.getRowNum(),
                beginColumn, endColumn));
        Cell cell = firstRow.createCell(beginColumn);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        secondRow.createCell(beginColumn).setCellStyle(style);
    }

    /**
     * 添加单元格批注
     *
     * @param cell 单元格
     * @param text 批注内容
     */
    public static void addComment(Cell cell, String text) {
        CreationHelper helper = cell.getSheet().getWorkbook().getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 3);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 3);
        Comment comment = cell.getSheet().createDrawingPatriarch().createCellComment(anchor);
        comment.setString(helper.createRichTextString(text));
        cell.setCellComment(comment);
    }

    // ==================== 样式 ====================

    /**
     * 创建细边框样式
     */
    public static CellStyle createBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    /**
     * 创建数据单元格样式：细边框、垂直居中，可指定锁定和填充色
     *
     * @param workbook 工作簿
     * @param locked 是否锁定，配合 protectSheet 使用
     * @param fillColor 填充色，传 null 时不填充
     */
    public static CellStyle createDataStyle(Workbook workbook, boolean locked, IndexedColors fillColor) {
        CellStyle style = createBorderStyle(workbook);
        style.setLocked(locked);
        if (fillColor != null) {
            style.setFillForegroundColor(fillColor.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 基于已有样式创建数字格式样式
     */
    public static CellStyle createNumberStyle(Workbook workbook, CellStyle base, String format) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(workbook.createDataFormat().getFormat(format));
        return style;
    }

    /**
     * 创建表头样式：细边框、指定填充色、加粗居中
     */
    public static CellStyle createHeaderStyle(Workbook workbook, IndexedColors fillColor) {
        CellStyle style = createBorderStyle(workbook);
        style.setFillForegroundColor(fillColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * 创建标题样式：大号加粗居中
     */
    public static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 20);
        style.setFont(font);
        return style;
    }

}
