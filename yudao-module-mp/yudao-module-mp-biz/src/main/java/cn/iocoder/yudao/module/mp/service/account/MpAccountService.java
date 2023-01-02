package cn.iocoder.yudao.module.mp.service.account;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import javax.validation.Valid;

/**
 * 公众号账户 Service 接口
 *
 * @author 芋道源码
 */
public interface MpAccountService {

    /**
     * 初始化缓存
     */
    void initLocalCache();

    /**
     * 创建公众号账户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAccount(@Valid MpAccountCreateReqVO createReqVO);

    /**
     * 更新公众号账户
     *
     * @param updateReqVO 更新信息
     */
    void updateAccount(@Valid MpAccountUpdateReqVO updateReqVO);

    /**
     * 删除公众号账户
     *
     * @param id 编号
     */
    void deleteAccount(Long id);

    /**
     * 获得公众号账户
     *
     * @param id 编号
     * @return 公众号账户
     */
    MpAccountDO getAccount(Long id);

    /**
     * 从缓存中，获得公众号账户
     *
     * @param appId 微信公众号 appId
     * @return 公众号账户
     */
    MpAccountDO getAccountFromCache(String appId);

    /**
     * 获得公众号账户分页
     *
     * @param pageReqVO 分页查询
     * @return 公众号账户分页
     */
    PageResult<MpAccountDO> getAccountPage(MpAccountPageReqVO pageReqVO);

    // TODO 芋艿：去除这样的方法
    /**
     * 查询账户
     *
     * @param field
     * @param val
     * @return
     */
    MpAccountDO findBy(SFunction<MpAccountDO, ?> field, Object val);

}
