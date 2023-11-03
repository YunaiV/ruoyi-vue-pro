package cn.iocoder.yudao.module.system.service.oauth2;

import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2ApproveDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * @param requestedScopes 授权范围
     * @return 是否授权通过
     */
    boolean checkForPreApproval(Long userId, Integer userType, String clientId, Collection<String> requestedScopes);

    /**
     * 在用户发起批准时，基于 scopes 的选项，计算最终是否通过
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @param clientId 客户端编号
     * @param requestedScopes 授权范围
     * @return 是否授权通过
     */
    boolean updateAfterApproval(Long userId, Integer userType, String clientId, Map<String, Boolean> requestedScopes);

    /**
     * 获得用户的批准列表，排除已过期的
     *
     * @param userId 用户编号
     * @param userType 用户类型
     * @param clientId 客户端编号
     * @return 是否授权通过
     */
    List<OAuth2ApproveDO> getApproveList(Long userId, Integer userType, String clientId);

}
