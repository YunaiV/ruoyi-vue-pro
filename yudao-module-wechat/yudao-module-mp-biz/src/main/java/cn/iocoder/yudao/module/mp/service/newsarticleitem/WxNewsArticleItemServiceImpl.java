package cn.iocoder.yudao.module.mp.service.newsarticleitem;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.newsarticleitem.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.newsarticleitem.WxNewsArticleItemDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.newsarticleitem.WxNewsArticleItemConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.newsarticleitem.WxNewsArticleItemMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 图文消息文章列表表  Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxNewsArticleItemServiceImpl implements WxNewsArticleItemService {

    @Resource
    private WxNewsArticleItemMapper wxNewsArticleItemMapper;

    @Override
    public Integer createWxNewsArticleItem(WxNewsArticleItemCreateReqVO createReqVO) {
        // 插入
        WxNewsArticleItemDO wxNewsArticleItem = WxNewsArticleItemConvert.INSTANCE.convert(createReqVO);
        wxNewsArticleItemMapper.insert(wxNewsArticleItem);
        // 返回
        return wxNewsArticleItem.getId();
    }

    @Override
    public void updateWxNewsArticleItem(WxNewsArticleItemUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxNewsArticleItemExists(updateReqVO.getId());
        // 更新
        WxNewsArticleItemDO updateObj = WxNewsArticleItemConvert.INSTANCE.convert(updateReqVO);
        wxNewsArticleItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxNewsArticleItem(Integer id) {
        // 校验存在
        this.validateWxNewsArticleItemExists(id);
        // 删除
        wxNewsArticleItemMapper.deleteById(id);
    }

    private void validateWxNewsArticleItemExists(Integer id) {
        if (wxNewsArticleItemMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxNewsArticleItemDO getWxNewsArticleItem(Integer id) {
        return wxNewsArticleItemMapper.selectById(id);
    }

    @Override
    public List<WxNewsArticleItemDO> getWxNewsArticleItemList(Collection<Integer> ids) {
        return wxNewsArticleItemMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxNewsArticleItemDO> getWxNewsArticleItemPage(WxNewsArticleItemPageReqVO pageReqVO) {
        return wxNewsArticleItemMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxNewsArticleItemDO> getWxNewsArticleItemList(WxNewsArticleItemExportReqVO exportReqVO) {
        return wxNewsArticleItemMapper.selectList(exportReqVO);
    }

}
