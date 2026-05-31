package cn.iocoder.yudao.module.im.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * IM 频道素材 Service 接口
 *
 * @author 芋道源码
 */
public interface ImChannelMaterialService {

    // ==================== 用户端 ====================

    /**
     * 校验素材存在
     *
     * @param id 素材编号
     * @return 素材 DO
     */
    ImChannelMaterialDO validateMaterialExists(Long id);

    /**
     * 按编号批量查询素材
     *
     * @param ids 素材编号列表
     * @return 素材列表
     */
    List<ImChannelMaterialDO> getMaterialList(Collection<Long> ids);

    /**
     * 按编号批量查询素材 Map
     *
     * @param ids 素材编号列表
     * @return id -> 素材 Map
     */
   default Map<Long, ImChannelMaterialDO> getMaterialMap(Collection<Long> ids) {
       return convertMap(getMaterialList(ids), ImChannelMaterialDO::getId);
   }

    /**
     * 统计指定频道下的素材数量
     *
     * @param channelId 频道编号
     * @return 数量
     */
    Long getMaterialCountByChannelId(Long channelId);

    // ==================== 管理后台 ====================

    /**
     * 按频道查询素材精简列表
     *
     * @param channelId 频道编号
     * @return 素材列表
     */
    List<ImChannelMaterialDO> getMaterialListByChannelId(Long channelId);

    /**
     * 分页查询素材
     *
     * @param reqVO 分页查询条件
     * @return 素材分页
     */
    PageResult<ImChannelMaterialDO> getMaterialPage(ImChannelMaterialPageReqVO reqVO);

    /**
     * 获取素材详情（含 content 富文本）
     *
     * @param id 素材编号
     * @return 素材 DO
     */
    ImChannelMaterialDO getMaterial(Long id);

    /**
     * 新增素材
     *
     * @param reqVO 新增请求
     * @return 新增素材编号
     */
    Long createMaterial(@Valid ImChannelMaterialSaveReqVO reqVO);

    /**
     * 修改素材
     *
     * @param reqVO 修改请求
     */
    void updateMaterial(@Valid ImChannelMaterialSaveReqVO reqVO);

    /**
     * 删除素材；素材已被推送过时拒绝，避免历史消息无法回查
     *
     * @param id 素材编号
     */
    void deleteMaterial(Long id);

}
