package cn.iocoder.yudao.module.mp.service.fanstag;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fanstag.WxFansTagDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 粉丝标签 Service 接口
 *
 * @author 芋道源码
 */
public interface WxFansTagService {

    /**
     * 创建粉丝标签
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxFansTag(@Valid WxFansTagCreateReqVO createReqVO);

    /**
     * 更新粉丝标签
     *
     * @param updateReqVO 更新信息
     */
    void updateWxFansTag(@Valid WxFansTagUpdateReqVO updateReqVO);

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
    WxFansTagDO getWxFansTag(Integer id);

    /**
     * 获得粉丝标签列表
     *
     * @param ids 编号
     * @return 粉丝标签列表
     */
    List<WxFansTagDO> getWxFansTagList(Collection<Integer> ids);

    /**
     * 获得粉丝标签分页
     *
     * @param pageReqVO 分页查询
     * @return 粉丝标签分页
     */
    PageResult<WxFansTagDO> getWxFansTagPage(WxFansTagPageReqVO pageReqVO);

    /**
     * 获得粉丝标签列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 粉丝标签列表
     */
    List<WxFansTagDO> getWxFansTagList(WxFansTagExportReqVO exportReqVO);

}
