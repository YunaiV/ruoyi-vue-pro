package cn.iocoder.yudao.module.oa.service.mail;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.account.OaMailAccountSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailAccountDO;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 企业邮箱账号 Service 接口
 *
 * @author 芋道源码
 */
public interface OaMailAccountService {

    /**
     * 锁定当前用户的邮箱账号
     *
     * @param id 邮箱账号编号
     * @param userId 当前用户编号
     * @return 锁定的邮箱账号
     */
    OaMailAccountDO lockMailAccount(Long id, Long userId);

    /**
     * 创建邮箱账号
     *
     * @param reqVO 邮箱账号信息
     * @param userId 当前用户编号
     * @return 邮箱账号编号
     */
    Long createMailAccount(@Valid OaMailAccountSaveReqVO reqVO, Long userId);

    /**
     * 更新邮箱账号
     *
     * @param reqVO 邮箱账号信息，密码为空时保留原密码
     * @param userId 当前用户编号
     */
    void updateMailAccount(@Valid OaMailAccountSaveReqVO reqVO, Long userId);

    /**
     * 删除邮箱账号
     *
     * @param id 邮箱账号编号
     * @param userId 当前用户编号
     */
    void deleteMailAccount(Long id, Long userId);

    /**
     * 设置默认邮箱账号
     *
     * @param id 邮箱账号编号
     * @param userId 当前用户编号
     */
    void updateMailAccountDefault(Long id, Long userId);

    /**
     * 获得当前用户的邮箱账号
     *
     * @param id 邮箱账号编号
     * @param userId 当前用户编号
     * @return 邮箱账号
     */
    OaMailAccountDO validateMailAccount(Long id, Long userId);

    /**
     * 获得当前用户的邮箱账号列表
     *
     * @param userId 当前用户编号
     * @param status 账号状态，空时查询全部状态
     * @return 邮箱账号列表
     */
    List<OaMailAccountDO> getMailAccountListByUserIdAndStatus(Long userId, Integer status);

    /**
     * 获得当前租户指定状态的邮箱地址列表
     *
     * @param status 账号状态
     * @return 邮箱账号列表，仅包含地址和归属人
     */
    List<OaMailAccountDO> getMailAccountListByStatus(Integer status);

    /**
     * 获得邮箱服务配置关联的账号数量
     *
     * @param providerId 邮箱服务配置编号
     * @return 邮箱账号数量
     */
    Long getMailAccountCountByProviderId(Long providerId);

    /**
     * 测试邮箱账号连接：分别测试 IMAP 和 SMTP 认证，不发送邮件
     *
     * @param id 邮箱账号编号
     * @param userId 当前用户编号
     * @return IMAP、SMTP 协议与连接是否成功的映射
     */
    Map<String, Boolean> testMailAccountConnection(Long id, Long userId);

}
