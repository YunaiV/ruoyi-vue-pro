package cn.iocoder.yudao.module.oa.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileFavoriteDO;
import cn.iocoder.yudao.module.oa.dal.mysql.file.OaFileFavoriteMapper;
import cn.iocoder.yudao.module.oa.enums.file.OaFilePermissionLevelEnum;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 云盘收藏 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OaFileFavoriteServiceImpl implements OaFileFavoriteService {

    @Resource
    private OaFileFavoriteMapper fileFavoriteMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaFileNodeService fileNodeService;

    @Override
    public List<OaFileFavoriteDO> getFileFavoriteList(Long userId) {
        return fileFavoriteMapper.selectListByUserId(userId);
    }

    @Override
    public List<OaFileFavoriteDO> getFileFavoriteListByUserIdAndNodeIds(Long userId, Collection<Long> nodeIds) {
        if (CollUtil.isEmpty(nodeIds)) {
            return Collections.emptyList();
        }
        return fileFavoriteMapper.selectListByUserIdAndNodeIds(userId, nodeIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFileFavorite(Long nodeId, Long userId) {
        // 1.1 锁定节点并校验可见权限
        fileNodeService.lockFileNode(nodeId);
        fileNodeService.validateFileNodePermission(nodeId, userId, OaFilePermissionLevelEnum.READ.getLevel());
        // 1.2 已收藏时直接返回，保证重复请求不会新增多条记录
        if (fileFavoriteMapper.selectByUserIdAndNodeId(userId, nodeId) != null) {
            return;
        }

        // 2. 新增本人收藏
        fileFavoriteMapper.insert(new OaFileFavoriteDO().setNodeId(nodeId).setUserId(userId));
    }

    @Override
    public void deleteFileFavorite(Long nodeId, Long userId) {
        fileFavoriteMapper.deleteByNodeIdAndUserId(nodeId, userId);
    }

    @Override
    public void deleteFileFavoritesByNodeIds(Collection<Long> nodeIds) {
        if (CollUtil.isEmpty(nodeIds)) {
            return;
        }
        fileFavoriteMapper.deleteByNodeIds(nodeIds);
    }

}
