package cn.iocoder.yudao.module.oa.service.note;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.category.OaNoteCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteCategoryDO;
import cn.iocoder.yudao.module.oa.dal.mysql.note.OaNoteCategoryMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.NOTE_CATEGORY_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.NOTE_CATEGORY_NOT_EXISTS;

/**
 * OA 笔记目录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaNoteCategoryServiceImpl implements OaNoteCategoryService {

    @Resource
    private OaNoteCategoryMapper noteCategoryMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaNoteService noteService;

    @Override
    public Long createNoteCategory(OaNoteCategorySaveReqVO createReqVO, Long userId) {
        // 1. 校验目录名称唯一
        validateNoteCategoryNameUnique(null, userId, createReqVO.getName());

        // 2. 新增笔记目录
        OaNoteCategoryDO category = BeanUtils.toBean(createReqVO, OaNoteCategoryDO.class).setUserId(userId);
        noteCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateNoteCategory(OaNoteCategorySaveReqVO updateReqVO, Long userId) {
        // 1.1 校验笔记目录属于当前用户
        validateNoteCategoryOwner(updateReqVO.getId(), userId);
        // 1.2 校验目录名称唯一
        validateNoteCategoryNameUnique(updateReqVO.getId(), userId, updateReqVO.getName());

        // 2. 更新笔记目录
        noteCategoryMapper.updateById(BeanUtils.toBean(updateReqVO, OaNoteCategoryDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNoteCategory(Long id, Long userId) {
        // 1. 校验笔记目录属于当前用户
        validateNoteCategoryOwner(id, userId);

        // 2.1 删除目录下的笔记
        noteService.deleteNotesByCategoryId(id, userId);

        // 2.2 删除笔记目录
        noteCategoryMapper.deleteById(id);
    }

    @Override
    public OaNoteCategoryDO getNoteCategory(Long id, Long userId) {
        return validateNoteCategoryOwner(id, userId);
    }

    @Override
    public List<OaNoteCategoryDO> getNoteCategoryList(Long userId) {
        return noteCategoryMapper.selectListByUserId(userId);
    }

    @Override
    public OaNoteCategoryDO validateNoteCategoryOwner(Long id, Long userId) {
        if (id == null) {
            return null;
        }
        OaNoteCategoryDO category = noteCategoryMapper.selectById(id);
        if (category == null || ObjectUtil.notEqual(category.getUserId(), userId)) {
            throw exception(NOTE_CATEGORY_NOT_EXISTS);
        }
        return category;
    }

    @Override
    public List<OaNoteCategoryDO> getNoteCategoryList(Collection<Long> ids) {
        return CollUtil.isEmpty(ids) ? Collections.emptyList() : noteCategoryMapper.selectByIds(ids);
    }

    /**
     * 校验同一用户的目录名称唯一
     *
     * @param id 当前目录编号，新增时为空
     * @param userId 用户编号
     * @param name 目录名称
     */
    private void validateNoteCategoryNameUnique(Long id, Long userId, String name) {
        OaNoteCategoryDO category = noteCategoryMapper.selectByUserIdAndName(userId, name);
        if (category != null && ObjectUtil.notEqual(category.getId(), id)) {
            throw exception(NOTE_CATEGORY_NAME_DUPLICATE);
        }
    }

}
