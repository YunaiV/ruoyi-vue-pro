package cn.iocoder.yudao.module.im.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * IM 模块全局配置
 * <p>
 * 各子模块用嵌套 inner class 区分（friend / 后续 group / sensitive 等），
 * yaml 路径保持 yudao.im.{module}.{key} 与原有部署保持兼容
 *
 * @author 芋道源码
 */
@Component
@ConfigurationProperties(prefix = "yudao.im")
@Data
public class ImProperties {

    private Friend friend = new Friend();

    /**
     * 好友模块配置
     */
    @Data
    public static class Friend {

        /**
         * 是否自动通过所有好友申请（全局开关）
         * <p>
         * 默认 false，普通用户必须走申请-审批流程；开启后所有用户的好友申请会立即同意，主要用于全员开放型 IM 部署。
         * 如需细化到「仅特定用户自动通过」（如机器人 / AI 账号），请在 system 用户表加字段，并在 applyFriend 内按用户级开关短路
         */
        private boolean autoAccept = false;

    }

}
