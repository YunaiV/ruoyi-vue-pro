package cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ThingModelRespVO {

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
        private List<ThingModelProperty> properties;

        /**
         * 服务列表
         */
        private List<ThingModelService> services;

        /**
         * 事件列表
         */
        private List<ThingModelEvent> events;
    }
}
