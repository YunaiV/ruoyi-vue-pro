package cn.iocoder.yudao.module.system.service.mail.impl;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.SystemMailAccountBaseVO;
import cn.iocoder.yudao.module.system.convert.mail.SystemMailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.SystemMailAccountDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.SystemMailAccountMapper;
import cn.iocoder.yudao.module.system.service.mail.SystemMailAccountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

// TODO @ジョイイ：类注释，应该是 邮箱账号 Service 实现类

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
public class SystemMailAccountServiceImpl  implements SystemMailAccountService {
    // TODO @ジョイイ： private
    @Resource
    SystemMailAccountMapper systemMailAccountMapper;  // TODO @ジョイイ： 变量，和方法要空一行
    @Override
    public Long create(SystemMailAccountBaseVO baseVO) {
        // TODO @ジョイイ： username 要校验唯一
        SystemMailAccountDO systemMailAccountDO = SystemMailAccountConvert.INSTANCE.convert(baseVO);
        systemMailAccountMapper.insert(systemMailAccountDO);
        return systemMailAccountDO.getId();
    }

    // TODO @ジョイイ： 不用返回值，void 即可
    @Override
    public String update(SystemMailAccountBaseVO baseVO) {
        // TODO @ジョイイ： username 要校验唯一
        // TODO @ジョイイ： 校验是否存在
        SystemMailAccountDO systemMailAccountDO = SystemMailAccountConvert.INSTANCE.convert(baseVO);
        systemMailAccountMapper.updateById(systemMailAccountDO);
        return null;
    }

    // TODO @ジョイイ： 不用返回值，void 即可

    @Override
    public String delete(SystemMailAccountBaseVO baseVO) {
        // TODO @ジョイイ： 校验是否存在
        SystemMailAccountDO systemMailAccountDO = SystemMailAccountConvert.INSTANCE.convert(baseVO);
        systemMailAccountMapper.deleteById(systemMailAccountDO);
        return null;
    }

    @Override
    public SystemMailAccountDO getMailAccount(Long id) {
        return systemMailAccountMapper.selectById(id);
    }

    @Override
    public PageResult<SystemMailAccountDO> getMailAccountPage(PageParam pageParam) {
        return systemMailAccountMapper.selectPage(pageParam);
    }

    @Override
    public List<SystemMailAccountDO> getMailAccountList() {
        return systemMailAccountMapper.selectList();
    }

}
