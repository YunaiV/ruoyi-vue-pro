package cn.iocoder.yudao.module.oa.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.message.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.*;

import java.util.List;
import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.mail.OaMailComposeModeEnum;

/**
 * 企业邮箱邮件 Service 接口
 *
 * @author 芋道源码
 */
public interface OaMailMessageService {

    /**
     * 获得回复、转发或草稿预填数据
     *
     * @param id 邮件编号
     * @param mode 写信方式
     * @param userId 当前用户编号
     * @return 写信预填数据
     */
    OaMailMessageSaveReqVO getMailMessageCompose(Long id, @InEnum(value = OaMailComposeModeEnum.class, message = "写信方式必须是 {value}") String mode, Long userId);

    /**
     * 手动同步本人账号的邮件文件夹和索引
     *
     * @param accountId 邮箱账号编号
     * @param userId 当前用户编号
     * @return 同步的索引数量
     */
    int syncMailMessageList(Long accountId, Long userId);

    /**
     * 获得当前用户邮箱账号的文件夹列表
     *
     * @param accountId 邮箱账号编号
     * @param userId 当前用户编号
     * @return 文件夹列表，包含收件箱对应的未读邮件虚拟入口
     */
    List<OaMailFolderRespVO> getMailFolderList(Long accountId, Long userId);

    /**
     * 从数据库分页查询邮件索引
     *
     * @param reqVO 分页查询条件
     * @param userId 当前用户编号
     * @return 邮件分页
     */
    PageResult<OaMailMessageDO> getMailMessagePage(OaMailMessagePageReqVO reqVO, Long userId);

    /**
     * 获得邮件详情，首次读取远端正文后缓存到本地
     *
     * @param id 邮件编号
     * @param userId 当前用户编号
     * @return 邮件详情
     */
    OaMailMessageDO getMailMessage(Long id, Long userId);

    /**
     * 下载本人邮箱中的附件
     *
     * @param id 邮件编号
     * @param part 附件 MIME 路径
     * @param userId 当前用户编号
     * @return 附件名称和内容
     */
    Pair<String, byte[]> getMailMessageAttachment(Long id, String part, Long userId);

    /**
     * 更新远端及本地已读状态
     *
     * @param id 邮件编号
     * @param readStatus 是否已读
     * @param userId 当前用户编号
     */
    void updateMailMessageRead(Long id, Boolean readStatus, Long userId);

    /**
     * 移入已删除文件夹，已删除中的邮件执行单封彻底删除
     *
     * @param id 邮件编号
     * @param userId 当前用户编号
     */
    void deleteMailMessage(Long id, Long userId);

    /**
     * 将已删除邮件恢复到收件箱
     *
     * @param id 邮件编号
     * @param userId 当前用户编号
     */
    void restoreMailMessage(Long id, Long userId);

    /**
     * 在远端保存草稿，返回新的本地索引编号
     *
     * @param reqVO 草稿内容
     * @param userId 当前用户编号
     * @return 邮件编号
     */
    Long saveMailMessageDraft(OaMailMessageSaveReqVO reqVO, Long userId);

    /**
     * 发送邮件，返回投递及副本处理结果，不自动重试
     *
     * @param reqVO 邮件内容
     * @param userId 当前用户编号
     * @return 投递及副本处理结果
     */
    String sendMailMessage(OaMailMessageSaveReqVO reqVO, Long userId);

}
