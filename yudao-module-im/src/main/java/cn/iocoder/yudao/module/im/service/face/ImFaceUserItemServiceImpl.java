package cn.iocoder.yudao.module.im.service.face;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.useritem.ImFaceUserItemSaveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.useritem.ImFaceUserItemManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFaceUserItemDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFaceUserItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_DUPLICATED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_NOT_OWN;

/**
 * IM 用户私有表情 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImFaceUserItemServiceImpl implements ImFaceUserItemService {

    @Resource
    private ImFaceUserItemMapper faceUserItemMapper;

    @Override
    public List<ImFaceUserItemDO> getFaceUserItemList(Long userId) {
        return faceUserItemMapper.selectListByUserId(userId);
    }

    @Override
    public Long createFaceUserItem(Long userId, ImFaceUserItemSaveReqVO reqVO) {
        // 1. 同 URL 已存在则报错；前端 catch 后 toast「已添加过」
        if (faceUserItemMapper.selectByUserIdAndUrl(userId, reqVO.getUrl()) != null) {
            throw exception(FACE_USER_ITEM_DUPLICATED);
        }

        // 2. 入库
        ImFaceUserItemDO item = BeanUtils.toBean(reqVO, ImFaceUserItemDO.class).setUserId(userId);
        faceUserItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void deleteFaceUserItem(Long userId, Long id) {
        // 1.1 校验存在
        ImFaceUserItemDO item = faceUserItemMapper.selectById(id);
        if (item == null) {
            throw exception(FACE_USER_ITEM_NOT_EXISTS);
        }
        // 1.2 校验归属：防止 A 用户传 B 用户的表情 id 删别人的
        if (ObjectUtil.notEqual(item.getUserId(), userId)) {
            throw exception(FACE_USER_ITEM_NOT_OWN);
        }

        // 2. 删除
        faceUserItemMapper.deleteById(id);
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImFaceUserItemDO> getFaceUserItemPage(ImFaceUserItemManagerPageReqVO reqVO) {
        return faceUserItemMapper.selectPage(reqVO);
    }

    @Override
    public void deleteFaceUserItem(Long id) {
        // 1. 校验存在
        if (faceUserItemMapper.selectById(id) == null) {
            throw exception(FACE_USER_ITEM_NOT_EXISTS);
        }

        // 2. 删除
        faceUserItemMapper.deleteById(id);
    }

}
