package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.file.core.client.db.DBFileContentFrameworkDAO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileContentDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Repository
public class FileContentDAOImpl implements DBFileContentFrameworkDAO {

    @Resource
    private FileContentMapper fileContentMapper;

    @Override
    public void insert(Long configId, String path, byte[] content) {
        FileContentDO entity = new FileContentDO().setConfigId(configId)
                .setPath(path).setContent(content);
        fileContentMapper.insert(entity);
    }

    @Override
    public void delete(Long configId, String path) {
        fileContentMapper.delete(buildQuery(configId, path));
    }

    @Override
    public byte[] selectContent(Long configId, String path) {
        List<FileContentDO> list = fileContentMapper.selectList(
                buildQuery(configId, path).select(FileContentDO::getContent).orderByDesc(FileContentDO::getId));
        return Optional.ofNullable(CollUtil.getFirst(list))
                .map(FileContentDO::getContent)
                .orElse(null);
    }

    private LambdaQueryWrapper<FileContentDO> buildQuery(Long configId, String path) {
        return new LambdaQueryWrapper<FileContentDO>()
                .eq(FileContentDO::getConfigId, configId)
                .eq(FileContentDO::getPath, path);
    }

}
