package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackItemDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

// TODO @AI：已经分了用户端、和管理后台，注释上就不用写【用户端】【【管理后台】】了。。。
// TODO @AI：注释的风格，参考下别的模块，一些 params、return 都要写呀；
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

    /**
     * 取某个表情包下的表情数量；ImFacePackService 删除前校验「包下无项」用
     */
    Long getFacePackItemCount(Long packId);

    /**
     * 取多个表情包下的表情数量合计；ImFacePackService 批量删除前校验用
     */
    Long getFacePackItemCount(Collection<Long> packIds);

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
