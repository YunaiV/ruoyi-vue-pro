<script lang="ts" setup>
import type { InfraRedisApi } from '#/api/infra/redis';

import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

defineProps<{
  redisData?: InfraRedisApi.RedisMonitorInfo;
}>();
</script>

<template>
  <ElDescriptions :column="6" border size="default" :label-width="138">
    <ElDescriptionsItem label="Redis 版本">
      {{ redisData?.info?.redis_version }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="运行模式">
      {{ redisData?.info?.redis_mode === 'standalone' ? '单机' : '集群' }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="端口">
      {{ redisData?.info?.tcp_port }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="客户端数">
      {{ redisData?.info?.connected_clients }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="运行时间(天)">
      {{ redisData?.info?.uptime_in_days }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="使用内存">
      {{ redisData?.info?.used_memory_human }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="使用 CPU">
      {{
        redisData?.info
          ? parseFloat(redisData?.info?.used_cpu_user_children).toFixed(2)
          : ''
      }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="内存配置">
      {{ redisData?.info?.maxmemory_human }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="AOF 是否开启">
      {{ redisData?.info?.aof_enabled === '0' ? '否' : '是' }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="RDB 是否成功">
      {{ redisData?.info?.rdb_last_bgsave_status }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="Key 数量">
      {{ redisData?.dbSize }}
    </ElDescriptionsItem>
    <ElDescriptionsItem label="网络入口/出口">
      {{ redisData?.info?.instantaneous_input_kbps }}kps /
      {{ redisData?.info?.instantaneous_output_kbps }}kps
    </ElDescriptionsItem>
  </ElDescriptions>
</template>
