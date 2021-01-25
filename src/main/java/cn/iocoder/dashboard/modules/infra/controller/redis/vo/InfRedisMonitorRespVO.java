package cn.iocoder.dashboard.modules.infra.controller.redis.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Properties;

@Data
@Builder
@AllArgsConstructor
public class InfRedisMonitorRespVO {

    private Properties info;

    private Long dbSize;

    private List<Object> commandStats;

    @Data
    @Builder
    @AllArgsConstructor
    public static class CommandStat {

        private String command;

        private Integer calls;

        private Integer usec;

    }

}
