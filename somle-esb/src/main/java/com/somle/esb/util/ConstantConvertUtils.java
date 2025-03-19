package com.somle.esb.util;


/**
 * @className: ConstantConverUtil
 * @author: Wqh
 * @date: 2024/11/11 13:58
 * @Version: 1.0
 * @description:
 */
public final class ConstantConvertUtils {
    private ConstantConvertUtils(){}


    /**
    * @Author Wqh
    * @Description 将国别码转换为指定的
    * @Date 14:01 2024/11/11
    * @Param [s]
    * @return java.lang.String
    **/
    public static String getCountrySuffix(String countryCode){
        return switch (countryCode) {
            case "CN" -> "CHN";
            case "US" -> "USA";
            case "IN" -> "IND";
            case "GB" -> "UK";
            case "SA" -> "KSA";
            default -> countryCode;
        };
    }
}
