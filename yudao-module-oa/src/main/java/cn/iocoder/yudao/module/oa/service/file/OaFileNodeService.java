package cn.iocoder.yudao.module.oa.service.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.node.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileNodeDO;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 云盘文件节点 Service 接口
 *
 * @author 芋道源码
 */
public interface OaFileNodeService {

    /**
     * 获得本人云盘空间使用情况，包含回收站文件，不包含他人共享文件
     *
     * @param userId 当前用户编号
     * @return 空间使用情况
     */
    OaFileStorageRespVO getFileStorage(Long userId);

    /**
     * 锁定文件节点，用于串行化收藏与共享修改
     *
     * @param id 节点编号
     */
    void lockFileNode(Long id);

    /**
     * 校验节点可访问且具有指定权限
     *
     * @param id 节点编号
     * @param userId 当前用户
     * @param level 所需权限级别
     * @return 节点
     */
    OaFileNodeDO validateFileNodePermission(Long id, Long userId, Integer level);

    /**
     * 获得云盘分页
     *
     * @param reqVO 查询条件
     * @param userId 当前用户
     * @return 文件分页
     */
    PageResult<OaFileNodeRespVO> getFileNodePage(OaFileNodePageReqVO reqVO, Long userId);

    /**
     * 获得本人可移动到的目录
     *
     * @param userId 当前用户
     * @return 目录列表
     */
    List<OaFileNodeRespVO> getFileDirectoryList(Long userId);

    /**
     * 创建文件或目录
     *
     * @param reqVO 节点信息
     * @param userId 当前用户
     * @return 节点编号
     */
    Long createFileNode(@Valid OaFileNodeSaveReqVO reqVO, Long userId);

    /**
     * 重命名文件或目录
     *
     * @param reqVO 新名称
     * @param userId 当前用户
     */
    void updateFileNodeName(OaFileNodeRenameReqVO reqVO, Long userId);

    /**
     * 移动本人文件或目录
     *
     * @param reqVO 目标位置
     * @param userId 当前用户
     */
    void updateFileNodeParent(OaFileNodeMoveReqVO reqVO, Long userId);

    /**
     * 复制文件或整棵目录到本人目录，不复制共享和收藏关系
     *
     * @param reqVO 源节点及目标目录
     * @param userId 当前用户
     * @return 副本根节点编号
     */
    Long copyFileNode(@Valid OaFileNodeCopyReqVO reqVO, Long userId);

    /**
     * 将本人节点移入回收站
     *
     * @param id 节点编号
     * @param userId 当前用户
     */
    void recycleFileNode(Long id, Long userId);

    /**
     * 恢复本人回收站节点
     *
     * @param id 节点编号
     * @param userId 当前用户
     */
    void restoreFileNode(Long id, Long userId);

    /**
     * 逻辑删除本人回收站节点及其子树，不清理物理文件
     *
     * @param id 节点编号
     * @param userId 当前用户
     */
    void deleteFileNode(Long id, Long userId);

    /**
     * 获得文件详情，具有下载权限时附带临时文件地址
     *
     * @param id 节点编号
     * @param userId 当前用户
     * @return 文件详情，目录或仅具有查看权限时不返回文件地址
     */
    OaFileNodeRespVO getFileNode(Long id, Long userId);

    /**
     * 校验节点及全部祖先处于正常状态
     *
     * 仅校验目录结构和回收状态，不代替归属及共享权限校验
     *
     * @param id 节点编号
     * @return 文件节点
     */
    OaFileNodeDO validateFileNodeAvailable(Long id);

}
