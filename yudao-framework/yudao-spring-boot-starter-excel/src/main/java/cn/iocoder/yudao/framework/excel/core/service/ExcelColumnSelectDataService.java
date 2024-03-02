package cn.iocoder.yudao.framework.excel.core.service;

import cn.hutool.core.util.StrUtil;

import java.util.Collections;
import java.util.List;

/**
 * Excel 列下拉数据源获取接口
 *
 * 为什么不直接解析字典还搞个接口？考虑到有的下拉数据不是从字典中获取的所有需要做一个兼容
 * TODO @puhui999：是不是 @ExcelColumnSelect 可以搞两个属性，一个 dictType，一个 functionName；如果 dictType，则默认走字典，否则走 functionName
 *                  这样的话，ExcelColumnSelectDataService 改成 ExcelColumnSelectFunction 用于获取数据。
 *
 * @author HUIHUI
 */
public interface ExcelColumnSelectDataService {

    // TODO @puhui999：可以考虑改成 getName
    /**
     * 获得方法名称
     *
     * @return 方法名称
     */
    String getFunctionName();

    // TODO @puhui999：可以考虑改成 getOptions；因为 select 下面是 option 哈，和前端 html 类似的标签；
    /**
     * 获得列下拉数据源
     *
     * @return 下拉数据源
     */
    List<String> getSelectDataList();

    // TODO @puhui999：这个建议放到 SelectSheetWriteHandler 里
    default List<String> handle(String funcName) {
        if (StrUtil.isEmptyIfStr(funcName) || !StrUtil.equals(getFunctionName(), funcName)) {
            return Collections.emptyList();
        }
        return getSelectDataList();
    }

}
