package cn.iocoder.yudao.module.mp.service.account;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;

import jakarta.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants.ACCOUNT_NOT_EXISTS;

/**
 * 公众号账号 Service 接口
 *
 * @author 芋道源码
 */
public interface MpAccountService {

    /**
     * 初始化缓存
     */
    void initLocalCache();

    /**
     * 创建公众号账号
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAccount(@Valid MpAccountCreateReqVO createReqVO);

    /**
     * 更新公众号账号
     *
     * @param updateReqVO 更新信息
     */
    void updateAccount(@Valid MpAccountUpdateReqVO updateReqVO);

    /**
     * 删除公众号账号
     *
     * @param id 编号
     */
    void deleteAccount(Long id);

    /**
     * 获得公众号账号
     *
     * @param id 编号
     * @return 公众号账号
     */
    MpAccountDO getAccount(Long id);

    /**
     * 获得公众号账号。若不存在，则抛出业务异常
     *
     * @param id 编号
     * @return 公众号账号
     */
    default MpAccountDO getRequiredAccount(Long id) {
        MpAccountDO account = getAccount(id);
        if (account == null) {
            throw exception(ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    /**
     * 从缓存中，获得公众号账号
     *
     * @param appId 微信公众号 appId
     * @return 公众号账号
     */
    MpAccountDO getAccountFromCache(String appId);

    /**
     * 获得公众号账号分页
     *
     * @param pageReqVO 分页查询
     * @return 公众号账号分页
     */
    PageResult<MpAccountDO> getAccountPage(MpAccountPageReqVO pageReqVO);

    /**
     * 获得公众号账号列表
     *
     * @return 公众号账号列表
     */
    List<MpAccountDO> getAccountList();

    /**
     * 生成公众号账号的二维码
     *
     * @param id 编号
     */
    void generateAccountQrCode(Long id);

    /**
     * 清空公众号账号的 API 配额
     *
     * 参考文档：<a href="https://developers.weixin.qq.com/doc/offiaccount/Message_Management/API_Call_Limits.html">接口调用频次限制说明</a>
     *
     * @param id 编号
     */
    void clearAccountQuota(Long id);

}
