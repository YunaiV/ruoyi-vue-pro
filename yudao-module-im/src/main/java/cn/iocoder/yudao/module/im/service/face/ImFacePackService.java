package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.ImFacePackPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.ImFacePackSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IM 表情包 Service 接口
 *
 * @author 芋道源码
 */
public interface ImFacePackService {

    // TODO @AI：已经区分了 === 就不用写【用户端】【管理后台】了。

    // ==================== 用户端 ====================

    /**
     * 【用户端】获取所有启用的表情包，按 sort 升序
     */
    List<ImFacePackDO> getEnabledFacePackList();

    /**
     * 校验表情包存在
     */
    @SuppressWarnings("UnusedReturnValue")
    ImFacePackDO validateFacePackExists(Long id);

    // ==================== 管理后台 ====================

    /**
     * 【管理后台】分页查询表情包
     */
    PageResult<ImFacePackDO> getFacePackPage(ImFacePackPageReqVO reqVO);

    /**
     * 【管理后台】获取表情包详情
     */
    ImFacePackDO getFacePack(Long id);

    /**
     * 【管理后台】新增表情包，返回新增 id
     */
    Long createFacePack(@Valid ImFacePackSaveReqVO reqVO);

    /**
     * 【管理后台】修改表情包
     */
    void updateFacePack(@Valid ImFacePackSaveReqVO reqVO);

    /**
     * 【管理后台】删除表情包；存在表情时拒绝
     */
    void deleteFacePack(Long id);

    /**
     * 【管理后台】批量删除表情包
     */
    void deleteFacePackList(List<Long> ids);

}
