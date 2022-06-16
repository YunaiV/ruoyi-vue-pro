package cn.iocoder.yudao.module.mp.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagUpdateReqVO;
import cn.iocoder.yudao.module.mp.convert.fanstag.WxFansTagConvert;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
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
    public WxUserTag createWxFansTag(FansTagCreateReqVO createReqVO) {
        try {
            return wxMpService.getUserTagService().tagCreate("wxFansTag");
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateWxFansTag(FansTagUpdateReqVO updateReqVO) {
        // 校验存在
        // 更新
        WxUserTag updateObj = WxFansTagConvert.INSTANCE.convert(updateReqVO);

    }

    @Override
    public void deleteWxFansTag(Integer id) {
        // 校验存在
        // 删除
    }


    @Override
    public WxUserTag getWxFansTag(Integer id) {
        return null;
    }

    @Override
    public List<WxUserTag> getWxFansTagList(Collection<Integer> ids) {
        return null;
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
