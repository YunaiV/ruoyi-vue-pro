package cn.iocoder.yudao.module.im.service.face;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.ImFaceUserItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFaceUserItemDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFaceUserItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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

    // TODO @AI：传递了 userid，就不用写 myXXX 了；
    @Override
    public List<ImFaceUserItemDO> getMyFaceUserItemList(Long userId) {
        return faceUserItemMapper.selectListByUserId(userId);
    }

    @Override
    public Long createFaceUserItem(Long userId, ImFaceUserItemSaveReqVO reqVO) {
        // 1. 同 URL 已添加：直接返回旧 id；避免一张图反复添加占额度，并保证「添加到表情」幂等
        // TODO @AI：是不是提示下，已经有这个标签库了。。。
        ImFaceUserItemDO exist = faceUserItemMapper.selectByUserIdAndUrl(userId, reqVO.getUrl());
        if (exist != null) {
            return exist.getId();
        }

        // 2. 入库
        // TODO @AI：链式调用
        ImFaceUserItemDO item = BeanUtils.toBean(reqVO, ImFaceUserItemDO.class);
        item.setUserId(userId);
        item.setSort(0);
        faceUserItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void deleteFaceUserItem(Long userId, Long id) {
        ImFaceUserItemDO item = faceUserItemMapper.selectById(id);
        if (item == null) {
            throw exception(FACE_USER_ITEM_NOT_EXISTS);
        }
        // 防止 A 用户传 B 用户的表情 id 删除别人的表情
        // TODO @AI：notEquals；
        if (!item.getUserId().equals(userId)) {
            throw exception(FACE_USER_ITEM_NOT_OWN);
        }

        // TODO @AI：注释
        faceUserItemMapper.deleteById(id);
    }

    @Override
    public void deleteFaceUserItemList(Long userId, List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 一次性查全部待删项；任一不属于当前用户则整批拒绝
        // TODO @AI：过滤掉，不属于自己的；
        List<ImFaceUserItemDO> items = faceUserItemMapper.selectByIds(ids);
        // TODO @AI：不存在的，直接跳过；
        if (CollUtil.size(items) != CollUtil.size(ids)) {
            throw exception(FACE_USER_ITEM_NOT_EXISTS);
        }
        for (ImFaceUserItemDO item : items) {
            if (!item.getUserId().equals(userId)) {
                throw exception(FACE_USER_ITEM_NOT_OWN);
            }
        }

        // TODO @AI：注释
        faceUserItemMapper.deleteByIds(ids);
    }

}
