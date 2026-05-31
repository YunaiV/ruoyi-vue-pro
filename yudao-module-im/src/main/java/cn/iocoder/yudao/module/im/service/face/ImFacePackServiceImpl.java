package cn.iocoder.yudao.module.im.service.face;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.pack.ImFacePackSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackDO;
import cn.iocoder.yudao.module.im.dal.mysql.face.ImFacePackMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_PACK_HAS_ITEMS;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.FACE_PACK_NOT_EXISTS;

/**
 * IM 表情包 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImFacePackServiceImpl implements ImFacePackService {

    @Resource
    private ImFacePackMapper facePackMapper;

    /**
     * @Lazy 解决与 ImFacePackItemServiceImpl 的循环依赖（item.create / update 校验所属包存在 → 反向调用本类）
     */
    @Resource
    @Lazy
    private ImFacePackItemService facePackItemService;

    // ==================== 用户端 ====================

    @Override
    public List<ImFacePackDO> getEnabledFacePackList() {
        return facePackMapper.selectListByStatusOrderBySort(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public ImFacePackDO validateFacePackExists(Long id) {
        ImFacePackDO pack = facePackMapper.selectById(id);
        if (pack == null) {
            throw exception(FACE_PACK_NOT_EXISTS);
        }
        return pack;
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImFacePackDO> getFacePackPage(ImFacePackPageReqVO reqVO) {
        return facePackMapper.selectPage(reqVO);
    }

    @Override
    public ImFacePackDO getFacePack(Long id) {
        return facePackMapper.selectById(id);
    }

    @Override
    public Long createFacePack(ImFacePackSaveReqVO reqVO) {
        ImFacePackDO pack = BeanUtils.toBean(reqVO, ImFacePackDO.class);
        facePackMapper.insert(pack);
        return pack.getId();
    }

    @Override
    public void updateFacePack(ImFacePackSaveReqVO reqVO) {
        // 1. 校验存在
        validateFacePackExists(reqVO.getId());

        // 2. 更新
        ImFacePackDO updateObj = BeanUtils.toBean(reqVO, ImFacePackDO.class);
        facePackMapper.updateById(updateObj);
    }

    @Override
    public void deleteFacePack(Long id) {
        // 1.1 校验存在
        validateFacePackExists(id);
        // 1.2 校验表情包下没有表情；防止误删表情包导致历史 face 消息无法回查归属
        if (facePackItemService.getFacePackItemCount(id) > 0) {
            throw exception(FACE_PACK_HAS_ITEMS);
        }

        // 2. 删除
        facePackMapper.deleteById(id);
    }

    @Override
    public void deleteFacePackList(List<Long> ids) {
        // 1. 任一存在表情则拒绝整批删除，避免「只删一半」的中间态
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        if (facePackItemService.getFacePackItemCount(ids) > 0) {
            throw exception(FACE_PACK_HAS_ITEMS);
        }

        // 2. 删除
        facePackMapper.deleteByIds(ids);
    }

}
