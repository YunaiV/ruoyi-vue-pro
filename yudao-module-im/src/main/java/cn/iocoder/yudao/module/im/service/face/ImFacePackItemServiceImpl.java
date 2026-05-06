package cn.iocoder.yudao.module.im.service.face;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.ImFacePackItemPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.ImFacePackItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackItemDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFacePackItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_PACK_ITEM_NOT_EXISTS;

/**
 * IM 表情包项 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImFacePackItemServiceImpl implements ImFacePackItemService {

    @Resource
    private ImFacePackItemMapper facePackItemMapper;
    @Resource
    private ImFacePackService facePackService;

    // ==================== 用户端 ====================

    @Override
    public List<ImFacePackItemDO> getEnabledItemListByPackIds(Collection<Long> packIds) {
        if (CollUtil.isEmpty(packIds)) {
            return Collections.emptyList();
        }
        return facePackItemMapper.selectListByPackIdsAndStatus(packIds, CommonStatusEnum.ENABLE.getStatus());
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImFacePackItemDO> getFacePackItemPage(ImFacePackItemPageReqVO reqVO) {
        return facePackItemMapper.selectPage(reqVO);
    }

    @Override
    public ImFacePackItemDO getFacePackItem(Long id) {
        return facePackItemMapper.selectById(id);
    }

    @Override
    public Long createFacePackItem(ImFacePackItemSaveReqVO reqVO) {
        // 校验所属表情包存在
        facePackService.validateFacePackExists(reqVO.getPackId());
        ImFacePackItemDO item = BeanUtils.toBean(reqVO, ImFacePackItemDO.class);

        // TODO @AI：注释风格，和别的模块一致
        facePackItemMapper.insert(item);
        return item.getId();
    }

    @Override
    public void updateFacePackItem(ImFacePackItemSaveReqVO reqVO) {
        // TODO @AI：注释风格，和别的模块一致
        validateFacePackItemExists(reqVO.getId());
        // TODO @AI：注释风格，和别的模块一致
        facePackService.validateFacePackExists(reqVO.getPackId());

        // TODO @AI：注释风格，和别的模块一致
        ImFacePackItemDO updateObj = BeanUtils.toBean(reqVO, ImFacePackItemDO.class);
        facePackItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteFacePackItem(Long id) {
        // TODO @AI：注释风格，和别的模块一致
        validateFacePackItemExists(id);
        // TODO @AI：注释风格，和别的模块一致
        facePackItemMapper.deleteById(id);
    }

    @Override
    public void deleteFacePackItemList(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        facePackItemMapper.deleteByIds(ids);
    }

    private void validateFacePackItemExists(Long id) {
        if (facePackItemMapper.selectById(id) == null) {
            throw exception(FACE_PACK_ITEM_NOT_EXISTS);
        }
    }

}
