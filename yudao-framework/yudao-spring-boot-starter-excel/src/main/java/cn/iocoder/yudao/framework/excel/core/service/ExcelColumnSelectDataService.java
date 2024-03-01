package cn.iocoder.yudao.framework.excel.core.service;

import cn.hutool.core.util.StrUtil;

import java.util.Collections;
import java.util.List;

/**
 * Excel 列下拉数据源获取接口
 *
 * 为什么不直接解析字典还搞个接口？考虑到有的下拉数据不是从字典中获取的所有需要做一个兼容
 *
 * @author HUIHUI
 */
public interface ExcelColumnSelectDataService {

    /**
     * 获得方法名称
     *
     * @return 方法名称
     */
    String getFunctionName();

    /**
     * 获得列下拉数据源
     *
     * @return 下拉数据源
     */
    List<String> getSelectDataList();

    default List<String> handle(String funcName) {
        if (StrUtil.isEmptyIfStr(funcName) || !StrUtil.equals(getFunctionName(), funcName)) {
            return Collections.emptyList();
        }
        return getSelectDataList();
    }

}
