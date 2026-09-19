package cn.iocoder.yudao.module.oa.service.mail;

import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailFolderDO;

import java.util.List;

/**
 * 企业邮箱文件夹 Service 接口
 *
 * @author 芋道源码
 */
public interface OaMailFolderService {

    /**
     * 获得邮箱文件夹列表
     *
     * @param accountId 邮箱账号编号
     * @return 文件夹列表
     */
    List<OaMailFolderDO> getMailFolderList(Long accountId);

    /**
     * 获得文件夹
     *
     * @param id 文件夹编号
     * @return 文件夹，不存在时返回 null
     */
    OaMailFolderDO getMailFolder(Long id);

    /**
     * 校验文件夹可用及账号归属
     *
     * @param id 文件夹编号
     * @param accountId 邮箱账号编号
     * @return 文件夹
     */
    OaMailFolderDO validateMailFolder(Long id, Long accountId);

    /**
     * 创建文件夹索引
     *
     * @param folder 文件夹信息
     * @return 文件夹编号
     */
    Long createMailFolder(OaMailFolderDO folder);

    /**
     * 更新文件夹同步状态
     *
     * @param folder 文件夹更新信息
     */
    void updateMailFolder(OaMailFolderDO folder);

}
