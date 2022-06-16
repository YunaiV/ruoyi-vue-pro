package cn.iocoder.yudao.module.mp.service.fanstag;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fanstag.WxFansTagDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.fanstag.WxFansTagConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.fanstag.WxFansTagMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 粉丝标签 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxFansTagServiceImpl implements WxFansTagService {

    @Resource
    private WxFansTagMapper wxFansTagMapper;

    @Override
    public Integer createWxFansTag(WxFansTagCreateReqVO createReqVO) {
        // 插入
        WxFansTagDO wxFansTag = WxFansTagConvert.INSTANCE.convert(createReqVO);
        wxFansTagMapper.insert(wxFansTag);
        // 返回
        return wxFansTag.getId();
    }

    @Override
    public void updateWxFansTag(WxFansTagUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxFansTagExists(updateReqVO.getId());
        // 更新
        WxFansTagDO updateObj = WxFansTagConvert.INSTANCE.convert(updateReqVO);
        wxFansTagMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxFansTag(Integer id) {
        // 校验存在
        this.validateWxFansTagExists(id);
        // 删除
        wxFansTagMapper.deleteById(id);
    }

    private void validateWxFansTagExists(Integer id) {
        if (wxFansTagMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxFansTagDO getWxFansTag(Integer id) {
        return wxFansTagMapper.selectById(id);
    }

    @Override
    public List<WxFansTagDO> getWxFansTagList(Collection<Integer> ids) {
        return wxFansTagMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxFansTagDO> getWxFansTagPage(WxFansTagPageReqVO pageReqVO) {
        return wxFansTagMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxFansTagDO> getWxFansTagList(WxFansTagExportReqVO exportReqVO) {
        return wxFansTagMapper.selectList(exportReqVO);
    }

}
