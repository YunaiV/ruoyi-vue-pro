package cn.iocoder.yudao.module.system.service.auth;

import java.util.Collection;

/**
 * OAuth2 批准 Service 接口
 *
 * 从功能上，和 Spring Security OAuth 的 ApprovalStoreUserApprovalHandler 的功能，记录用户针对指定客户端的授权，减少手动确定。
 *
 * @author 芋道源码
 */
public interface OAuth2ApproveService {

    /**
     * 获得指定用户，针对指定客户端的指定授权，是否通过
     *
     * 参考 ApprovalStoreUserApprovalHandler 的 checkForPreApproval 方法
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @param clientId 客户端编号
     * @param scopes 授权范围
     * @return 是否授权通过
     */
    boolean checkForPreApproval(Long userId, Integer userType, String clientId, Collection<String> scopes);

}
