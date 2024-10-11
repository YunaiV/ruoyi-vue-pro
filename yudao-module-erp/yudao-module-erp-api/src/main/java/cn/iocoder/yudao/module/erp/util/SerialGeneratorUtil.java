package cn.iocoder.yudao.module.erp.util;

import cn.hutool.core.text.StrPool;

/**
* @Author Wqh
* @Description 编码自动生成器
* @Date 16:02 2024/10/9
**/

public class SerialGeneratorUtil {
    private SerialGeneratorUtil(){}
    /**
    * @Author Wqh
    * @Description 根据指定前缀+日期生成+随机数序列号
    * @Date 16:06 2024/10/9
    * @Param [prefix, suffix]
    * @return java.lang.String
    **/

    public static String generateSerial(String prefix, String dateStr,String suffix) {
        //获取当前年月日
        return prefix + StrPool.DASHED + dateStr + StrPool.DASHED + suffix;
    }
}
