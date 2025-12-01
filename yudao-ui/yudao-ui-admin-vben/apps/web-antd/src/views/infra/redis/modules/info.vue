<script lang="ts" setup>
import type { InfraRedisApi } from '#/api/infra/redis';

import { useDescription } from '#/components/description';

defineProps<{
  redisData?: InfraRedisApi.RedisMonitorInfo;
}>();

const [Descriptions] = useDescription({
  bordered: false,
  column: 6,
  class: 'mx-4',
  schema: [
    {
      field: 'info.redis_version',
      label: 'Redis 版本',
    },
    {
      field: 'info.redis_mode',
      label: '运行模式',
      render: (val) => (val === 'standalone' ? '单机' : '集群'),
    },
    {
      field: 'info.tcp_port',
      label: '端口',
    },
    {
      field: 'info.connected_clients',
      label: '客户端数',
    },
    {
      field: 'info.uptime_in_days',
      label: '运行时间(天)',
    },
    {
      field: 'info.used_memory_human',
      label: '使用内存',
    },
    {
      field: 'info.used_cpu_user_children',
      label: '使用 CPU',
      render: (val) => Number.parseFloat(val).toFixed(2),
    },
    {
      field: 'info.maxmemory_human',
      label: '内存配置',
    },
    {
      field: 'info.aof_enabled',
      label: 'AOF 是否开启',
      render: (val) => (val === '0' ? '否' : '是'),
    },
    {
      field: 'info.rdb_last_bgsave_status',
      label: 'RDB 是否成功',
    },
    {
      field: 'dbSize',
      label: 'Key 数量',
    },
    {
      field: 'info.instantaneous_input_kbps',
      label: '网络入口/出口',
      render: (val, data) =>
        `${val}kps / ${data?.info?.instantaneous_output_kbps}kps`,
    },
  ],
});
</script>

<template>
  <Descriptions :data="redisData" />
</template>
