package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.useritem.ImFaceUserItemSaveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.useritem.ImFaceUserItemManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFaceUserItemDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IM 用户私有表情 Service 接口
 *
 * @author 芋道源码
 */
public interface ImFaceUserItemService {

    /**
     * 获取指定用户的个人表情列表
     *
     * @param userId 用户编号
     * @return 个人表情列表
     */
    List<ImFaceUserItemDO> getFaceUserItemList(Long userId);

    /**
     * 添加个人表情
     *
     * @param userId 用户编号
     * @param reqVO  添加请求
     * @return 新增表情编号
     */
    Long createFaceUserItem(Long userId, @Valid ImFaceUserItemSaveReqVO reqVO);

    /**
     * 删除指定用户的某条个人表情
     *
     * @param userId 用户编号
     * @param id     表情编号
     */
    void deleteFaceUserItem(Long userId, Long id);

    // ==================== 管理后台 ====================

    /**
     * 分页查询所有用户的个人表情；管理后台审计 / 删除违规图用
     *
     * @param reqVO 分页查询条件
     * @return 个人表情分页
     */
    PageResult<ImFaceUserItemDO> getFaceUserItemPage(ImFaceUserItemManagerPageReqVO reqVO);

    /**
     * 管理后台直接删除某条个人表情；不做归属校验
     *
     * @param id 表情编号
     */
    void deleteFaceUserItem(Long id);

}
