package cn.iocoder.yudao.module.mp.service.accountfanstag;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.accountfanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfanstag.WxAccountFansTagDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.accountfanstag.WxAccountFansTagConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.accountfanstag.WxAccountFansTagMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 粉丝标签关联 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxAccountFansTagServiceImpl implements WxAccountFansTagService {

    @Resource
    private WxAccountFansTagMapper wxAccountFansTagMapper;

    @Override
    public Integer createWxAccountFansTag(WxAccountFansTagCreateReqVO createReqVO) {
        // 插入
        WxAccountFansTagDO wxAccountFansTag = WxAccountFansTagConvert.INSTANCE.convert(createReqVO);
        wxAccountFansTagMapper.insert(wxAccountFansTag);
        // 返回
        return wxAccountFansTag.getId();
    }

    @Override
    public void updateWxAccountFansTag(WxAccountFansTagUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxAccountFansTagExists(updateReqVO.getId());
        // 更新
        WxAccountFansTagDO updateObj = WxAccountFansTagConvert.INSTANCE.convert(updateReqVO);
        wxAccountFansTagMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxAccountFansTag(Integer id) {
        // 校验存在
        this.validateWxAccountFansTagExists(id);
        // 删除
        wxAccountFansTagMapper.deleteById(id);
    }

    private void validateWxAccountFansTagExists(Integer id) {
        if (wxAccountFansTagMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxAccountFansTagDO getWxAccountFansTag(Integer id) {
        return wxAccountFansTagMapper.selectById(id);
    }

    @Override
    public List<WxAccountFansTagDO> getWxAccountFansTagList(Collection<Integer> ids) {
        return wxAccountFansTagMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxAccountFansTagDO> getWxAccountFansTagPage(WxAccountFansTagPageReqVO pageReqVO) {
        return wxAccountFansTagMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxAccountFansTagDO> getWxAccountFansTagList(WxAccountFansTagExportReqVO exportReqVO) {
        return wxAccountFansTagMapper.selectList(exportReqVO);
    }

    @Override
    public void processFansTags(WxAccountDO wxAccountDO, WxMpUser wxmpUser) {
        WxAccountFansTagExportReqVO wxAccountFansTagTpl = new WxAccountFansTagExportReqVO();
        wxAccountFansTagTpl.setOpenid(wxmpUser.getOpenId());
        List<WxAccountFansTagDO> wxAccountFansTagList = this.getWxAccountFansTagList(wxAccountFansTagTpl);
        wxAccountFansTagList.forEach(temp -> this.deleteWxAccountFansTag(temp.getId()));

        Long[] tagIds = wxmpUser.getTagIds();
        for (Long tagId : tagIds) {
            WxAccountFansTagCreateReqVO wxAccountFansTag = new WxAccountFansTagCreateReqVO();
            wxAccountFansTag.setOpenid(wxmpUser.getOpenId());
            wxAccountFansTag.setTagId(String.valueOf(tagId));
            wxAccountFansTag.setWxAccountId(String.valueOf(wxAccountDO.getId()));
            this.createWxAccountFansTag(wxAccountFansTag);
        }
    }


}
