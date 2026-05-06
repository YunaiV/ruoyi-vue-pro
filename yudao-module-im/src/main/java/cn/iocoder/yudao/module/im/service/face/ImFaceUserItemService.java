package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.module.im.controller.admin.face.vo.ImFaceUserItemSaveReqVO;
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
     * 获取我的个人表情列表
     */
    List<ImFaceUserItemDO> getMyFaceUserItemList(Long userId);

    /**
     * 添加个人表情；返回新增 id（已存在则直接返回旧 id）
     * <p>
     * 同 URL 重复添加时返回旧记录 id，不报错
     */
    Long createFaceUserItem(Long userId, @Valid ImFaceUserItemSaveReqVO reqVO);

    /**
     * 删除我的某条个人表情
     */
    void deleteFaceUserItem(Long userId, Long id);

    /**
     * 批量删除我的个人表情
     */
    void deleteFaceUserItemList(Long userId, List<Long> ids);

}
