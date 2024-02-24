package cn.iocoder.yudao.framework.excel.core.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.excel.core.enums.ExcelColumn;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于固定 sheet 实现下拉框
 *
 * @author HUIHUI
 */
public class SelectSheetWriteHandler implements SheetWriteHandler {

    /**
     * 数据起始行从 0 开始
     *
     * 约定：本项目第一行有标题所以从 1 开始如果您的 Excel 有多行标题请自行更改
     */
    public static final int FIRST_ROW = 1;
    /**
     * 下拉列需要创建下拉框的行数，默认两千行如需更多请自行调整
     */
    public static final int LAST_ROW = 2000;

    private static final String DICT_SHEET_NAME = "字典sheet";

    // TODO @puhui999：Map<ExcelColumn, List<String>> 可以么？之前用 keyvalue 的原因，返回给前端，无法用 linkedhashmap，默认 key 会乱序
    private final List<KeyValue<ExcelColumn, List<String>>> selectMap;

    public SelectSheetWriteHandler(List<KeyValue<ExcelColumn, List<String>>> selectMap) {
        if (CollUtil.isEmpty(selectMap)) {
            this.selectMap = null;
            return;
        }
        // 校验一下 key 是否唯一
        Map<String, Long> nameCounts = selectMap.stream()
                .collect(Collectors.groupingBy(item -> item.getKey().name(), Collectors.counting()));
        Assert.isFalse(nameCounts.entrySet().stream().allMatch(entry -> entry.getValue() > 1), "下拉数据 key 重复请排查！！！");

        selectMap.sort(Comparator.comparing(item -> item.getValue().size())); // 升序不然创建下拉会报错
        this.selectMap = selectMap;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (CollUtil.isEmpty(selectMap)) {
            return;
        }

        // 1. 获取相应操作对象
        DataValidationHelper helper = writeSheetHolder.getSheet().getDataValidationHelper(); // 需要设置下拉框的 sheet 页的数据验证助手
        Workbook workbook = writeWorkbookHolder.getWorkbook(); // 获得工作簿

        // 2. 创建数据字典的 sheet 页
        Sheet dictSheet = workbook.createSheet(DICT_SHEET_NAME);
        for (KeyValue<ExcelColumn, List<String>> keyValue : selectMap) {
            int rowLength = keyValue.getValue().size();
            // 2.1 设置字典 sheet 页的值 每一列一个字典项
            for (int i = 0; i < rowLength; i++) {
                Row row = dictSheet.getRow(i);
                if (row == null) {
                    row = dictSheet.createRow(i);
                }
                row.createCell(keyValue.getKey().getColNum()).setCellValue(keyValue.getValue().get(i));
            }
            // 2.2 设置单元格下拉选择
            setColumnSelect(writeSheetHolder, workbook, helper, keyValue);
        }
    }

    /**
     * 设置单元格下拉选择
     */
    private static void setColumnSelect(WriteSheetHolder writeSheetHolder, Workbook workbook, DataValidationHelper helper,
                                        KeyValue<ExcelColumn, List<String>> keyValue) {
        // 1.1 创建可被其他单元格引用的名称
        Name name = workbook.createName();
        String excelColumn = keyValue.getKey().name();
        // 1.2 下拉框数据来源 eg:字典sheet!$B1:$B2
        String refers = DICT_SHEET_NAME + "!$" + excelColumn + "$1:$" + excelColumn + "$" + keyValue.getValue().size();
        name.setNameName("dict" + keyValue.getKey()); // 设置名称的名字
        name.setRefersToFormula(refers); // 设置公式

        // 2.1 设置约束
        DataValidationConstraint constraint = helper.createFormulaListConstraint("dict" + keyValue.getKey()); // 设置引用约束
        // 设置下拉单元格的首行、末行、首列、末列
        CellRangeAddressList rangeAddressList = new CellRangeAddressList(FIRST_ROW, LAST_ROW,
                keyValue.getKey().getColNum(), keyValue.getKey().getColNum());
        DataValidation validation = helper.createValidation(constraint, rangeAddressList);
        if (validation instanceof HSSFDataValidation) {
            validation.setSuppressDropDownArrow(false);
        } else {
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
        }
        // 2.2 阻止输入非下拉框的值
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("提示", "此值不存在于下拉选择中！");
        // 2.3 添加下拉框约束
        writeSheetHolder.getSheet().addValidationData(validation);
    }

}