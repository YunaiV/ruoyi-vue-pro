package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.ImFacePackItemPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.ImFacePackItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * IM 表情包项 Service 接口
 *
 * @author 芋道源码
 */
public interface ImFacePackItemService {

    // ==================== 用户端 ====================

    /**
     * 【用户端】按 packIds 批量取启用项，给前端聚合接口用
     */
    List<ImFacePackItemDO> getEnabledItemListByPackIds(Collection<Long> packIds);

    // ==================== 管理后台 ====================

    /**
     * 【管理后台】分页查询
     */
    PageResult<ImFacePackItemDO> getFacePackItemPage(ImFacePackItemPageReqVO reqVO);

    /**
     * 【管理后台】获取详情
     */
    ImFacePackItemDO getFacePackItem(Long id);

    /**
     * 【管理后台】新增，返回新增 id
     */
    Long createFacePackItem(@Valid ImFacePackItemSaveReqVO reqVO);

    /**
     * 【管理后台】修改
     */
    void updateFacePackItem(@Valid ImFacePackItemSaveReqVO reqVO);

    /**
     * 【管理后台】删除
     */
    void deleteFacePackItem(Long id);

    /**
     * 【管理后台】批量删除
     */
    void deleteFacePackItemList(List<Long> ids);

}
