package cn.iocoder.yudao.module.infra.api.file;

/**
 * 文件 API 接口
 *
 * @author 芋道源码
 */
public interface FileApi {

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param content 文件内容
     * @return 文件路径
     */
    default String createFile(byte[] content) throws Exception {
        return createFile(null, content);
    }

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param name 原文件名称
     * @param content 文件内容
     * @return 文件路径
     */
    String createFile(String name, byte[] content) throws Exception;

}
