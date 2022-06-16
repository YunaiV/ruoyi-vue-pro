package cn.iocoder.yudao.module.mp.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.FansTagUpdateReqVO;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;

import javax.validation.Valid;
import java.util.Collection;
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
     * @param createReqVO 创建信息
     * @return 编号
     */
    WxUserTag createWxFansTag(@Valid FansTagCreateReqVO createReqVO);

    /**
     * 更新粉丝标签
     *
     * @param updateReqVO 更新信息
     */
    void updateWxFansTag(@Valid FansTagUpdateReqVO updateReqVO);

    /**
     * 删除粉丝标签
     *
     * @param id 编号
     */
    void deleteWxFansTag(Integer id);

    /**
     * 获得粉丝标签
     *
     * @param id 编号
     * @return 粉丝标签
     */
    WxUserTag getWxFansTag(Integer id);

    /**
     * 获得粉丝标签列表
     *
     * @param ids 编号
     * @return 粉丝标签列表
     */
    List<WxUserTag> getWxFansTagList(Collection<Integer> ids);

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
