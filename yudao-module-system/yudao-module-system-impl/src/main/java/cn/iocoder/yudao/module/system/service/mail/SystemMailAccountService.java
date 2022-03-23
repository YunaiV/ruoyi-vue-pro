package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.SystemMailAccountBaseVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.SystemMailAccountDO;

import java.util.List;

// TODO @ジョイイ：类注释，应该是 邮箱账号 Service 接口

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface SystemMailAccountService  {

    Long create(SystemMailAccountBaseVO baseVO);

    String update(SystemMailAccountBaseVO baseVO);

    String delete(SystemMailAccountBaseVO baseVO);

    SystemMailAccountDO getMailAccount(Long id);

    PageResult<SystemMailAccountDO> getMailAccountPage(PageParam pageParam);

    List<SystemMailAccountDO> getMailAccountList();
}
