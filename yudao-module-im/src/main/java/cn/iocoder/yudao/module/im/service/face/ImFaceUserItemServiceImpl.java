package cn.iocoder.yudao.module.im.service.face;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.useritem.ImFaceUserItemSaveReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.useritem.ImFaceUserItemManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFaceUserItemDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFaceUserItemMapper;
import cn.iocoder.yudao.module.im.framework.config.ImProperties;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_DUPLICATED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_USER_ITEM_MAX_LIMIT;
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
    @Resource
    private ImProperties imProperties;

    @Override
    public List<ImFaceUserItemDO> getFaceUserItemList(Long userId) {
        return faceUserItemMapper.selectListByUserId(userId);
    }

    @Override
    public Long createFaceUserItem(Long userId, ImFaceUserItemSaveReqVO reqVO) {
        // 1.1 同 URL 已存在则报错
        if (faceUserItemMapper.selectByUserIdAndUrl(userId, reqVO.getUrl()) != null) {
            throw exception(FACE_USER_ITEM_DUPLICATED);
        }
        // 1.2 超过最大数量限制则报错
        int maxCount = imProperties.getFace().getUserItemMaxCount();
        if (faceUserItemMapper.selectCountByUserId(userId) >= maxCount) {
            throw exception(FACE_USER_ITEM_MAX_LIMIT, maxCount);
        }

        // 2. 入库
        ImFaceUserItemDO item = BeanUtils.toBean(reqVO, ImFaceUserItemDO.class).setUserId(userId);
        try {
            faceUserItemMapper.insert(item);
        } catch (DuplicateKeyException ex) {
            throw exception(FACE_USER_ITEM_DUPLICATED);
        }
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
