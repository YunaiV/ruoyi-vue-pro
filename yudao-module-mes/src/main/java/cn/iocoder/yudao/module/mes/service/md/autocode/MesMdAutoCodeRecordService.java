package cn.iocoder.yudao.module.mes.service.md.autocode;

/**
 * MES 编码生成记录 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdAutoCodeRecordService {

    /**
     * 生成编码（无输入字符）
     *
     * @param ruleCode 规则编码
     * @return 生成的编码
     */
    default String generateAutoCode(String ruleCode) {
        return generateAutoCode(ruleCode, null);
    }

    /**
     * 生成编码
     *
     * @param ruleCode 规则编码
     * @param inputChar 输入字符
     * @return 生成的编码
     */
    String generateAutoCode(String ruleCode, String inputChar);

}
