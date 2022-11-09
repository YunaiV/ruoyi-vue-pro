package cn.iocoder.yudao.module.infra.controller.admin.redis.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Properties;

@ApiModel("管理后台 - Redis 监控信息 Response VO")
@Data
@Builder
@AllArgsConstructor
public class RedisMonitorRespVO {

    @ApiModelProperty(value = "Redis info 指令结果", required = true, notes = "具体字段，查看 Redis 文档")
    private Properties info;

    @ApiModelProperty(value = "Redis key 数量", required = true, example = "1024")
    private Long dbSize;

    @ApiModelProperty(value = "CommandStat 数组", required = true)
    private List<CommandStat> commandStats;

    @ApiModel("Redis 命令统计结果")
    @Data
    @Builder
    @AllArgsConstructor
    public static class CommandStat {

        @ApiModelProperty(value = "Redis 命令", required = true, example = "get")
        private String command;

        @ApiModelProperty(value = "调用次数", required = true, example = "1024")
        private Long calls;

        @ApiModelProperty(value = "消耗 CPU 秒数", required = true, example = "666")
        private Long usec;

    }

}
