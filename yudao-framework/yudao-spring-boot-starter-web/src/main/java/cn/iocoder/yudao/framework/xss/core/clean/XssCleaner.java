package cn.iocoder.yudao.framework.xss.core.clean;

/**
 * 对 html 文本中的有 Xss 风险的数据进行清理
 */
public interface XssCleaner {

    /**
     * 清理有 Xss 风险的文本
     *
     * @param html 原 html
     * @return 清理后的 html
     */
    String clean(String html);

}
