<script lang="ts" setup>
import type { InfraRedisApi } from '#/api/infra/redis';

import { Descriptions } from 'ant-design-vue';

defineProps<{
  redisData?: InfraRedisApi.RedisMonitorInfo;
}>();
</script>

<template>
  <Descriptions
    :column="6"
    bordered
    size="middle"
    :label-style="{ width: '138px' }"
  >
    <Descriptions.Item label="Redis 版本">
      {{ redisData?.info?.redis_version }}
    </Descriptions.Item>
    <Descriptions.Item label="运行模式">
      {{ redisData?.info?.redis_mode === 'standalone' ? '单机' : '集群' }}
    </Descriptions.Item>
    <Descriptions.Item label="端口">
      {{ redisData?.info?.tcp_port }}
    </Descriptions.Item>
    <Descriptions.Item label="客户端数">
      {{ redisData?.info?.connected_clients }}
    </Descriptions.Item>
    <Descriptions.Item label="运行时间(天)">
      {{ redisData?.info?.uptime_in_days }}
    </Descriptions.Item>
    <Descriptions.Item label="使用内存">
      {{ redisData?.info?.used_memory_human }}
    </Descriptions.Item>
    <Descriptions.Item label="使用 CPU">
      {{
        redisData?.info
          ? parseFloat(redisData?.info?.used_cpu_user_children).toFixed(2)
          : ''
      }}
    </Descriptions.Item>
    <Descriptions.Item label="内存配置">
      {{ redisData?.info?.maxmemory_human }}
    </Descriptions.Item>
    <Descriptions.Item label="AOF 是否开启">
      {{ redisData?.info?.aof_enabled === '0' ? '否' : '是' }}
    </Descriptions.Item>
    <Descriptions.Item label="RDB 是否成功">
      {{ redisData?.info?.rdb_last_bgsave_status }}
    </Descriptions.Item>
    <Descriptions.Item label="Key 数量">
      {{ redisData?.dbSize }}
    </Descriptions.Item>
    <Descriptions.Item label="网络入口/出口">
      {{ redisData?.info?.instantaneous_input_kbps }}kps /
      {{ redisData?.info?.instantaneous_output_kbps }}kps
    </Descriptions.Item>
  </Descriptions>
</template>
