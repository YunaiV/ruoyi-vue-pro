package cn.iocoder.yudao.module.oa.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.node.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileFavoriteDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileNodeDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFilePermissionDO;
import cn.iocoder.yudao.module.oa.dal.mysql.file.OaFileNodeMapper;
import cn.iocoder.yudao.module.oa.enums.file.OaFileCategoryEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileNodeStatusEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileNodeTypeEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFilePermissionLevelEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileScopeEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileSubjectTypeEnum;
import cn.iocoder.yudao.module.oa.framework.config.OaProperties;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 云盘文件节点 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaFileNodeServiceImpl implements OaFileNodeService {

    @Resource
    private OaProperties properties;

    @Resource
    private OaFileNodeMapper fileNodeMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaFilePermissionService filePermissionService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaFileFavoriteService fileFavoriteService;

    @Resource
    private FileApi fileApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public OaFileStorageRespVO getFileStorage(Long userId) {
        // 1.1 数据库汇总本人文件容量，回收站文件仍占用容量
        Long usedSize = fileNodeMapper.selectSumSizeByCreatorAndType(userId.toString(), OaFileNodeTypeEnum.FILE.getType());
        // 1.2 数量统计只查询本人正常节点及共享给本人的候选节点
        Long deptId = adminUserApi.getUser(userId).getDeptId();
        List<OaFileNodeDO> candidates = fileNodeMapper.selectListByCreatorAndStatus(userId.toString(), OaFileNodeStatusEnum.NORMAL.getStatus());
        candidates.addAll(getSharedFileNodeList(new OaFileNodePageReqVO().setScope(OaFileScopeEnum.SHARED.getScope()), userId, deptId));
        // 1.3 按需补齐祖先，并查询这些节点的授权
        Map<Long, OaFileNodeDO> nodes = getFileNodeMap(candidates);
        List<OaFilePermissionDO> permissions = filePermissionService.getFilePermissionListByNodeIds(nodes.keySet());
        Set<Long> sharedNodeIds = convertSet(permissions, OaFilePermissionDO::getNodeId,
                permission -> permission.getExpireTime() == null || permission.getExpireTime().isAfter(LocalDateTime.now()));

        // 2. 文件数量和共享数量仅统计可用节点，保留祖先状态及授权有效期校验
        long fileCount = 0;
        long sharedCount = 0;
        long receivedCount = 0;
        for (OaFileNodeDO node : candidates) {
            boolean isOwner = userId.toString().equals(node.getCreator());
            boolean isFile = ObjUtil.equal(node.getType(), OaFileNodeTypeEnum.FILE.getType());
            if (!isAvailable(node, nodes)) {
                continue;
            }
            if (isOwner) {
                if (isFile) {
                    fileCount++;
                }
                if (sharedNodeIds.contains(node.getId())) {
                    sharedCount++;
                }
            } else if (getLevel(node, nodes, permissions, userId, deptId) > OaFilePermissionLevelEnum.LEVEL_NONE) {
                // 共享目录及继承权限的子节点只统计一个入口，与共享列表保持一致
                OaFileNodeDO parent = nodes.get(node.getParentId());
                if (parent == null || getLevel(parent, nodes, permissions, userId, deptId) == OaFilePermissionLevelEnum.LEVEL_NONE) {
                    receivedCount++;
                }
            }
        }
        return new OaFileStorageRespVO().setUsedSize(usedSize).setTotalSize(properties.getFile().getStorageSize())
                .setFileCount(fileCount).setSharedCount(sharedCount).setReceivedCount(receivedCount);
    }

    @Override
    public void lockFileNode(Long id) {
        if (fileNodeMapper.selectByIdForUpdate(id) == null) {
            throw exception(FILE_NODE_NOT_EXISTS);
        }
    }

    @Override
    public OaFileNodeDO validateFileNodePermission(Long id, Long userId, Integer level) {
        // 1. 校验目标及祖先均处于正常状态
        OaFileNodeDO node = validateFileNodeAvailable(id);
        // 2. 计算本人、用户共享和部门共享权限
        Map<Long, OaFileNodeDO> nodes = getFileNodeMap(Collections.singletonList(node));
        List<OaFilePermissionDO> permissions = filePermissionService.getFilePermissionListByNodeIds(nodes.keySet());
        int currentLevel = getLevel(node, nodes, permissions, userId, adminUserApi.getUser(userId).getDeptId());
        if (currentLevel < level) {
            throw exception(FILE_NODE_ACCESS_DENIED);
        }
        return node;
    }

    @Override
    public PageResult<OaFileNodeRespVO> getFileNodePage(OaFileNodePageReqVO reqVO, Long userId) {
        // 1.1 校验指定目录及祖先可见，回收站仍按原有范围筛选
        OaFileNodeDO parent = null;
        if (reqVO.getParentId() != null && ObjUtil.notEqual(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)) {
            parent = validateFileNodePermission(reqVO.getParentId(), userId, OaFilePermissionLevelEnum.READ.getLevel());
        }
        Long deptId = adminUserApi.getUser(userId).getDeptId();
        // 1.2 本人根目录和已校验的本人目录，正常子节点均可见，直接使用数据库分页
        boolean ownRootQuery = OaFileScopeEnum.MY.getScope().equals(reqVO.getScope())
                && ObjUtil.equal(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT);
        boolean ownDirectoryQuery = parent != null && userId.toString().equals(parent.getCreator())
                && ObjUtil.equal(parent.getType(), OaFileNodeTypeEnum.FOLDER.getType())
                && !OaFileScopeEnum.RECYCLE.getScope().equals(reqVO.getScope());
        if (ownRootQuery || ownDirectoryQuery) {
            PageResult<OaFileNodeDO> pageResult = fileNodeMapper.selectPageByParentIdAndCreatorAndStatus(reqVO,
                    ownRootQuery ? userId.toString() : null, OaFileNodeStatusEnum.NORMAL.getStatus());
            return buildFileNodePageRespVO(pageResult, parent == null ? Collections.emptyMap()
                    : Collections.singletonMap(parent.getId(), parent), Collections.emptyList(), userId, deptId);
        }
        // 1.3 仅收藏入口需要提前查询收藏编号，用于筛选；普通列表分页后再补收藏状态
        Set<Long> favorites = Collections.emptySet();
        if (OaFileScopeEnum.FAVORITE.getScope().equals(reqVO.getScope())
                && (reqVO.getParentId() == null || ObjUtil.equal(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT))) {
            favorites = convertSet(fileFavoriteService.getFileFavoriteList(userId), OaFileFavoriteDO::getNodeId);
            if (CollUtil.isEmpty(favorites)) {
                return PageResult.empty();
            }
        }
        // 1.4 数据库筛选候选节点，再批量补齐祖先；祖先仅用于判权，不加入分页结果
        // 共享跨层级入口仍需候选集判权后分页，不能先截取一页再过滤权限
        boolean sharedRootQuery = OaFileScopeEnum.SHARED.getScope().equals(reqVO.getScope())
                && (reqVO.getParentId() == null || ObjUtil.equal(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT));
        List<OaFileNodeDO> candidates = sharedRootQuery ? getSharedFileNodeList(reqVO, userId, deptId)
                : fileNodeMapper.selectCandidateList(reqVO, userId, favorites);
        Map<Long, OaFileNodeDO> nodes = getFileNodeMap(candidates);
        List<OaFilePermissionDO> permissions = filePermissionService.getFilePermissionListByNodeIds(nodes.keySet());

        // 2. 按可见范围筛选，回收站只展示回收根节点，不重复展示被其遮蔽的子节点
        List<OaFileNodeDO> result = new ArrayList<>();
        for (OaFileNodeDO node : candidates) {
            boolean isOwner = userId.toString().equals(node.getCreator());
            boolean isRecycledScope = OaFileScopeEnum.RECYCLE.getScope().equals(reqVO.getScope());
            if (isRecycledScope) {
                if (!isOwner || ObjUtil.notEqual(node.getStatus(), OaFileNodeStatusEnum.RECYCLED.getStatus())
                        || !isAvailableParent(node, nodes)) {
                    continue;
                }
            } else if (!isAvailable(node, nodes)) {
                continue;
            }
            int level = getLevel(node, nodes, permissions, userId, deptId);
            if (!isRecycledScope && level == OaFilePermissionLevelEnum.LEVEL_NONE) {
                continue;
            }
            if (OaFileScopeEnum.MY.getScope().equals(reqVO.getScope()) && !isOwner
                    && (reqVO.getParentId() == null || ObjUtil.equal(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT))) {
                continue;
            }
            if (OaFileScopeEnum.SHARED.getScope().equals(reqVO.getScope()) && isOwner
                    && (reqVO.getParentId() == null || ObjUtil.equal(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT))) {
                continue;
            }
            if (OaFileScopeEnum.FAVORITE.getScope().equals(reqVO.getScope())
                    && (reqVO.getParentId() == null || ObjUtil.equal(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT))
                    && !favorites.contains(node.getId())) {
                continue;
            }
            if (reqVO.getParentId() != null && !isRecycledScope) {
                if (OaFileScopeEnum.SHARED.getScope().equals(reqVO.getScope()) && ObjUtil.equal(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)) {
                    OaFileNodeDO parentNode = nodes.get(node.getParentId());
                    if (parentNode != null && getLevel(parentNode, nodes, permissions, userId, deptId) > OaFilePermissionLevelEnum.LEVEL_NONE) {
                        continue;
                    }
                } else if (ObjUtil.notEqual(node.getParentId(), reqVO.getParentId())) {
                    continue;
                }
            }
            result.add(node);
        }

        // 3. 文件夹优先，同类型按创建时间及编号倒序，分页结果保持稳定
        result.sort(Comparator.comparing(OaFileNodeDO::getType)
                .thenComparing(OaFileNodeDO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(OaFileNodeDO::getId, Comparator.reverseOrder()));
        return buildFileNodePageRespVO(PageUtils.buildPageResult(reqVO, result),
                nodes, permissions, userId, deptId);
    }

    /**
     * 拼接文件分页响应，仅转换当前页并补充收藏状态和权限
     *
     * @param pageResult 文件分页
     * @param nodes 当前节点及祖先
     * @param permissions 相关节点的共享权限
     * @param userId 当前用户编号
     * @param deptId 当前用户部门编号
     * @return 文件分页响应
     */
    private PageResult<OaFileNodeRespVO> buildFileNodePageRespVO(PageResult<OaFileNodeDO> pageResult,
            Map<Long, OaFileNodeDO> nodes, List<OaFilePermissionDO> permissions, Long userId, Long deptId) {
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1. 仅查询当前页节点的本人收藏
        List<OaFileFavoriteDO> favorites = fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(userId,
                convertSet(pageResult.getList(), OaFileNodeDO::getId));
        Set<Long> favoriteNodeIds = convertSet(favorites, OaFileFavoriteDO::getNodeId);
        // 2. 转换当前页并补充权限与收藏状态，保留查询总数
        List<OaFileNodeRespVO> list = convertList(pageResult.getList(), node -> {
            OaFileNodeRespVO respVO = BeanUtils.toBean(node, OaFileNodeRespVO.class);
            respVO.setUrl(null); // 列表不暴露原始地址，预览和下载统一通过详情接口授权
            respVO.setLevel(getLevel(node, nodes, permissions, userId, deptId));
            respVO.setFavorite(favoriteNodeIds.contains(node.getId()));
            return respVO;
        });
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<OaFileNodeRespVO> getFileDirectoryList(Long userId) {
        // 1. 只查询本人正常目录及其祖先，不加载文件
        List<OaFileNodeDO> candidates = fileNodeMapper.selectListByCreatorAndTypeAndStatus(userId.toString(),
                OaFileNodeTypeEnum.FOLDER.getType(), OaFileNodeStatusEnum.NORMAL.getStatus());
        Map<Long, OaFileNodeDO> nodes = getFileNodeMap(candidates);
        // 2. 筛选可用目录并转换响应
        return convertList(candidates,
                node -> BeanUtils.toBean(node, OaFileNodeRespVO.class).setUrl(null),
                node -> ObjUtil.equal(node.getType(), OaFileNodeTypeEnum.FOLDER.getType()) && isAvailable(node, nodes));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFileNode(OaFileNodeSaveReqVO reqVO, Long userId) {
        // 1.1 校验目标目录编辑权限
        if (ObjUtil.notEqual(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)) {
            lockFileNode(reqVO.getParentId());
            OaFileNodeDO parent = validateFileNodePermission(reqVO.getParentId(), userId, OaFilePermissionLevelEnum.EDIT.getLevel());
            if (ObjUtil.notEqual(parent.getType(), OaFileNodeTypeEnum.FOLDER.getType())) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
        }
        // 1.2 文件登记前校验本人剩余容量，回收站文件仍计入占用
        if (ObjUtil.equal(reqVO.getType(), OaFileNodeTypeEnum.FILE.getType())) {
            Long usedSize = fileNodeMapper.selectSumSizeByCreatorAndType(userId.toString(), OaFileNodeTypeEnum.FILE.getType());
            if (reqVO.getSize() > properties.getFile().getStorageSize() - usedSize) {
                throw exception(FILE_STORAGE_NOT_ENOUGH);
            }
        }

        // 2. 保存前端上传后的文件信息，文件分类由扩展名计算
        OaFileNodeDO node = BeanUtils.toBean(reqVO, OaFileNodeDO.class)
                .setStatus(OaFileNodeStatusEnum.NORMAL.getStatus()).setSize(0L).setUrl(null);
        if (ObjUtil.equal(reqVO.getType(), OaFileNodeTypeEnum.FILE.getType())) {
            String extension = FileUtil.extName(reqVO.getName()).toLowerCase(Locale.ROOT);
            node.setUrl(reqVO.getUrl()).setSize(reqVO.getSize()).setExtension(extension)
                    .setCategory(OaFileCategoryEnum.getByExtension(extension).getCategory());
        }
        node.setName(getAvailableFileNodeName(node, reqVO.getParentId(), userId, null));
        node.setDeptId(adminUserApi.getUser(userId).getDeptId());
        fileNodeMapper.insert(node);
        return node.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFileNodeName(OaFileNodeRenameReqVO reqVO, Long userId) {
        // 1.1 锁定节点并校验编辑权限
        lockFileNode(reqVO.getId());
        OaFileNodeDO node = validateFileNodePermission(reqVO.getId(), userId, OaFilePermissionLevelEnum.EDIT.getLevel());
        // 1.2 排除自身，同名时自动追加序号
        node.setName(reqVO.getName());
        String name = getAvailableFileNodeName(node, node.getParentId(), Long.valueOf(node.getCreator()), node.getId());

        // 2. 更新名称，文件分类随新扩展名更新
        OaFileNodeDO update = new OaFileNodeDO().setId(node.getId()).setName(name);
        if (ObjUtil.equal(node.getType(), OaFileNodeTypeEnum.FILE.getType())) {
            String extension = FileUtil.extName(reqVO.getName()).toLowerCase(Locale.ROOT);
            update.setExtension(extension).setCategory(OaFileCategoryEnum.getByExtension(extension).getCategory());
        }
        fileNodeMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFileNodeParent(OaFileNodeMoveReqVO reqVO, Long userId) {
        // 1.1 按编号锁定本人目录，串行校验目录移动，防止两个目录并发互移形成环
        Map<Long, OaFileNodeDO> folders = convertMap(fileNodeMapper.selectListByCreatorAndTypeForUpdate(
                userId.toString(), OaFileNodeTypeEnum.FOLDER.getType()), OaFileNodeDO::getId);
        // 1.2 锁定节点并校验编辑权限
        lockFileNode(reqVO.getId());
        OaFileNodeDO node = validateFileNodePermission(reqVO.getId(), userId, OaFilePermissionLevelEnum.EDIT.getLevel());
        // 1.3 校验节点归属，只允许移动本人节点
        validateOwner(node, userId);
        // 1.4 校验目标目录权限、归属及目录层级
        if (ObjUtil.notEqual(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)) {
            OaFileNodeDO parent = validateFileNodePermission(reqVO.getParentId(), userId, OaFilePermissionLevelEnum.EDIT.getLevel());
            validateOwner(parent, userId);
            if (ObjUtil.notEqual(parent.getType(), OaFileNodeTypeEnum.FOLDER.getType())) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
            parent = folders.get(parent.getId());
            if (parent == null) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
            for (int i = 0; i < Short.MAX_VALUE && parent != null; i++) {
                if (ObjUtil.equal(parent.getId(), node.getId())) {
                    throw exception(FILE_NODE_MOVE_INVALID);
                }
                parent = folders.get(parent.getParentId());
            }
            if (parent != null) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
        }
        // 1.5 目标目录同名时自动追加序号，排除当前节点
        String name = getAvailableFileNodeName(node, reqVO.getParentId(), userId, node.getId());

        // 2. 更新父目录和名称，子树保持原有结构
        fileNodeMapper.updateById(new OaFileNodeDO().setId(node.getId()).setParentId(reqVO.getParentId()).setName(name));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyFileNode(OaFileNodeCopyReqVO reqVO, Long userId) {
        // 1.1 校验源节点下载权限，只复制正常节点
        OaFileNodeDO root = validateFileNodePermission(reqVO.getId(), userId, OaFilePermissionLevelEnum.DOWNLOAD.getLevel());
        // 1.2 目标必须是本人目录，不能复制到源目录自身或下级目录
        if (ObjUtil.notEqual(reqVO.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)) {
            lockFileNode(reqVO.getParentId());
            OaFileNodeDO parent = validateFileNodePermission(reqVO.getParentId(), userId, OaFilePermissionLevelEnum.EDIT.getLevel());
            validateOwner(parent, userId);
            if (ObjUtil.notEqual(parent.getType(), OaFileNodeTypeEnum.FOLDER.getType())
                    || getFileNodeMap(Collections.singletonList(parent)).containsKey(root.getId())) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
        }

        // 2. 按层读取源子树，跳过回收节点，复制前校验每个节点的下载权限
        List<OaFileNodeDO> sources = new ArrayList<>(Collections.singletonList(root));
        Collection<Long> parentIds = ObjUtil.equal(root.getType(), OaFileNodeTypeEnum.FOLDER.getType())
                ? Collections.singleton(root.getId()) : Collections.emptyList();
        while (CollUtil.isNotEmpty(parentIds)) {
            List<OaFileNodeDO> children = filterList(fileNodeMapper.selectListByParentIds(parentIds),
                    node -> ObjUtil.equal(node.getStatus(), OaFileNodeStatusEnum.NORMAL.getStatus()));
            for (OaFileNodeDO child : children) {
                validateFileNodePermission(child.getId(), userId, OaFilePermissionLevelEnum.DOWNLOAD.getLevel());
            }
            sources.addAll(children);
            parentIds = convertList(children, OaFileNodeDO::getId,
                    node -> ObjUtil.equal(node.getType(), OaFileNodeTypeEnum.FOLDER.getType()));
        }

        // 3. 为副本根节点生成不重名的名称，保留文件扩展名
        String name = getAvailableFileNodeName(root, reqVO.getParentId(), userId, null);

        // 4. 保存新的节点树，底层文件由 Infra 复用；不继承原创建人、共享或收藏
        Long deptId = adminUserApi.getUser(userId).getDeptId();
        Map<Long, Long> copiedIds = new HashMap<>();
        Map<Long, List<OaFileNodeDO>> childrenMap = convertMultiMap(sources, OaFileNodeDO::getParentId);
        List<OaFileNodeDO> sourceNodes = Collections.singletonList(root);
        while (CollUtil.isNotEmpty(sourceNodes)) {
            List<OaFileNodeDO> copies = convertList(sourceNodes, source -> new OaFileNodeDO()
                    .setParentId(source == root ? reqVO.getParentId() : copiedIds.get(source.getParentId()))
                    .setName(source == root ? name : source.getName()).setType(source.getType())
                    .setExtension(source.getExtension()).setCategory(source.getCategory())
                    .setSize(source.getSize()).setUrl(source.getUrl()).setDeptId(deptId)
                    .setStatus(OaFileNodeStatusEnum.NORMAL.getStatus()));
            fileNodeMapper.insertBatch(copies);
            List<OaFileNodeDO> children = new ArrayList<>();
            for (int i = 0; i < sourceNodes.size(); i++) {
                OaFileNodeDO source = sourceNodes.get(i);
                copiedIds.put(source.getId(), copies.get(i).getId());
                children.addAll(childrenMap.getOrDefault(source.getId(), Collections.emptyList()));
            }
            sourceNodes = children;
        }
        return copiedIds.get(root.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recycleFileNode(Long id, Long userId) {
        // 1.1 锁定节点并校验节点处于正常状态
        lockFileNode(id);
        OaFileNodeDO node = validateFileNodeAvailable(id);
        // 1.2 校验节点属于本人
        validateOwner(node, userId);

        // 2. 仅回收当前节点，保留子节点原有状态
        fileNodeMapper.updateById(new OaFileNodeDO().setId(id).setStatus(OaFileNodeStatusEnum.RECYCLED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFileNode(Long id, Long userId) {
        // 1.1 锁定节点并校验节点存在
        lockFileNode(id);
        OaFileNodeDO node = validateFileNodeExists(id);
        // 1.2 校验节点属于本人
        validateOwner(node, userId);
        // 1.3 校验节点处于回收站
        if (ObjUtil.notEqual(node.getStatus(), OaFileNodeStatusEnum.RECYCLED.getStatus())) {
            throw exception(FILE_NODE_NOT_RECYCLED);
        }

        // 2. 恢复到个人根目录，同名时在扩展名前追加序号
        String name = getAvailableFileNodeName(node, OaFileNodeDO.PARENT_ID_ROOT, userId, node.getId());
        fileNodeMapper.updateById(new OaFileNodeDO().setId(id).setParentId(OaFileNodeDO.PARENT_ID_ROOT)
                .setName(name).setStatus(OaFileNodeStatusEnum.NORMAL.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFileNode(Long id, Long userId) {
        // 1.1 锁定节点并校验节点存在
        lockFileNode(id);
        OaFileNodeDO root = validateFileNodeExists(id);
        // 1.2 校验节点属于本人
        validateOwner(root, userId);
        // 1.3 校验节点处于回收站
        if (ObjUtil.notEqual(root.getStatus(), OaFileNodeStatusEnum.RECYCLED.getStatus())) {
            throw exception(FILE_NODE_NOT_RECYCLED);
        }

        // 2. 仅删除所选节点及其共享、收藏，不递归删除子节点
        filePermissionService.deleteFilePermissionsByNodeIds(Collections.singleton(id));
        fileFavoriteService.deleteFileFavoritesByNodeIds(Collections.singleton(id));
        fileNodeMapper.deleteById(id);
    }

    @Override
    public OaFileNodeRespVO getFileNode(Long id, Long userId) {
        // 1.1 校验节点及祖先均处于正常状态
        OaFileNodeDO node = validateFileNodeAvailable(id);
        // 1.2 校验当前用户具有查看权限
        Map<Long, OaFileNodeDO> nodes = getFileNodeMap(Collections.singletonList(node));
        List<OaFilePermissionDO> permissions = filePermissionService.getFilePermissionListByNodeIds(nodes.keySet());
        int level = getLevel(node, nodes, permissions, userId, adminUserApi.getUser(userId).getDeptId());
        if (level < OaFilePermissionLevelEnum.READ.getLevel()) {
            throw exception(FILE_NODE_ACCESS_DENIED);
        }

        // 2. 查询当前用户的收藏状态
        List<OaFileFavoriteDO> favorites = fileFavoriteService.getFileFavoriteListByUserIdAndNodeIds(userId,
                Collections.singleton(id));

        // 3. 拼接详情，仅为具有下载权限的文件签发临时地址
        OaFileNodeRespVO respVO = BeanUtils.toBean(node, OaFileNodeRespVO.class)
                .setLevel(level)
                .setFavorite(CollUtil.isNotEmpty(favorites))
                .setUrl(null);
        if (ObjUtil.equal(node.getType(), OaFileNodeTypeEnum.FILE.getType())
                && level >= OaFilePermissionLevelEnum.DOWNLOAD.getLevel()) {
            respVO.setUrl(fileApi.presignGetUrl(node.getUrl(), properties.getFile().getDownloadUrlExpireSeconds()));
        }
        return respVO;
    }

    // ==================== 校验与权限计算 ====================

    /**
     * 获得共享给当前用户的候选节点
     *
     * @param reqVO 查询条件
     * @param userId 当前用户编号
     * @param deptId 当前用户部门编号
     * @return 直接共享及目录继承范围内的候选节点，祖先状态和最终权限由调用方校验
     */
    private List<OaFileNodeDO> getSharedFileNodeList(OaFileNodePageReqVO reqVO, Long userId, Long deptId) {
        // 1. 查询本人、部门授权，以及本人创建且可向子节点传递管理权限的目录
        List<OaFilePermissionDO> permissions = filePermissionService.getValidFilePermissionList(userId, deptId);
        Set<Long> sharedNodeIds = convertSet(permissions, OaFilePermissionDO::getNodeId);
        Set<Long> inheritedParentIds = convertSet(fileNodeMapper.selectListByCreatorAndTypeAndStatus(userId.toString(),
                OaFileNodeTypeEnum.FOLDER.getType(), OaFileNodeStatusEnum.NORMAL.getStatus()), OaFileNodeDO::getId);
        inheritedParentIds.addAll(convertSet(permissions, OaFilePermissionDO::getNodeId,
                permission -> Boolean.TRUE.equals(permission.getInherit())));

        // 2. 按层批量补齐可继承的子目录，只查询目录，不拉取整棵文件树
        Collection<Long> parentIds = inheritedParentIds;
        for (int i = 0; i < Short.MAX_VALUE && CollUtil.isNotEmpty(parentIds); i++) {
            List<OaFileNodeDO> children = fileNodeMapper.selectListByParentIdsAndTypeAndStatus(parentIds,
                    OaFileNodeTypeEnum.FOLDER.getType(), OaFileNodeStatusEnum.NORMAL.getStatus());
            parentIds = convertSet(children, OaFileNodeDO::getId, node -> !inheritedParentIds.contains(node.getId()));
            inheritedParentIds.addAll(parentIds);
        }
        if (CollUtil.isEmpty(sharedNodeIds) && CollUtil.isEmpty(inheritedParentIds)) {
            return Collections.emptyList();
        }

        // 3. 将授权编号、可继承目录和搜索条件交给 Mapper，进一步缩小文件候选集
        return fileNodeMapper.selectListBySharedNodeIdsOrParentIdsAndCreatorNot(reqVO, userId, sharedNodeIds, inheritedParentIds);
    }

    /**
     * 获得候选节点及全部祖先
     *
     * @param candidates 候选节点
     * @return 节点及祖先 Map
     */
    private Map<Long, OaFileNodeDO> getFileNodeMap(Collection<OaFileNodeDO> candidates) {
        Map<Long, OaFileNodeDO> nodes = convertMap(candidates, OaFileNodeDO::getId);
        Collection<OaFileNodeDO> currentNodes = candidates;
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            Set<Long> parentIds = convertSet(currentNodes, OaFileNodeDO::getParentId,
                    node -> node.getParentId() != null
                            && ObjUtil.notEqual(node.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)
                            && !nodes.containsKey(node.getParentId()));
            if (CollUtil.isEmpty(parentIds)) {
                return nodes;
            }
            currentNodes = fileNodeMapper.selectByIds(parentIds);
            nodes.putAll(convertMap(currentNodes, OaFileNodeDO::getId));
        }
        throw exception(FILE_NODE_PATH_INVALID);
    }

    /**
     * 校验文件节点由当前用户创建
     *
     * @param node 文件节点
     * @param userId 当前用户编号
     */
    private void validateOwner(OaFileNodeDO node, Long userId) {
        if (ObjUtil.notEqual(userId.toString(), node.getCreator())) {
            throw exception(FILE_NODE_ACCESS_DENIED);
        }
    }

    /**
     * 获得目标目录中的可用名称，同名时在文件扩展名前追加序号
     *
     * @param node 源节点
     * @param parentId 目标目录编号
     * @param userId 当前用户编号
     * @param excludeId 排除的节点编号，新增或复制时为空
     * @return 不与目标目录正常节点重名的名称
     */
    private String getAvailableFileNodeName(OaFileNodeDO node, Long parentId, Long userId, Long excludeId) {
        List<OaFileNodeDO> siblings = fileNodeMapper.selectListByParentId(parentId);
        Set<String> names = convertSet(siblings, item -> item.getName().toLowerCase(Locale.ROOT),
                item -> ObjUtil.equal(item.getStatus(), OaFileNodeStatusEnum.NORMAL.getStatus())
                        && ObjUtil.notEqual(item.getId(), excludeId)
                        && (ObjUtil.notEqual(parentId, OaFileNodeDO.PARENT_ID_ROOT)
                        || userId.toString().equals(item.getCreator())));
        String name = node.getName();
        String baseName = ObjUtil.equal(node.getType(), OaFileNodeTypeEnum.FILE.getType())
                ? StrUtil.blankToDefault(FileUtil.mainName(name), name) : name;
        String suffix = StrUtil.removePrefix(name, baseName);
        for (int index = 1; names.contains(name.toLowerCase(Locale.ROOT)); index++) {
            String number = "(" + index + ")";
            name = StrUtil.subPre(baseName, Math.max(0, 255 - suffix.length() - number.length())) + number + suffix;
        }
        return name;
    }

    /**
     * 判断节点是否位于根目录或可用的父级路径下
     *
     * @param node 文件节点
     * @param nodes 节点及祖先 Map
     * @return 父级路径是否可用
     */
    private boolean isAvailableParent(OaFileNodeDO node, Map<Long, OaFileNodeDO> nodes) {
        return ObjUtil.equal(node.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)
                || isAvailable(nodes.get(node.getParentId()), nodes);
    }

    /**
     * 判断节点及祖先是否正常，并排除断链和循环路径
     *
     * @param node 文件节点
     * @param nodes 节点及祖先 Map
     * @return 节点路径是否可用
     */
    private boolean isAvailable(OaFileNodeDO node, Map<Long, OaFileNodeDO> nodes) {
        Set<Long> visited = new HashSet<>();
        for (int i = 0; i < Short.MAX_VALUE && node != null && visited.add(node.getId()); i++) {
            if (ObjUtil.notEqual(node.getStatus(), OaFileNodeStatusEnum.NORMAL.getStatus())) {
                return false;
            }
            if (ObjUtil.equal(node.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)) {
                return true;
            }
            node = nodes.get(node.getParentId());
            if (node != null && ObjUtil.notEqual(node.getType(), OaFileNodeTypeEnum.FOLDER.getType())) {
                return false;
            }
        }
        return false;
    }

    /**
     * 计算本人、用户共享及部门共享中的最高权限等级
     *
     * @param node 当前节点
     * @param nodes 当前节点及祖先
     * @param permissions 相关节点的共享权限
     * @param userId 当前用户编号
     * @param deptId 当前用户部门编号
     * @return 有效权限等级，无授权时返回 LEVEL_NONE
     */
    private int getLevel(OaFileNodeDO node, Map<Long, OaFileNodeDO> nodes,
                         List<OaFilePermissionDO> permissions, Long userId, Long deptId) {
        // 本人创建的节点直接拥有管理权限
        if (userId.toString().equals(node.getCreator())) {
            return OaFilePermissionLevelEnum.MANAGE.getLevel();
        }
        int level = OaFilePermissionLevelEnum.LEVEL_NONE;
        Long nodeId = node.getId();
        Set<Long> visited = new HashSet<>();
        for (int i = 0; i < Short.MAX_VALUE && node != null && visited.add(node.getId()); i++) {
            // 仅计算当前节点授权，以及祖先允许继承且尚未过期的授权
            for (OaFilePermissionDO permission : permissions) {
                if (ObjUtil.notEqual(permission.getNodeId(), node.getId())
                        || (permission.getExpireTime() != null && !permission.getExpireTime().isAfter(LocalDateTime.now()))
                        || (ObjUtil.notEqual(nodeId, node.getId()) && !Boolean.TRUE.equals(permission.getInherit()))) {
                    continue;
                }
                if ((ObjUtil.equal(permission.getSubjectType(), OaFileSubjectTypeEnum.USER.getType())
                        && ObjUtil.equal(permission.getSubjectId(), userId))
                        || (ObjUtil.equal(permission.getSubjectType(), OaFileSubjectTypeEnum.DEPT.getType())
                        && deptId != null && ObjUtil.equal(permission.getSubjectId(), deptId))) {
                    level = Math.max(level, permission.getLevel());
                }
            }
            // 目录创建人对该目录下新增的文件保留管理能力
            if (userId.toString().equals(node.getCreator())) {
                level = OaFilePermissionLevelEnum.MANAGE.getLevel();
            }
            node = nodes.get(node.getParentId());
        }
        return level;
    }

    /**
     * 校验文件节点存在
     *
     * @param id 节点编号
     * @return 文件节点
     */
    private OaFileNodeDO validateFileNodeExists(Long id) {
        OaFileNodeDO node = fileNodeMapper.selectById(id);
        if (node == null) {
            throw exception(FILE_NODE_NOT_EXISTS);
        }
        return node;
    }

    @Override
    public OaFileNodeDO validateFileNodeAvailable(Long id) {
        // 1. 校验目标节点存在，逻辑删除的节点由 Mapper 自动过滤
        OaFileNodeDO node = validateFileNodeExists(id);

        // 2. 校验节点及祖先状态，防止通过搜索、收藏或直接编号访问回收目录中的文件
        Set<Long> visitedIds = new HashSet<>();
        OaFileNodeDO current = node;
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 2.1 拒绝循环目录，避免异常数据导致无限向上查询
            if (!visitedIds.add(current.getId())) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
            // 2.2 任意一层已回收时，该节点均不可访问
            if (ObjUtil.notEqual(current.getStatus(), OaFileNodeStatusEnum.NORMAL.getStatus())) {
                throw exception(FILE_NODE_NOT_AVAILABLE);
            }
            if (ObjUtil.equal(current.getParentId(), OaFileNodeDO.PARENT_ID_ROOT)) {
                return node;
            }
            // 2.3 父节点必须存在且为目录，不能将断开的目录链视为根目录
            if (current.getParentId() == null) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
            current = fileNodeMapper.selectById(current.getParentId());
            if (current == null || ObjUtil.notEqual(current.getType(), OaFileNodeTypeEnum.FOLDER.getType())) {
                throw exception(FILE_NODE_PATH_INVALID);
            }
        }
        throw exception(FILE_NODE_PATH_INVALID);
    }

}
