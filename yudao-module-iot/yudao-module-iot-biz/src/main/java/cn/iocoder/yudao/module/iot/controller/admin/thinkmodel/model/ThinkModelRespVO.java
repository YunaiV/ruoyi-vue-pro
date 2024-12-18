package cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ThinkModelRespVO {

    /**
     * 产品编号
     */
    private Long id;

    /**
     * 产品标识
     */
    private String productKey;

    /**
     * 物模型
     */
    private Model model;

    /**
     * 物模型
     */
    @Data
    public static class Model {

        /**
         * 属性列表
         */
        private List<ThinkModelProperty> properties;

        /**
         * 服务列表
         */
        private List<ThinkModelService> services;

        /**
         * 事件列表
         */
        private List<ThinkModelEvent> events;
    }
}
