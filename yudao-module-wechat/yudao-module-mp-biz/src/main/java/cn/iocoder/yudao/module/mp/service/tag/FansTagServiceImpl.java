package cn.iocoder.yudao.module.mp.service.tag;

import cn.hutool.core.util.ReUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagUpdateReqVO;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 粉丝标签 Service 实现类
 *
 * @author fengdan
 */
@Slf4j
@Service
@Validated
public class FansTagServiceImpl implements FansTagService {
    @Resource
    private WxMpService wxMpService;

    @Override
    public WxUserTag createWxFansTag(FansTagCreateReqVO createReqVO) throws WxErrorException {
        // TODO 切换公众号操作 调整为 aop 或者 过滤器\拦截器 处理
        wxMpService.switchover(createReqVO.getAppId());
        return wxMpService.getUserTagService().tagCreate(createReqVO.getName());
    }

    @Override
    public Boolean updateWxFansTag(FansTagUpdateReqVO updateReqVO) throws WxErrorException {
        wxMpService.switchover(updateReqVO.getAppId());
        return wxMpService.getUserTagService().tagUpdate(updateReqVO.getId(), updateReqVO.getName());
    }

    @Override
    public Boolean deleteWxFansTag(Long id, String appId) throws WxErrorException {
        wxMpService.switchover(appId);
        return wxMpService.getUserTagService().tagDelete(id);
    }


    @Override
    public List<WxUserTag> getWxFansTagList(String appId) throws WxErrorException {
        wxMpService.switchover(appId);
        return wxMpService.getUserTagService().tagGet();
    }

    @Override
    public PageResult<WxUserTag> getWxFansTagPage(FansTagPageReqVO pageReqVO) {
        return null;
    }

    @Override
    public List<WxUserTag> getWxFansTagList(FansTagExportReqVO exportReqVO) {
        return null;
    }

}
