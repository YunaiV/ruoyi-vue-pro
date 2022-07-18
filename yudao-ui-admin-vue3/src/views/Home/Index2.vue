<script setup lang="ts">
import { useTimeAgo } from '@/hooks/web/useTimeAgo'
import { ElRow, ElCol, ElSkeleton, ElCard, ElDivider, ElLink } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive } from 'vue'
import { CountTo } from '@/components/CountTo'
import { formatTime } from '@/utils'
import { Echart } from '@/components/Echart'
import { EChartsOption } from 'echarts'
import { radarOption } from './echarts-data'
import { Highlight } from '@/components/Highlight'
import type { WorkplaceTotal, Project, Dynamic, Team } from './types'
import { set } from 'lodash-es'
import { useCache } from '@/hooks/web/useCache'

const { t } = useI18n()
const { wsCache } = useCache()
const loading = ref(true)
const avatar = wsCache.get('user').user.avatar
const username = wsCache.get('user').user.nickname
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

let projects = reactive<Project[]>([])

// 获取项目数
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

// 获取动态
let dynamics = reactive<Dynamic[]>([])

const getDynamic = async () => {
  const data = [
    {
      keys: ['workplace.push', 'Github'],
      time: new Date()
    },
    {
      keys: ['workplace.push', 'Github'],
      time: new Date()
    },
    {
      keys: ['workplace.push', 'Github'],
      time: new Date()
    },
    {
      keys: ['workplace.push', 'Github'],
      time: new Date()
    },
    {
      keys: ['workplace.push', 'Github'],
      time: new Date()
    },
    {
      keys: ['workplace.push', 'Github'],
      time: new Date()
    }
  ]
  dynamics = Object.assign(dynamics, data)
}

// 获取团队
let team = reactive<Team[]>([])

const getTeam = async () => {
  const data = [
    {
      name: 'Github',
      icon: 'akar-icons:github-fill'
    },
    {
      name: 'Vue',
      icon: 'logos:vue'
    },
    {
      name: 'Angular',
      icon: 'logos:angular-icon'
    },
    {
      name: 'React',
      icon: 'logos:react'
    },
    {
      name: 'Webpack',
      icon: 'logos:webpack'
    },
    {
      name: 'Vite',
      icon: 'vscode-icons:file-type-vite'
    }
  ]
  team = Object.assign(team, data)
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

const getAllApi = async () => {
  await Promise.all([getCount(), getProject(), getDynamic(), getTeam(), getRadar()])
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
                  {{ t('workplace.goodMorning') }} {{ username }} {{ t('workplace.happyDay') }}
                </div>
                <div class="mt-10px text-14px text-gray-500">
                  {{ t('workplace.toady') }}，20℃ - 32℃！
                </div>
              </div>
            </div>
          </el-col>
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="flex h-70px items-center justify-end <sm:mt-20px">
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

  <el-row class="mt-20px" :gutter="20" justify="space-between">
    <el-col :xl="16" :lg="16" :md="24" :sm="24" :xs="24" class="mb-20px">
      <el-card shadow="never">
        <template #header>
          <div class="flex justify-between">
            <span>{{ t('workplace.project') }}</span>
            <ElLink type="primary" :underline="false">{{ t('workplace.more') }}</ElLink>
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

      <el-card shadow="never" class="mt-20px">
        <template #header>
          <div class="flex justify-between">
            <span>{{ t('workplace.dynamic') }}</span>
            <ElLink type="primary" :underline="false">{{ t('workplace.more') }}</ElLink>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <div v-for="(item, index) in dynamics" :key="`dynamics-${index}`">
            <div class="flex items-center">
              <img :src="avatar" alt="" class="w-35px h-35px rounded-[50%] mr-20px" />
              <div>
                <div class="text-14px">
                  <Highlight :keys="item.keys.map((v) => t(v))">
                    {{ username }} {{ t('workplace.pushCode') }}
                  </Highlight>
                </div>
                <div class="mt-15px text-12px text-gray-400">
                  {{ useTimeAgo(item.time) }}
                </div>
              </div>
            </div>
            <el-divider />
          </div>
        </el-skeleton>
      </el-card>
    </el-col>
    <el-col :xl="8" :lg="8" :md="24" :sm="24" :xs="24" class="mb-20px">
      <el-card shadow="never">
        <template #header>
          <span>{{ t('workplace.shortcutOperation') }}</span>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-col
            v-for="item in 9"
            :key="`card-${item}`"
            :xl="12"
            :lg="12"
            :md="12"
            :sm="24"
            :xs="24"
            class="mb-10px"
          >
            <ElLink type="default" :underline="false">
              {{ t('workplace.operation') }}{{ item }}
            </ElLink>
          </el-col>
        </el-skeleton>
      </el-card>

      <el-card shadow="never" class="mt-20px">
        <template #header>
          <span>{{ t('workplace.index') }}</span>
        </template>
        <el-skeleton :loading="loading" animated>
          <Echart :options="radarOptionData" :height="400" />
        </el-skeleton>
      </el-card>

      <el-card shadow="never" class="mt-20px">
        <template #header>
          <span>{{ t('workplace.team') }}</span>
        </template>
        <el-skeleton :loading="loading" animated>
          <el-row>
            <el-col v-for="item in team" :key="`team-${item.name}`" :span="12" class="mb-20px">
              <div class="flex items-center">
                <Icon :icon="item.icon" class="mr-10px" />
                <ElLink type="default" :underline="false">
                  {{ item.name }}
                </ElLink>
              </div>
            </el-col>
          </el-row>
        </el-skeleton>
      </el-card>
    </el-col>
  </el-row>
</template>
