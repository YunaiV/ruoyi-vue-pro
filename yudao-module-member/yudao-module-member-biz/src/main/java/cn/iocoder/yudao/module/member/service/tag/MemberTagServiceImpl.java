package cn.iocoder.yudao.module.member.service.tag;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.MemberTagCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.MemberTagExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.MemberTagPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.MemberTagUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.tag.MemberTagConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.tag.MemberTagDO;
import cn.iocoder.yudao.module.member.dal.mysql.tag.MemberTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.TAG_NAME_EXISTS;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.TAG_NOT_EXISTS;

/**
 * 会员标签 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MemberTagServiceImpl implements MemberTagService {

    @Resource
    private MemberTagMapper tagMapper;

    @Override
    public Long createTag(MemberTagCreateReqVO createReqVO) {
        // 校验名称唯一
        validateTagNameUnique(null, createReqVO.getName());
        // 插入
        MemberTagDO tag = MemberTagConvert.INSTANCE.convert(createReqVO);
        tagMapper.insert(tag);
        // 返回
        return tag.getId();
    }

    @Override
    public void updateTag(MemberTagUpdateReqVO updateReqVO) {
        // 校验存在
        validateTagExists(updateReqVO.getId());
        // 校验名称唯一
        validateTagNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        MemberTagDO updateObj = MemberTagConvert.INSTANCE.convert(updateReqVO);
        tagMapper.updateById(updateObj);
    }

    @Override
    public void deleteTag(Long id) {
        // 校验存在
        validateTagExists(id);
        // 删除
        tagMapper.deleteById(id);
    }

    private void validateTagExists(Long id) {
        if (tagMapper.selectById(id) == null) {
            throw exception(TAG_NOT_EXISTS);
        }
    }

    private void validateTagNameUnique(Long id, String name) {
        boolean exists = tagMapper.exists(id, name);
        if (exists) {
            throw exception(TAG_NAME_EXISTS);
        }
    }

    @Override
    public MemberTagDO getTag(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public List<MemberTagDO> getTagList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return tagMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberTagDO> getTagPage(MemberTagPageReqVO pageReqVO) {
        return tagMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MemberTagDO> getTagList(MemberTagExportReqVO exportReqVO) {
        return tagMapper.selectList(exportReqVO);
    }

}
