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
      <el-col :span="12" class="mt-3">
        <el-card :gutter="12" shadow="hover">
          <div ref="commandStatsRef" class="h-88"></div>
        </el-card>
      </el-col>
      <el-col :span="12" class="mt-3">
        <el-card class="ml-3" :gutter="12" shadow="hover">
          <div ref="usedmemory" class="h-88"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row class="mt-3">
      <el-col :span="24" class="card-box" shadow="hover">
        <el-card>
          <el-table
            v-loading="keyListLoad"
            :data="keyList"
            row-key="id"
            @row-click="openKeyTemplate"
          >
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
  <XModal v-model="dialogVisible" :title="keyTemplate + ' 模板'">
    <el-row>
      <el-col :span="14" class="mt-3">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>键名列表</span>
            </div>
          </template>
          <el-table :data="cacheKeys" style="width: 100%" @row-click="handleKeyValue">
            <el-table-column label="缓存键名" align="center" :show-overflow-tooltip="true">
              <template #default="{ row }">
                {{ row }}
              </template>
            </el-table-column>
            <el-table-column label="操作" align="right" width="60">
              <template #default="{ row }">
                <XTextButton preIcon="ep:delete" @click="handleDeleteKey(row)" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10" class="mt-3">
        <el-card shadow="always">
          <template #header>
            <div class="card-header">
              <span>缓存内容</span>
              <XTextButton
                preIcon="ep:refresh"
                title="清理全部"
                @click="handleDeleteKeys(keyTemplate)"
                class="float-right p-1"
              />
            </div>
          </template>
          <el-descriptions :column="1">
            <el-descriptions-item label="缓存键名:">{{ cacheForm.key }}</el-descriptions-item>
            <el-descriptions-item label="缓存内容:">{{ cacheForm.value }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </XModal>
</template>
<script setup lang="ts" name="Redis">
import { onBeforeMount, ref } from 'vue'
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
import * as echarts from 'echarts'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import * as RedisApi from '@/api/infra/redis'
import { RedisKeyInfo, RedisMonitorInfoVO } from '@/api/infra/redis/types'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const cache = ref<RedisMonitorInfoVO>()
const keyListLoad = ref(true)
const keyList = ref<RedisKeyInfo[]>([])
// 基本信息
const readRedisInfo = async () => {
  const data = await RedisApi.getCacheApi()
  cache.value = data
  loadEchartOptions(data.commandStats)
  const redisKeysInfo = await RedisApi.getKeyDefineListApi()
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
const dialogVisible = ref(false)
const keyTemplate = ref('')
const cacheKeys = ref()
const cacheForm = ref<{
  key: string
  value: string
}>({
  key: '',
  value: ''
})
const openKeyTemplate = async (row: RedisKeyInfo) => {
  keyTemplate.value = row.keyTemplate
  cacheKeys.value = await RedisApi.getKeyListApi(row.keyTemplate)
  dialogVisible.value = true
}
const handleDeleteKey = async (row) => {
  RedisApi.deleteKeyApi(row)
  message.success(t('common.delSuccess'))
}
const handleDeleteKeys = async (row) => {
  RedisApi.deleteKeysApi(row)
  message.success(t('common.delSuccess'))
}
const handleKeyValue = async (row) => {
  const res = await RedisApi.getKeyValueApi(row)
  cacheForm.value = res
}
onBeforeMount(() => {
  readRedisInfo()
})
</script>
<style scoped>
.redis {
  height: 600px;
  max-height: 860px;
}
</style>
