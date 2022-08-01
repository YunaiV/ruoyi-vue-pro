<script setup lang="ts">
import { ElRow, ElCol, ElSkeleton, ElCard, ElDivider, ElLink } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive } from 'vue'
import { CountTo } from '@/components/CountTo'
import { formatTime } from '@/utils'
import { Echart } from '@/components/Echart'
import { EChartsOption } from 'echarts'
import { radarOption } from './echarts-data'
import { Highlight } from '@/components/Highlight'
import type { WorkplaceTotal, Project, Notice, Shortcut } from './types'
import { set } from 'lodash-es'
import { useCache } from '@/hooks/web/useCache'
import { pieOptions, barOptions, lineOptions } from './echarts-data'

const { t } = useI18n()
const { wsCache } = useCache()
const loading = ref(true)
const avatar = wsCache.get('user').user.avatar
const username = wsCache.get('user').user.nickname
const pieOptionsData = reactive<EChartsOption>(pieOptions) as EChartsOption
// 获取统计数
let totalSate = reactive<WorkplaceTotal>({
  project: 0,
  access: 0,
  todo: 0
})

const getCount = async () => {
  const data = {
    project: 40,
    access: 2340,
    todo: 10
  }
  totalSate = Object.assign(totalSate, data)
}

// 获取项目数
let projects = reactive<Project[]>([])
const getProject = async () => {
  const data = [
    {
      name: 'Github',
      icon: 'akar-icons:github-fill',
      message: 'workplace.introduction',
      personal: 'Archer',
      time: new Date()
    },
    {
      name: 'Vue',
      icon: 'logos:vue',
      message: 'workplace.introduction',
      personal: 'Archer',
      time: new Date()
    },
    {
      name: 'Angular',
      icon: 'logos:angular-icon',
      message: 'workplace.introduction',
      personal: 'Archer',
      time: new Date()
    },
    {
      name: 'React',
      icon: 'logos:react',
      message: 'workplace.introduction',
      personal: 'Archer',
      time: new Date()
    },
    {
      name: 'Webpack',
      icon: 'logos:webpack',
      message: 'workplace.introduction',
      personal: 'Archer',
      time: new Date()
    },
    {
      name: 'Vite',
      icon: 'vscode-icons:file-type-vite',
      message: 'workplace.introduction',
      personal: 'Archer',
      time: new Date()
    }
  ]
  projects = Object.assign(projects, data)
}

// 获取通知公告
let notice = reactive<Notice[]>([])
const getNotice = async () => {
  const data = [
    {
      title: '系统升级版本',
      type: '通知',
      keys: ['通知', '升级'],
      date: new Date()
    },
    {
      title: '系统凌晨维护',
      type: '公告',
      keys: ['公告', '维护'],
      date: new Date()
    },
    {
      title: '系统升级版本',
      type: '通知',
      keys: ['通知', '升级'],
      date: new Date()
    },
    {
      title: '系统凌晨维护',
      type: '公告',
      keys: ['公告', '维护'],
      date: new Date()
    }
  ]
  notice = Object.assign(notice, data)
}

// 获取快捷入口
let shortcut = reactive<Shortcut[]>([])

const getShortcut = async () => {
  const data = [
    {
      name: 'Github',
      icon: 'akar-icons:github-fill',
      url: 'github.io'
    },
    {
      name: 'Vue',
      icon: 'logos:vue',
      url: 'vuejs.org'
    },
    {
      name: 'Vite',
      icon: 'vscode-icons:file-type-vite',
      url: 'https://vitejs.dev/'
    },
    {
      name: 'Angular',
      icon: 'logos:angular-icon',
      url: 'github.io'
    },
    {
      name: 'React',
      icon: 'logos:react',
      url: 'github.io'
    },
    {
      name: 'Webpack',
      icon: 'logos:webpack',
      url: 'github.io'
    }
  ]
  shortcut = Object.assign(shortcut, data)
}

// 获取指数
let radarOptionData = reactive<EChartsOption>(radarOption) as EChartsOption

const getRadar = async () => {
  const data = [
    { name: 'workplace.quote', max: 65, personal: 42, team: 50 },
    { name: 'workplace.contribution', max: 160, personal: 30, team: 140 },
    { name: 'workplace.hot', max: 300, personal: 20, team: 28 },
    { name: 'workplace.yield', max: 130, personal: 35, team: 35 },
    { name: 'workplace.follow', max: 100, personal: 80, team: 90 }
  ]
  set(
    radarOptionData,
    'radar.indicator',
    data.map((v) => {
      return {
        name: t(v.name),
        max: v.max
      }
    })
  )
  set(radarOptionData, 'series', [
    {
      name: '指数',
      type: 'radar',
      data: [
        {
          value: data.map((v) => v.personal),
          name: t('workplace.personal')
        },
        {
          value: data.map((v) => v.team),
          name: t('workplace.team')
        }
      ]
    }
  ])
}
// 用户来源
const getUserAccessSource = async () => {
  const data = [
    { value: 335, name: 'analysis.directAccess' },
    { value: 310, name: 'analysis.mailMarketing' },
    { value: 234, name: 'analysis.allianceAdvertising' },
    { value: 135, name: 'analysis.videoAdvertising' },
    { value: 1548, name: 'analysis.searchEngines' }
  ]
  set(
    pieOptionsData,
    'legend.data',
    data.map((v) => t(v.name))
  )
  pieOptionsData!.series![0].data = data.map((v) => {
    return {
      name: t(v.name),
      value: v.value
    }
  })
}
const barOptionsData = reactive<EChartsOption>(barOptions) as EChartsOption

// 周活跃量
const getWeeklyUserActivity = async () => {
  const data = [
    { value: 13253, name: 'analysis.monday' },
    { value: 34235, name: 'analysis.tuesday' },
    { value: 26321, name: 'analysis.wednesday' },
    { value: 12340, name: 'analysis.thursday' },
    { value: 24643, name: 'analysis.friday' },
    { value: 1322, name: 'analysis.saturday' },
    { value: 1324, name: 'analysis.sunday' }
  ]
  set(
    barOptionsData,
    'xAxis.data',
    data.map((v) => t(v.name))
  )
  set(barOptionsData, 'series', [
    {
      name: t('analysis.activeQuantity'),
      data: data.map((v) => v.value),
      type: 'bar'
    }
  ])
}

const lineOptionsData = reactive<EChartsOption>(lineOptions) as EChartsOption

// 每月销售总额
const getMonthlySales = async () => {
  const data = [
    { estimate: 100, actual: 120, name: 'analysis.january' },
    { estimate: 120, actual: 82, name: 'analysis.february' },
    { estimate: 161, actual: 91, name: 'analysis.march' },
    { estimate: 134, actual: 154, name: 'analysis.april' },
    { estimate: 105, actual: 162, name: 'analysis.may' },
    { estimate: 160, actual: 140, name: 'analysis.june' },
    { estimate: 165, actual: 145, name: 'analysis.july' },
    { estimate: 114, actual: 250, name: 'analysis.august' },
    { estimate: 163, actual: 134, name: 'analysis.september' },
    { estimate: 185, actual: 56, name: 'analysis.october' },
    { estimate: 118, actual: 99, name: 'analysis.november' },
    { estimate: 123, actual: 123, name: 'analysis.december' }
  ]
  set(
    lineOptionsData,
    'xAxis.data',
    data.map((v) => t(v.name))
  )
  set(lineOptionsData, 'series', [
    {
      name: t('analysis.estimate'),
      smooth: true,
      type: 'line',
      data: data.map((v) => v.estimate),
      animationDuration: 2800,
      animationEasing: 'cubicInOut'
    },
    {
      name: t('analysis.actual'),
      smooth: true,
      type: 'line',
      itemStyle: {},
      data: data.map((v) => v.actual),
      animationDuration: 2800,
      animationEasing: 'quadraticOut'
    }
  ])
}

const getAllApi = async () => {
  await Promise.all([
    getCount(),
    getProject(),
    getNotice(),
    getShortcut(),
    getRadar(),
    getUserAccessSource(),
    getWeeklyUserActivity(),
    getMonthlySales()
  ])
  loading.value = false
}

getAllApi()
</script>

<template>
  <div>
    <el-card shadow="never">
      <el-skeleton :loading="loading" animated>
        <el-row :gutter="20" justify="space-between">
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="flex items-center">
              <img :src="avatar" alt="" class="w-70px h-70px rounded-[50%] mr-20px" />
              <div>
                <div class="text-20px text-700">
                  {{ t('workplace.welcome') }} {{ username }} {{ t('workplace.happyDay') }}
                </div>
                <div class="mt-10px text-14px text-gray-500">
                  {{ t('workplace.toady') }}，20℃ - 32℃！
                </div>
              </div>
            </div>
          </el-col>
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="flex h-70px items-center justify-end <sm:mt-10px">
              <div class="px-8px text-right">
                <div class="text-14px text-gray-400 mb-20px">{{ t('workplace.project') }}</div>
                <CountTo
                  class="text-20px"
                  :start-val="0"
                  :end-val="totalSate.project"
                  :duration="2600"
                />
              </div>
              <el-divider direction="vertical" />
              <div class="px-8px text-right">
                <div class="text-14px text-gray-400 mb-20px">{{ t('workplace.toDo') }}</div>
                <CountTo
                  class="text-20px"
                  :start-val="0"
                  :end-val="totalSate.todo"
                  :duration="2600"
                />
              </div>
              <el-divider direction="vertical" border-style="dashed" />
              <div class="px-8px text-right">
                <div class="text-14px text-gray-400 mb-20px">{{ t('workplace.access') }}</div>
                <CountTo
                  class="text-20px"
                  :start-val="0"
                  :end-val="totalSate.access"
                  :duration="2600"
                />
              </div>
            </div>
          </el-col>
        </el-row>
      </el-skeleton>
    </el-card>
  </div>

  <el-row class="mt-10px" :gutter="20" justify="space-between">
    <el-col :xl="16" :lg="16" :md="24" :sm="24" :xs="24" class="mb-20px">
      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between">
            <span>{{ t('workplace.project') }}</span>
            <el-link type="primary" :underline="false">{{ t('workplace.more') }}</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row>
            <el-col
              v-for="(item, index) in projects"
              :key="`card-${index}`"
              :xl="8"
              :lg="8"
              :md="12"
              :sm="24"
              :xs="24"
            >
              <el-card shadow="hover">
                <div class="flex items-center">
                  <Icon :icon="item.icon" :size="25" class="mr-10px" />
                  <span class="text-16px">{{ item.name }}</span>
                </div>
                <div class="mt-15px text-14px text-gray-400">{{ t(item.message) }}</div>
                <div class="mt-20px text-12px text-gray-400 flex justify-between">
                  <span>{{ item.personal }}</span>
                  <span>{{ formatTime(item.time, 'yyyy-MM-dd') }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>

      <el-card shadow="never" class="mt-10px">
        <el-skeleton :loading="loading" animated>
          <el-row :gutter="20" justify="space-between">
            <el-col :xl="10" :lg="10" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover" class="mb-20px">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="pieOptionsData" :height="300" />
                </el-skeleton>
              </el-card>
            </el-col>
            <el-col :xl="14" :lg="14" :md="24" :sm="24" :xs="24">
              <el-card shadow="hover" class="mb-20px">
                <el-skeleton :loading="loading" animated>
                  <Echart :options="barOptionsData" :height="300" />
                </el-skeleton>
              </el-card>
            </el-col>
            <el-col :span="24">
              <el-card shadow="hover" class="mb-20px">
                <el-skeleton :loading="loading" animated :rows="4">
                  <Echart :options="lineOptionsData" :height="350" />
                </el-skeleton>
              </el-card>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
    </el-col>
    <el-col :xl="8" :lg="8" :md="24" :sm="24" :xs="24" class="mb-20px">
      <el-card shadow="never">
        <template #header>
          <span>{{ t('workplace.shortcutOperation') }}</span>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row>
            <el-col v-for="item in shortcut" :key="`team-${item.name}`" :span="8" class="mb-20px">
              <div class="flex items-center">
                <Icon :icon="item.icon" class="mr-10px" />
                <el-link type="default" :underline="false" :href="item.url">
                  {{ item.name }}
                </el-link>
              </div>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
      <el-card shadow="never" class="mt-10px">
        <template #header>
          <div class="flex justify-between">
            <span>{{ t('workplace.notice') }}</span>
            <el-link type="primary" :underline="false">{{ t('workplace.more') }}</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <div v-for="(item, index) in notice" :key="`dynamics-${index}`">
            <div class="flex items-center">
              <img :src="avatar" alt="" class="w-35px h-35px rounded-[50%] mr-20px" />
              <div>
                <div class="text-14px">
                  <Highlight :keys="item.keys.map((v) => t(v))">
                    {{ item.type }} : {{ item.title }}
                  </Highlight>
                </div>
                <div class="mt-15px text-12px text-gray-400">
                  {{ formatTime(item.date, 'yyyy-MM-dd') }}
                </div>
              </div>
            </div>
            <el-divider />
          </div>
        </el-skeleton>
      </el-card>
      <el-card shadow="never" class="mt-10px">
        <template #header>
          <span>{{ t('workplace.index') }}</span>
        </template>
        <el-skeleton :loading="loading" animated>
          <Echart :options="radarOptionData" :height="400" />
        </el-skeleton>
      </el-card>
    </el-col>
  </el-row>
</template>
