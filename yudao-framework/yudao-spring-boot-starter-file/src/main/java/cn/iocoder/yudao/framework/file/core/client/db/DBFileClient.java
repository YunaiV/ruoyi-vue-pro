package cn.iocoder.yudao.framework.file.core.client.db;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.file.core.client.AbstractFileClient;

/**
 * 基于 DB 存储的文件客户端的配置类
 *
 * @author 芋道源码
 */
public class DBFileClient extends AbstractFileClient<DBFileClientConfig> {

    private DBFileContentFrameworkDAO dao;

    public DBFileClient(Long id, DBFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        dao = SpringUtil.getBean(DBFileContentFrameworkDAO.class);
    }

    @Override
    public String upload(byte[] content, String path) {
        dao.insert(getId(), path, content);
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public void delete(String path) {
        dao.delete(getId(), path);
    }

    @Override
    public byte[] getContent(String path) {
        return dao.selectContent(getId(), path);
    }

}
