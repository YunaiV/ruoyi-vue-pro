package cn.iocoder.yudao.module.infra.api.file;

import cn.hutool.core.util.IdUtil;

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
       return createFile(IdUtil.fastUUID(), content);
   }

    /**
     * 保存文件，并返回文件的访问路径
     *
     * @param path 文件路径
     * @param content 文件内容
     * @return 文件路径
     */
    String createFile(String path, byte[] content) throws Exception;

}
