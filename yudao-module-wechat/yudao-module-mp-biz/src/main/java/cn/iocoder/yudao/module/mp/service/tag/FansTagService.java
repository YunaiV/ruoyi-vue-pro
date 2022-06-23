package cn.iocoder.yudao.module.mp.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagUpdateReqVO;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;

import javax.validation.Valid;
import java.util.List;

/**
 * 粉丝标签 Service 接口
 *
 * @author fengdan
 */
public interface FansTagService {

    /**
     * 创建粉丝标签
     *
     * @param createReqVO 创建标签信息
     * @return {@link WxUserTag}  用户标签对象
     * @throws WxErrorException 微信异常
     */
    WxUserTag createWxFansTag(FansTagCreateReqVO createReqVO) throws WxErrorException;

    /**
     * 更新粉丝标签
     *
     * @param updateReqVO 更新信息
     * @return {@link         Boolean}
     * @throws WxErrorException 微信异常
     */
    Boolean updateWxFansTag(@Valid FansTagUpdateReqVO updateReqVO) throws WxErrorException;

    /**
     * 删除粉丝标签
     *
     * @param id    编号
     * @param appId 公众号appId
     * @return {@link         Boolean}
     * @throws WxErrorException 微信异常
     */
    Boolean deleteWxFansTag(Long id, String appId) throws WxErrorException;

    /**
     * 获取公众号已创建的标签
     *
     * @param appId 公众号appId
     * @return 粉丝标签列表
     * @throws WxErrorException 微信异常
     */
    List<WxUserTag> getWxFansTagList(String appId) throws WxErrorException;

    /**
     * 获得粉丝标签分页
     *
     * @param pageReqVO 分页查询
     * @return 粉丝标签分页
     */
    PageResult<WxUserTag> getWxFansTagPage(FansTagPageReqVO pageReqVO);

    /**
     * 获得粉丝标签列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 粉丝标签列表
     */
    List<WxUserTag> getWxFansTagList(FansTagExportReqVO exportReqVO);

}
