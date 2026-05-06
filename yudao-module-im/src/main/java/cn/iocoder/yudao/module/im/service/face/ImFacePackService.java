package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IM 表情包 Service 接口
 *
 * @author 芋道源码
 */
public interface ImFacePackService {

    // ==================== 用户端 ====================

    /**
     * 获取所有启用的表情包，按 sort 升序
     *
     * @return 启用的表情包列表
     */
    List<ImFacePackDO> getEnabledFacePackList();

    /**
     * 校验表情包存在
     *
     * @param id 表情包编号
     * @return 表情包 DO
     */
    @SuppressWarnings("UnusedReturnValue")
    ImFacePackDO validateFacePackExists(Long id);

    // ==================== 管理后台 ====================

    /**
     * 分页查询表情包
     *
     * @param reqVO 分页查询条件
     * @return 表情包分页
     */
    PageResult<ImFacePackDO> getFacePackPage(ImFacePackPageReqVO reqVO);

    /**
     * 获取表情包详情
     *
     * @param id 表情包编号
     * @return 表情包 DO
     */
    ImFacePackDO getFacePack(Long id);

    /**
     * 新增表情包
     *
     * @param reqVO 新增请求
     * @return 新增表情包编号
     */
    Long createFacePack(@Valid ImFacePackSaveReqVO reqVO);

    /**
     * 修改表情包
     *
     * @param reqVO 修改请求
     */
    void updateFacePack(@Valid ImFacePackSaveReqVO reqVO);

    /**
     * 删除表情包；包下存在表情时拒绝，避免历史 face 消息无法回查归属
     *
     * @param id 表情包编号
     */
    void deleteFacePack(Long id);

    /**
     * 批量删除表情包；任一包下存在表情时整批拒绝，避免「只删一半」中间态
     *
     * @param ids 表情包编号列表
     */
    void deleteFacePackList(List<Long> ids);

}
