package cn.iocoder.yudao.module.oa.service.mail;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailFolderDO;
import cn.iocoder.yudao.module.oa.dal.mysql.mail.OaMailFolderMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.MAIL_FOLDER_NOT_AVAILABLE;

/**
 * 企业邮箱文件夹 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OaMailFolderServiceImpl implements OaMailFolderService {

    @Resource
    private OaMailFolderMapper mailFolderMapper;

    @Override
    public List<OaMailFolderDO> getMailFolderList(Long accountId) {
        return mailFolderMapper.selectListByAccountId(accountId);
    }

    @Override
    public OaMailFolderDO getMailFolder(Long id) {
        return mailFolderMapper.selectById(id);
    }

    @Override
    public OaMailFolderDO validateMailFolder(Long id, Long accountId) {
        // 1. 校验文件夹存在且可用
        OaMailFolderDO folder = mailFolderMapper.selectById(id);
        if (folder == null || Boolean.FALSE.equals(folder.getAvailable())) {
            throw exception(MAIL_FOLDER_NOT_AVAILABLE);
        }
        // 2. 校验文件夹账号归属
        if (ObjUtil.notEqual(folder.getAccountId(), accountId)) {
            throw exception(MAIL_FOLDER_NOT_AVAILABLE);
        }
        return folder;
    }

    @Override
    public Long createMailFolder(OaMailFolderDO folder) {
        // 1. 保存文件夹索引
        mailFolderMapper.insert(folder);
        return folder.getId();
    }

    @Override
    public void updateMailFolder(OaMailFolderDO folder) {
        // 1. 更新文件夹同步信息
        mailFolderMapper.updateById(folder);
    }

}
