<script lang="ts" setup>
import { onBeforeMount, ref } from 'vue'
import * as RedisApi from '@/api/infra/redis'
import DictTag from '@/components/DictTag/src/DictTag.vue'
import { DICT_TYPE } from '@/utils/dict'
import * as echarts from 'echarts'
import { RedisKeyInfo, RedisMonitorInfoVO } from '@/api/infra/redis/types'
import {
  ElRow,
  ElCard,
  ElCol,
  ElTable,
  ElTableColumn,
  ElScrollbar,
  ElDescriptions,
  ElDescriptionsItem
} from 'element-plus'
const cache = ref<RedisMonitorInfoVO>()
const keyListLoad = ref(true)
const keyList = ref<RedisKeyInfo[]>([])
// 基本信息
const readRedisInfo = async () => {
  const data = await RedisApi.redisMonitorInfo()
  cache.value = data
  loadEchartOptions(cache.value.commandStats)
  const redisKeysInfo = await RedisApi.redisKeysInfo()
  keyList.value = redisKeysInfo
  keyListLoad.value = false //加载完成
}
// 图表
const commandStatsRef = ref<HTMLElement>()

const usedmemory = ref<HTMLDivElement>()

const loadEchartOptions = (stats) => {
  const commandStats = [] as any[]
  const nameList = [] as string[]
  stats.forEach((row) => {
    commandStats.push({
      name: row.command,
      value: row.calls
    })
    nameList.push(row.command)
  })

  const commandStatsInstance = echarts.init(commandStatsRef.value!, 'macarons')

  commandStatsInstance.setOption({
    title: {
      text: '命令统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b} : {c} ({d}%)'
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 30,
      top: 10,
      bottom: 20,
      data: nameList,
      textStyle: {
        color: '#a1a1a1'
      }
    },
    series: [
      {
        name: '命令',
        type: 'pie',
        radius: [20, 120],
        center: ['40%', '60%'],
        data: commandStats,
        roseType: 'radius',
        label: {
          show: true
        },
        emphasis: {
          label: {
            show: true
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  })

  const usedMemoryInstance = echarts.init(usedmemory.value!, 'macarons')
  usedMemoryInstance.setOption({
    title: {
      text: '内存使用情况',
      left: 'center'
    },
    tooltip: {
      formatter: '{b} <br/>{a} : ' + cache.value!.info.used_memory_human
    },
    series: [
      {
        name: '峰值',
        type: 'gauge',
        min: 0,
        max: 100,
        progress: {
          show: true
        },
        detail: {
          formatter: cache.value!.info.used_memory_human
        },
        data: [
          {
            value: parseFloat(cache.value!.info.used_memory_human),
            name: '内存消耗'
          }
        ]
      }
    ]
  })
}

onBeforeMount(() => {
  readRedisInfo()
})
</script>
<template>
  <el-scrollbar height="calc(100vh - 88px - 40px - 50px)">
    <el-row>
      <el-col :span="24" class="card-box" shadow="hover">
        <el-card>
          <el-descriptions title="基本信息" :column="6" border>
            <el-descriptions-item label="Redis版本 :">
              {{ cache?.info?.redis_version }}
            </el-descriptions-item>
            <el-descriptions-item label="运行模式 :">
              {{ cache?.info?.redis_mode == 'standalone' ? '单机' : '集群' }}
            </el-descriptions-item>
            <el-descriptions-item label="端口 :">
              {{ cache?.info?.tcp_port }}
            </el-descriptions-item>
            <el-descriptions-item label="客户端数 :">
              {{ cache?.info?.connected_clients }}
            </el-descriptions-item>
            <el-descriptions-item label="运行时间(天) :">
              {{ cache?.info?.uptime_in_days }}
            </el-descriptions-item>
            <el-descriptions-item label="使用内存 :">
              {{ cache?.info?.used_memory_human }}
            </el-descriptions-item>
            <el-descriptions-item label="使用CPU :">
              {{ cache?.info ? parseFloat(cache?.info?.used_cpu_user_children).toFixed(2) : '' }}
            </el-descriptions-item>
            <el-descriptions-item label="内存配置 :">
              {{ cache?.info?.maxmemory_human }}
            </el-descriptions-item>
            <el-descriptions-item label="AOF是否开启 :">
              {{ cache?.info?.aof_enabled == '0' ? '否' : '是' }}
            </el-descriptions-item>
            <el-descriptions-item label="RDB是否成功 :">
              {{ cache?.info?.rdb_last_bgsave_status }}
            </el-descriptions-item>
            <el-descriptions-item label="Key数量 :">
              {{ cache?.dbSize }}
            </el-descriptions-item>
            <el-descriptions-item label="网络入口/出口 :">
              {{ cache?.info?.instantaneous_input_kbps }}kps/
              {{ cache?.info?.instantaneous_output_kbps }}kps
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12" style="margin-top: 10px">
        <el-card :gutter="12" shadow="hover">
          <div ref="commandStatsRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12" style="margin-top: 10px">
        <el-card style="margin-left: 10px" :gutter="12" shadow="hover">
          <div ref="usedmemory" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row style="margin-top: 10px">
      <el-col :span="24" class="card-box" shadow="hover">
        <el-card>
          <el-table v-loading="keyListLoad" :data="keyList" row-key="id">
            <el-table-column prop="keyTemplate" label="Key 模板" width="200" />
            <el-table-column prop="keyType" label="Key 类型" width="100" />
            <el-table-column prop="valueType" label="Value 类型" />
            <el-table-column prop="timeoutType" label="超时时间" width="200">
              <template #default="{ row }">
                <DictTag :type="DICT_TYPE.INFRA_REDIS_TIMEOUT_TYPE" :value="row?.timeoutType" />
                <span v-if="row?.timeout > 0">({{ row?.timeout / 1000 }} 秒)</span>
              </template>
            </el-table-column>
            <el-table-column prop="memo" label="备注" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </el-scrollbar>
</template>
