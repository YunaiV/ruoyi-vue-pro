package cn.iocoder.yudao.module.im.service.face;

import cn.iocoder.yudao.module.im.controller.admin.face.vo.userItem.ImFaceUserItemSaveReqVO;
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
     */
    List<ImFaceUserItemDO> getFaceUserItemList(Long userId);

    /**
     * 添加个人表情；返回新增记录 id
     * <p>
     * 同 URL 重复添加抛 FACE_USER_ITEM_DUPLICATED；表上 (user_id, url) 唯一约束兜底并发 race
     */
    Long createFaceUserItem(Long userId, @Valid ImFaceUserItemSaveReqVO reqVO);

    /**
     * 删除指定用户的某条个人表情
     */
    void deleteFaceUserItem(Long userId, Long id);

}
