package cn.iocoder.yudao.framework.excel.core.function;

import java.util.List;

/**
 * Excel 列下拉数据源获取接口
 *
 * 为什么不直接解析字典还搞个接口？考虑到有的下拉数据不是从字典中获取的所有需要做一个兼容

 * @author HUIHUI
 */
public interface ExcelColumnSelectFunction {

    /**
     * 获得方法名称
     *
     * @return 方法名称
     */
    String getName();

    /**
     * 获得列下拉数据源
     *
     * @return 下拉数据源
     */
    List<String> getOptions();

}
