package cn.iocoder.yudao.module.oa.service.file;

import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFileFavoriteDO;

import java.util.Collection;
import java.util.List;

/**
 * 云盘收藏 Service 接口
 *
 * @author 芋道源码
 */
public interface OaFileFavoriteService {

    /**
     * 获得用户收藏列表
     *
     * @param userId 用户编号
     * @return 收藏列表
     */
    List<OaFileFavoriteDO> getFileFavoriteList(Long userId);

    /**
     * 获得用户在指定节点中的收藏列表
     *
     * @param userId 用户编号
     * @param nodeIds 节点编号集合
     * @return 收藏列表
     */
    List<OaFileFavoriteDO> getFileFavoriteListByUserIdAndNodeIds(Long userId, Collection<Long> nodeIds);

    /**
     * 收藏文件节点
     *
     * @param nodeId 节点编号
     * @param userId 用户编号
     */
    void createFileFavorite(Long nodeId, Long userId);

    /**
     * 取消本人收藏
     *
     * @param nodeId 节点编号
     * @param userId 用户编号
     */
    void deleteFileFavorite(Long nodeId, Long userId);

    /**
     * 清理节点收藏
     *
     * @param nodeIds 节点编号集合
     */
    void deleteFileFavoritesByNodeIds(Collection<Long> nodeIds);

}
