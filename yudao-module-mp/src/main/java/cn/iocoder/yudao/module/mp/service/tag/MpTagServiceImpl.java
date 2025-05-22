package cn.iocoder.yudao.module.mp.service.tag;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagUpdateReqVO;
import cn.iocoder.yudao.module.mp.convert.tag.MpTagConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.tag.MpTagDO;
import cn.iocoder.yudao.module.mp.dal.mysql.tag.MpTagMapper;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.service.account.MpAccountService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

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
    private MpAccountService mpAccountService;

    @Resource
    @Lazy // 延迟加载，为了解决延迟加载
    private MpServiceFactory mpServiceFactory;

    @Override
    public Long createTag(MpTagCreateReqVO createReqVO) {
        // 获得公众号账号
        MpAccountDO account = mpAccountService.getRequiredAccount(createReqVO.getAccountId());

        // 第一步，新增标签到公众号平台。标签名的唯一，交给公众号平台
        WxMpService mpService = mpServiceFactory.getRequiredMpService(createReqVO.getAccountId());
        WxUserTag wxTag;
        try {
            wxTag = mpService.getUserTagService().tagCreate(createReqVO.getName());
        } catch (WxErrorException e) {
            throw exception(TAG_CREATE_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，新增标签到数据库
        MpTagDO tag = MpTagConvert.INSTANCE.convert(wxTag, account);
        mpTagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void updateTag(MpTagUpdateReqVO updateReqVO) {
        // 校验标签存在
        MpTagDO tag = validateTagExists(updateReqVO.getId());

        // 第一步，更新标签到公众号平台。标签名的唯一，交给公众号平台
        WxMpService mpService = mpServiceFactory.getRequiredMpService(tag.getAccountId());
        try {
            mpService.getUserTagService().tagUpdate(tag.getTagId(), updateReqVO.getName());
        } catch (WxErrorException e) {
            throw exception(TAG_UPDATE_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，更新标签到数据库
        mpTagMapper.updateById(new MpTagDO().setId(tag.getId()).setName(updateReqVO.getName()));
    }

    @Override
    public void deleteTag(Long id) {
        // 校验标签存在
        MpTagDO tag = validateTagExists(id);

        // 第一步，删除标签到公众号平台。
        WxMpService mpService = mpServiceFactory.getRequiredMpService(tag.getAccountId());
        try {
            mpService.getUserTagService().tagDelete(tag.getTagId());
        } catch (WxErrorException e) {
            throw exception(TAG_DELETE_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，删除标签到数据库
        mpTagMapper.deleteById(tag.getId());
    }

    private MpTagDO validateTagExists(Long id) {
        MpTagDO tag = mpTagMapper.selectById(id);
        if (tag == null) {
            throw exception(TAG_NOT_EXISTS);
        }
        return tag;
    }

    @Override
    public PageResult<MpTagDO> getTagPage(MpTagPageReqVO pageReqVO) {
        return mpTagMapper.selectPage(pageReqVO);
    }

    @Override
    public MpTagDO get(Long id) {
        return mpTagMapper.selectById(id);
    }

    @Override
    public List<MpTagDO> getTagList() {
        return mpTagMapper.selectList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncTag(Long accountId) {
        MpAccountDO account = mpAccountService.getRequiredAccount(accountId);

        // 第一步，从公众号平台获取最新的标签列表
        WxMpService mpService = mpServiceFactory.getRequiredMpService(accountId);
        List<WxUserTag> wxTags;
        try {
            wxTags = mpService.getUserTagService().tagGet();
        } catch (WxErrorException e) {
            throw exception(TAG_GET_FAIL, e.getError().getErrorMsg());
        }

        // 第二步，合并更新回自己的数据库；由于标签只有 100 个，所以直接 for 循环操作
        Map<Long, MpTagDO> tagMap = convertMap(mpTagMapper.selectListByAccountId(accountId),
                MpTagDO::getTagId);
        wxTags.forEach(wxTag -> {
            MpTagDO tag = tagMap.remove(wxTag.getId());
            // 情况一，不存在，新增
            if (tag == null) {
                tag = MpTagConvert.INSTANCE.convert(wxTag, account);
                mpTagMapper.insert(tag);
                return;
            }
            // 情况二，存在，则更新
            mpTagMapper.updateById(new MpTagDO().setId(tag.getId())
                    .setName(wxTag.getName()).setCount(wxTag.getCount()));
        });
        // 情况三，部分标签已经不存在了，删除
        if (CollUtil.isNotEmpty(tagMap)) {
            mpTagMapper.deleteBatchIds(convertList(tagMap.values(), MpTagDO::getId));
        }
    }

}
