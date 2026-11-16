package cn.iocoder.yudao.module.infra.framework.file.core.client.db;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileContentDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileContentMapper;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;

/**
 * 基于 DB 存储的文件客户端的配置类
 *
 * @author 芋道源码
 */
public class DBFileClient extends AbstractFileClient<DBFileClientConfig> {

    private FileContentMapper fileContentMapper;

    public DBFileClient(Long id, DBFileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        fileContentMapper = SpringUtil.getBean(FileContentMapper.class);
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        FileContentDO contentDO = new FileContentDO().setConfigId(getId())
                .setPath(path).setContent(content);
        fileContentMapper.insert(contentDO);
        // 拼接返回路径
        return super.formatFileUrl(config.getDomain(), path);
    }

    @Override
    public String upload(InputStream content, long size, String path, String type) {
        // 数据库按字节数组保存文件内容，需要整体读入内存，不具备流式大文件优势
        return upload(IoUtil.readBytes(content, false), path, type);
    }

    @Override
    public void delete(String path) {
        fileContentMapper.deleteByConfigIdAndPath(getId(), path);
    }

    @Override
    public byte[] getContent(String path) {
        List<FileContentDO> list = fileContentMapper.selectListByConfigIdAndPath(getId(), path);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        // 排序后，拿 id 最大的，即最后上传的
        list.sort(Comparator.comparing(FileContentDO::getId));
        return CollUtil.getLast(list).getContent();
    }

    @Override
    public InputStream getStream(String path) {
        // 数据库存储没有真实文件流，读取字节数组后适配为文件流
        byte[] content = getContent(path);
        return content == null ? null : new ByteArrayInputStream(content);
    }

}
