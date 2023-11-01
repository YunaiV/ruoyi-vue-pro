package cn.iocoder.yudao.module.crm.framework.core.service;

/**
 * 校验数据是否存在 service 接口
 * TODO 需要使用团队成员相关操作的业务接口都需要继承此接口
 *
 * @author HUIHUI
 */
public interface CrmPermissionValidateService {

    /**
     * 校验数据是否存在
     *
     * @param bizType CRM 类型
     * @param bizId   数据编号
     * @return 是/否
     */
    boolean validateBizIdExists(Integer bizType, Long bizId);

}
