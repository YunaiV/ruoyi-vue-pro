package cn.iocoder.yudao.module.hrm.framework.excel.core;

import cn.iocoder.yudao.framework.excel.core.function.ExcelColumnSelectFunction;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 员工试用期月份下拉框数据源的 {@link ExcelColumnSelectFunction} 实现类
 *
 * @author 芋道源码
 */
@Service
public class HrmEmployeeProbationExcelColumnSelectFunction implements ExcelColumnSelectFunction {

    public static final String NAME = "getHrmEmployeeProbationMonthList";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getOptions() {
        return Arrays.asList("0", "1", "2", "3", "4", "5", "6");
    }

}
