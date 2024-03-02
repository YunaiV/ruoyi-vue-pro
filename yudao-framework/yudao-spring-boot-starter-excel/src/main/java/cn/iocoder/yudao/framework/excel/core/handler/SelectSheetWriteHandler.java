package cn.iocoder.yudao.framework.excel.core.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.iocoder.yudao.framework.common.core.KeyValue;
import cn.iocoder.yudao.framework.excel.core.annotations.ExcelColumnSelect;
import cn.iocoder.yudao.framework.excel.core.service.ExcelColumnSelectDataService;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 基于固定 sheet 实现下拉框
 *
 * @author HUIHUI
 */
public class SelectSheetWriteHandler implements SheetWriteHandler {

    // TODO @puhui999：注释哈；
    private static final List<String> ALPHABET = getExcelColumnNameList();

    private static final List<ExcelColumnSelectDataService> EXCEL_COLUMN_SELECT_DATA_SERVICES = new ArrayList<>();

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

    // TODO @puhui999：这个 selectMap 可以改成 Map<Integer, List<String>>，然后在 afterSheetCreate 里面进行排序。这样整体的理解成本会更简单
    private final List<KeyValue<Integer, List<String>>> selectMap = new ArrayList<>(); // 使用 List + KeyValue 组合方便排序

    public SelectSheetWriteHandler(Class<?> head) {
        // 加载下拉数据获取接口
        Map<String, ExcelColumnSelectDataService> beansMap = SpringUtil.getBeanFactory().getBeansOfType(ExcelColumnSelectDataService.class);
        if (MapUtil.isEmpty(beansMap)) {
            return;
        }
        // TODO @puhui999：static 是共享。如果并发情况下，会不会存在问题？         其实 EXCEL_COLUMN_SELECT_DATA_SERVICES 不用全局声明，直接 按照 ExcelColumnSelect 去拿下就好了；
        if (CollUtil.isEmpty(EXCEL_COLUMN_SELECT_DATA_SERVICES) || EXCEL_COLUMN_SELECT_DATA_SERVICES.size() != beansMap.values().size()) {
            EXCEL_COLUMN_SELECT_DATA_SERVICES.clear();
            EXCEL_COLUMN_SELECT_DATA_SERVICES.addAll(convertList(beansMap.values(), b -> b));
        }

        // 解析下拉数据
        // TODO @puhui999：感觉可以 head 循环 field，如果有 ExcelColumnSelect 则进行处理；而 ExcelProperty 可能是非必须的
        Map<String, Field> excelPropertyFields = getFieldsWithAnnotation(head, ExcelProperty.class);
        Map<String, Field> excelColumnSelectFields = getFieldsWithAnnotation(head, ExcelColumnSelect.class);
        int colIndex = 0;
        for (String fieldName : excelPropertyFields.keySet()) {
            Field field = excelColumnSelectFields.get(fieldName);
            if (field != null) {
                getSelectDataList(colIndex, field);
            }
            colIndex++;
        }
        selectMap.sort(Comparator.comparing(item -> item.getValue().size())); // 升序不然创建下拉会报错
    }

    /**
     * 获得下拉数据
     *
     * @param colIndex 列索引
     * @param field    字段
     */
    private void getSelectDataList(int colIndex, Field field) {
        EXCEL_COLUMN_SELECT_DATA_SERVICES.forEach(selectDataService -> {
            List<String> stringList = selectDataService.handle(field.getAnnotation(ExcelColumnSelect.class).value());
            if (CollUtil.isEmpty(stringList)) {
                return;
            }
            selectMap.add(new KeyValue<>(colIndex, stringList));
        });
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
        for (KeyValue<Integer, List<String>> keyValue : selectMap) {
            int rowLength = keyValue.getValue().size();
            // 2.1 设置字典 sheet 页的值 每一列一个字典项
            for (int i = 0; i < rowLength; i++) {
                Row row = dictSheet.getRow(i);
                if (row == null) {
                    row = dictSheet.createRow(i);
                }
                row.createCell(keyValue.getKey()).setCellValue(keyValue.getValue().get(i));
            }
            // 2.2 设置单元格下拉选择
            setColumnSelect(writeSheetHolder, workbook, helper, keyValue);
        }
    }

    /**
     * 设置单元格下拉选择
     */
    private static void setColumnSelect(WriteSheetHolder writeSheetHolder, Workbook workbook, DataValidationHelper helper,
                                        KeyValue<Integer, List<String>> keyValue) {
        // 1.1 创建可被其他单元格引用的名称
        Name name = workbook.createName();
        String excelColumn = ALPHABET.get(keyValue.getKey());
        // 1.2 下拉框数据来源 eg:字典sheet!$B1:$B2
        String refers = DICT_SHEET_NAME + "!$" + excelColumn + "$1:$" + excelColumn + "$" + keyValue.getValue().size();
        name.setNameName("dict" + keyValue.getKey()); // 设置名称的名字
        name.setRefersToFormula(refers); // 设置公式

        // 2.1 设置约束
        DataValidationConstraint constraint = helper.createFormulaListConstraint("dict" + keyValue.getKey()); // 设置引用约束
        // 设置下拉单元格的首行、末行、首列、末列
        CellRangeAddressList rangeAddressList = new CellRangeAddressList(FIRST_ROW, LAST_ROW,
                keyValue.getKey(), keyValue.getKey());
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

    public static Map<String, Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Map<String, Field> annotatedFields = new LinkedHashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotationClass)) {
                annotatedFields.put(field.getName(), field);
            }
        }
        return annotatedFields;
    }


    private static List<String> getExcelColumnNameList() {
        // TODO @puhui999：是不是可以使用 ExcelUtil.indexToColName() 替代
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 1; i <= 52; i++) { // 生成 52 列名称，需要更多请重写此方法
            if (i <= 26) {
                strings.add(String.valueOf((char) ('A' + i - 1))); // 使用 ASCII 码值转字母
            } else {
                strings.add(String.valueOf((char) ('A' + (i - 1) / 26 - 1)) + (char) ('A' + (i - 1) % 26));
            }
        }
        return strings;
    }

}