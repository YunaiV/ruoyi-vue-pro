package cn.iocoder.yudao.module.wechatMp.service.accountfans;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.wechatMp.controller.admin.accountfans.vo.*;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.wechatMp.dal.dataobject.accountfans.WxAccountFansDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * 微信公众号粉丝 Service 接口
 *
 * @author 芋道源码
 */
public interface WxAccountFansService {

    /**
     * 创建微信公众号粉丝
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWxAccountFans(@Valid WxAccountFansCreateReqVO createReqVO);

    /**
     * 更新微信公众号粉丝
     *
     * @param updateReqVO 更新信息
     */
    void updateWxAccountFans(@Valid WxAccountFansUpdateReqVO updateReqVO);

    /**
     * 删除微信公众号粉丝
     *
     * @param id 编号
     */
    void deleteWxAccountFans(Long id);

    /**
     * 获得微信公众号粉丝
     *
     * @param id 编号
     * @return 微信公众号粉丝
     */
    WxAccountFansDO getWxAccountFans(Long id);

    /**
     * 获得微信公众号粉丝列表
     *
     * @param ids 编号
     * @return 微信公众号粉丝列表
     */
    List<WxAccountFansDO> getWxAccountFansList(Collection<Long> ids);

    /**
     * 获得微信公众号粉丝分页
     *
     * @param pageReqVO 分页查询
     * @return 微信公众号粉丝分页
     */
    PageResult<WxAccountFansDO> getWxAccountFansPage(WxAccountFansPageReqVO pageReqVO);

    /**
     * 获得微信公众号粉丝列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 微信公众号粉丝列表
     */
    List<WxAccountFansDO> getWxAccountFansList(WxAccountFansExportReqVO exportReqVO);

    /**
     * 查询粉丝
     *
     * @param sFunction
     * @param val
     * @return
     */
    WxAccountFansDO findBy(SFunction<WxAccountFansDO, ?> sFunction, Object val);
}
