package cn.iocoder.yudao.module.mp.service.subscribetext;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.mp.controller.admin.subscribetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.subscribetext.WxSubscribeTextDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.mp.convert.subscribetext.WxSubscribeTextConvert;
import cn.iocoder.yudao.module.mp.dal.mysql.subscribetext.WxSubscribeTextMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.*;

/**
 * 关注欢迎语 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WxSubscribeTextServiceImpl implements WxSubscribeTextService {

    @Resource
    private WxSubscribeTextMapper wxSubscribeTextMapper;

    @Override
    public Integer createWxSubscribeText(WxSubscribeTextCreateReqVO createReqVO) {
        // 插入
        WxSubscribeTextDO wxSubscribeText = WxSubscribeTextConvert.INSTANCE.convert(createReqVO);
        wxSubscribeTextMapper.insert(wxSubscribeText);
        // 返回
        return wxSubscribeText.getId();
    }

    @Override
    public void updateWxSubscribeText(WxSubscribeTextUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxSubscribeTextExists(updateReqVO.getId());
        // 更新
        WxSubscribeTextDO updateObj = WxSubscribeTextConvert.INSTANCE.convert(updateReqVO);
        wxSubscribeTextMapper.updateById(updateObj);
    }

    @Override
    public void deleteWxSubscribeText(Integer id) {
        // 校验存在
        this.validateWxSubscribeTextExists(id);
        // 删除
        wxSubscribeTextMapper.deleteById(id);
    }

    private void validateWxSubscribeTextExists(Integer id) {
        if (wxSubscribeTextMapper.selectById(id) == null) {
            throw exception(COMMON_NOT_EXISTS);
        }
    }

    @Override
    public WxSubscribeTextDO getWxSubscribeText(Integer id) {
        return wxSubscribeTextMapper.selectById(id);
    }

    @Override
    public List<WxSubscribeTextDO> getWxSubscribeTextList(Collection<Integer> ids) {
        return wxSubscribeTextMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxSubscribeTextDO> getWxSubscribeTextPage(WxSubscribeTextPageReqVO pageReqVO) {
        return wxSubscribeTextMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxSubscribeTextDO> getWxSubscribeTextList(WxSubscribeTextExportReqVO exportReqVO) {
        return wxSubscribeTextMapper.selectList(exportReqVO);
    }

    @Override
    public WxSubscribeTextDO findBy(SFunction<WxSubscribeTextDO, ?> column, Object obj) {
        return wxSubscribeTextMapper.selectOne(column, obj);
    }
}
