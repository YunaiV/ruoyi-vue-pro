package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackItemDO;

import javax.validation.Valid;
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
     * 按 packIds 批量取启用项，给前端聚合接口用
     *
     * @param packIds 表情包编号列表
     * @return 启用状态的表情列表
     */
    List<ImFacePackItemDO> getEnabledItemListByPackIds(Collection<Long> packIds);

    /**
     * 取某个表情包下的表情数量；ImFacePackService 删除前校验「包下无项」用
     *
     * @param packId 表情包编号
     * @return 数量
     */
    Long getFacePackItemCount(Long packId);

    /**
     * 取多个表情包下的表情数量合计；ImFacePackService 批量删除前校验用
     *
     * @param packIds 表情包编号列表
     * @return 数量合计
     */
    Long getFacePackItemCount(Collection<Long> packIds);

    // ==================== 管理后台 ====================

    /**
     * 分页查询表情包项
     *
     * @param reqVO 分页查询条件
     * @return 表情包项分页
     */
    PageResult<ImFacePackItemDO> getFacePackItemPage(ImFacePackItemPageReqVO reqVO);

    /**
     * 获取表情包项详情
     *
     * @param id 表情包项编号
     * @return 表情包项 DO
     */
    ImFacePackItemDO getFacePackItem(Long id);

    /**
     * 新增表情包项
     *
     * @param reqVO 新增请求
     * @return 新增表情包项编号
     */
    Long createFacePackItem(@Valid ImFacePackItemSaveReqVO reqVO);

    /**
     * 修改表情包项
     *
     * @param reqVO 修改请求
     */
    void updateFacePackItem(@Valid ImFacePackItemSaveReqVO reqVO);

    /**
     * 删除表情包项
     *
     * @param id 表情包项编号
     */
    void deleteFacePackItem(Long id);

    /**
     * 批量删除表情包项
     *
     * @param ids 表情包项编号列表
     */
    void deleteFacePackItemList(List<Long> ids);

}
