package cn.iocoder.yudao.framework.excel.core.handler;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.Comparator;
import java.util.List;

/**
 * 基于固定 sheet 实现下拉框
 *
 * @author HUIHUI
 */
public class SelectSheetWriteHandler implements SheetWriteHandler {

    private final List<KeyValue<Integer, List<String>>> selectMap;

    private static final char[] ALPHABET = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public SelectSheetWriteHandler(List<KeyValue<Integer, List<String>>> selectMap) {
        selectMap.sort(Comparator.comparing(item -> item.getValue().size())); // 升序不然创建下拉会报错
        this.selectMap = selectMap;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (selectMap == null || CollUtil.isEmpty(selectMap)) {
            return;
        }
        // 需要设置下拉框的sheet页
        Sheet curSheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = curSheet.getDataValidationHelper();
        String dictSheetName = "字典sheet";
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        // 数据字典的sheet页
        Sheet dictSheet = workbook.createSheet(dictSheetName);
        for (KeyValue<Integer, List<String>> keyValue : selectMap) {
            // 设置下拉单元格的首行、末行、首列、末列
            CellRangeAddressList rangeAddressList = new CellRangeAddressList(1, 65533, keyValue.getKey(), keyValue.getKey());
            int rowLen = keyValue.getValue().size();
            // 设置字典sheet页的值 每一列一个字典项
            for (int i = 0; i < rowLen; i++) {
                Row row = dictSheet.getRow(i);
                if (row == null) {
                    row = dictSheet.createRow(i);
                }
                row.createCell(keyValue.getKey()).setCellValue(keyValue.getValue().get(i));
            }
            String excelColumn = getExcelColumn(keyValue.getKey());
            // 下拉框数据来源 eg:字典sheet!$B1:$B2
            String refers = dictSheetName + "!$" + excelColumn + "$1:$" + excelColumn + "$" + rowLen;
            // 创建可被其他单元格引用的名称
            Name name = workbook.createName();
            // 设置名称的名字
            name.setNameName("dict" + keyValue.getKey());
            // 设置公式
            name.setRefersToFormula(refers);
            // 设置引用约束
            DataValidationConstraint constraint = helper.createFormulaListConstraint("dict" + keyValue.getKey());
            // 设置约束
            DataValidation validation = helper.createValidation(constraint, rangeAddressList);
            if (validation instanceof HSSFDataValidation) {
                validation.setSuppressDropDownArrow(false);
            } else {
                validation.setSuppressDropDownArrow(true);
                validation.setShowErrorBox(true);
            }
            // 阻止输入非下拉框的值
            validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            validation.createErrorBox("提示", "此值不存在于下拉选择中！");
            // 添加下拉框约束
            writeSheetHolder.getSheet().addValidationData(validation);
        }
    }

    /**
     * 将数字列转化成为字母列
     *
     * @param num 数字
     * @return 字母
     */
    private String getExcelColumn(int num) {
        String column = "";
        int len = ALPHABET.length - 1;
        int first = num / len;
        int second = num % len;
        if (num <= len) {
            column = ALPHABET[num] + "";
        } else {
            column = ALPHABET[first - 1] + "";
            if (second == 0) {
                column = column + ALPHABET[len] + "";
            } else {
                column = column + ALPHABET[second - 1] + "";
            }
        }
        return column;
    }

}