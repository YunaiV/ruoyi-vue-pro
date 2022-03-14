package cn.iocoder.yudao.framework.file.core.client.db;

/**
 * 文件内容 Framework DAO 接口
 *
 * @author 芋道源码
 */
public interface DBFileContentFrameworkDAO {

    void insert(Long configId, String path, byte[] content);

    void delete(Long configId, String path);

    byte[] selectContent(Long configId, String path);

}
