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

    private static final String DICT_SHEET_NAME = "字典sheet";

    // TODO @puhui999：key 不使用 int 值么？感觉不是很优雅哈。
    private final List<KeyValue<Integer, List<String>>> selectMap;

    private static final char[] ALPHABET = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public SelectSheetWriteHandler(List<KeyValue<Integer, List<String>>> selectMap) {
        if (CollUtil.isEmpty(selectMap)) {
            this.selectMap = null;
            return;
        }
        selectMap.sort(Comparator.comparing(item -> item.getValue().size())); // 升序不然创建下拉会报错
        this.selectMap = selectMap;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        if (CollUtil.isEmpty(selectMap)) {
            return;
        }
        // 需要设置下拉框的 sheet 页
        Sheet currentSheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = currentSheet.getDataValidationHelper();
        Workbook workbook = writeWorkbookHolder.getWorkbook();

        // 数据字典的 sheet 页
        Sheet dictSheet = workbook.createSheet(DICT_SHEET_NAME);
        for (KeyValue<Integer, List<String>> keyValue : selectMap) {
            // 设置下拉单元格的首行、末行、首列、末列
            CellRangeAddressList rangeAddressList = new CellRangeAddressList(1, 65533, keyValue.getKey(), keyValue.getKey());
            int rowLen = keyValue.getValue().size();
            // 设置字典 sheet 页的值 每一列一个字典项
            for (int i = 0; i < rowLen; i++) {
                Row row = dictSheet.getRow(i);
                if (row == null) {
                    row = dictSheet.createRow(i);
                }
                row.createCell(keyValue.getKey()).setCellValue(keyValue.getValue().get(i));
            }

            // TODO @puhui999：下面 1. 2.1 2.2 2.3 我是按照已经理解的，调整了下格式；这样可读性更好；在 52 到 62 行，你可以看看，是不是也弄下序号；
            // 1. 创建可被其他单元格引用的名称
            Name name = workbook.createName();
            // TODO @puhui999：下面的 excelColumn 和 refers 两行，是不是可以封装成一个方法，替代 getExcelColumn；
            String excelColumn = getExcelColumn(keyValue.getKey());
            String refers = DICT_SHEET_NAME + "!$" + excelColumn + "$1:$" + excelColumn + "$" + rowLen; // 下拉框数据来源 eg:字典sheet!$B1:$B2
            name.setNameName("dict" + keyValue.getKey()); // 设置名称的名字
            name.setRefersToFormula(refers); // 设置公式

            // 2.1 设置约束
            DataValidationConstraint constraint = helper.createFormulaListConstraint("dict" + keyValue.getKey()); // 设置引用约束
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

    /**
     * 将数字列转化成为字母列
     *
     * @param num 数字
     * @return 字母
     */
    // TODO @puhui999：这个是必须字母列哇？还是数字其实也可以哈？主要想看看，怎么能把这个逻辑，进一步简化
    private String getExcelColumn(int num) {
        String column;
        int len = ALPHABET.length - 1;
        int first = num / len;
        int second = num % len;
        if (num <= len) {
            column = ALPHABET[num] + "";
        } else {
            column = ALPHABET[first - 1] + "";
            if (second == 0) {
                column = column + ALPHABET[len];
            } else {
                column = column + ALPHABET[second - 1];
            }
        }
        return column;
    }

}