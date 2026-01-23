package cn.iocoder.yudao.module.iot.core.topic.topo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

// TODO @AI：得一起讨论下，到底使用什么后缀合适：1）一方面要体现出请求、响应；2）一方面体现出上下行（设备 to server，还是 server to 设备），可以一起讨论？
// TODO @AI：文档地址：https://help.aliyun.com/zh/iot/user-guide/manage-topological-relationships?spm=a2c4g.11186623.help-menu-30520.d_2_2_7_3_2.2e983f47Z2iGbo&scm=20140722.H_89299._.OR_help-T_cn~zh-V_1#section-w33-vyg-12b
/**
 * IoT 设备拓扑添加 Request DTO
 * <p>
 * 用于 thing.topo.add 消息的 params 参数
 *
 * @author 芋道源码
 */
@Data
public class IotDeviceTopoAddReqDTO {

    // TODO @AI：是个数组；
    // TODO @AI：有响应结果的；

    /**
     * 子设备客户端 ID
     */
    @NotEmpty(message = "客户端 ID 不能为空")
    private String clientId;

    /**
     * 子设备用户名
     */
    @NotEmpty(message = "用户名不能为空")
    private String username;

    /**
     * 子设备认证密码
     */
    @NotEmpty(message = "密码不能为空")
    private String password;

}
