package cn.iocoder.yudao.module.mp.service.accountfanstag;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.accountfanstag.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfanstag.WxAccountFansTagDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import me.chanjar.weixin.mp.bean.result.WxMpUser;

/**
 * 粉丝标签关联 Service 接口
 *
 * @author 芋道源码
 */
public interface WxAccountFansTagService {

    /**
     * 创建粉丝标签关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxAccountFansTag(@Valid WxAccountFansTagCreateReqVO createReqVO);

    /**
     * 更新粉丝标签关联
     *
     * @param updateReqVO 更新信息
     */
    void updateWxAccountFansTag(@Valid WxAccountFansTagUpdateReqVO updateReqVO);

    /**
     * 删除粉丝标签关联
     *
     * @param id 编号
     */
    void deleteWxAccountFansTag(Integer id);

    /**
     * 获得粉丝标签关联
     *
     * @param id 编号
     * @return 粉丝标签关联
     */
    WxAccountFansTagDO getWxAccountFansTag(Integer id);

    /**
     * 获得粉丝标签关联列表
     *
     * @param ids 编号
     * @return 粉丝标签关联列表
     */
    List<WxAccountFansTagDO> getWxAccountFansTagList(Collection<Integer> ids);

    /**
     * 获得粉丝标签关联分页
     *
     * @param pageReqVO 分页查询
     * @return 粉丝标签关联分页
     */
    PageResult<WxAccountFansTagDO> getWxAccountFansTagPage(WxAccountFansTagPageReqVO pageReqVO);

    /**
     * 获得粉丝标签关联列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 粉丝标签关联列表
     */
    List<WxAccountFansTagDO> getWxAccountFansTagList(WxAccountFansTagExportReqVO exportReqVO);

    void processFansTags(WxAccountDO wxAccount, WxMpUser wxmpUser);
}
