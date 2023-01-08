package cn.iocoder.yudao.module.mp.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.tag.MpTagDO;
import cn.iocoder.yudao.module.mp.dal.mysql.tag.MpTagMapper;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 公众号标签 Service 实现类
 *
 * @author fengdan
 */
@Slf4j
@Service
@Validated
public class MpTagServiceImpl implements MpTagService {

    @Resource
    private MpTagMapper mpTagMapper;

    @Resource
    @Lazy // 延迟加载，为了解决延迟加载
    private MpServiceFactory mpServiceFactory;

    @Override
    public Long createTag(MpTagCreateReqVO createReqVO) {
//        return wxMpService.getUserTagService().tagCreate(createReqVO.getName());
        return null;
    }

    @Override
    public void updateTag(MpTagUpdateReqVO updateReqVO) {
//        return wxMpService.getUserTagService().tagUpdate(updateReqVO.getId(), updateReqVO.getName());
    }

    @Override
    public void deleteTag(Long id) {
//        return wxMpService.getUserTagService().tagDelete(id);
    }

    @Override
    public PageResult<MpTagDO> getTagPage(MpTagPageReqVO pageReqVO) {
        return null;
    }

}
