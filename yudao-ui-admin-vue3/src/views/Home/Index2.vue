<template>
  <el-row :gutter="20" justify="space-between" :class="prefixCls">
    <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
      <el-card shadow="hover" class="mb-20px">
        <el-skeleton :loading="loading" animated :rows="2">
          <template #default>
            <div :class="`${prefixCls}__item flex justify-between`">
              <div>
                <div
                  :class="`${prefixCls}__item--icon ${prefixCls}__item--peoples p-16px inline-block rounded-6px`"
                >
                  <Icon icon="svg-icon:peoples" :size="40" />
                </div>
              </div>
              <div class="flex flex-col justify-between">
                <div :class="`${prefixCls}__item--text text-16px text-gray-500 text-right`">{{
                  t('analysis.newUser')
                }}</div>
                <CountTo
                  class="text-20px font-700 text-right"
                  :start-val="0"
                  :end-val="102400"
                  :duration="2600"
                />
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </el-col>

    <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
      <el-card shadow="hover" class="mb-20px">
        <el-skeleton :loading="loading" animated :rows="2">
          <template #default>
            <div :class="`${prefixCls}__item flex justify-between`">
              <div>
                <div
                  :class="`${prefixCls}__item--icon ${prefixCls}__item--message p-16px inline-block rounded-6px`"
                >
                  <Icon icon="svg-icon:message" :size="40" />
                </div>
              </div>
              <div class="flex flex-col justify-between">
                <div :class="`${prefixCls}__item--text text-16px text-gray-500 text-right`">{{
                  t('analysis.unreadInformation')
                }}</div>
                <CountTo
                  class="text-20px font-700 text-right"
                  :start-val="0"
                  :end-val="81212"
                  :duration="2600"
                />
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </el-col>

    <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
      <el-card shadow="hover" class="mb-20px">
        <el-skeleton :loading="loading" animated :rows="2">
          <template #default>
            <div :class="`${prefixCls}__item flex justify-between`">
              <div>
                <div
                  :class="`${prefixCls}__item--icon ${prefixCls}__item--money p-16px inline-block rounded-6px`"
                >
                  <Icon icon="svg-icon:money" :size="40" />
                </div>
              </div>
              <div class="flex flex-col justify-between">
                <div :class="`${prefixCls}__item--text text-16px text-gray-500 text-right`">{{
                  t('analysis.transactionAmount')
                }}</div>
                <CountTo
                  class="text-20px font-700 text-right"
                  :start-val="0"
                  :end-val="9280"
                  :duration="2600"
                />
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </el-col>

    <el-col :xl="6" :lg="6" :md="12" :sm="12" :xs="24">
      <el-card shadow="hover" class="mb-20px">
        <el-skeleton :loading="loading" animated :rows="2">
          <template #default>
            <div :class="`${prefixCls}__item flex justify-between`">
              <div>
                <div
                  :class="`${prefixCls}__item--icon ${prefixCls}__item--shopping p-16px inline-block rounded-6px`"
                >
                  <Icon icon="svg-icon:shopping" :size="40" />
                </div>
              </div>
              <div class="flex flex-col justify-between">
                <div :class="`${prefixCls}__item--text text-16px text-gray-500 text-right`">{{
                  t('analysis.totalShopping')
                }}</div>
                <CountTo
                  class="text-20px font-700 text-right"
                  :start-val="0"
                  :end-val="13600"
                  :duration="2600"
                />
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </el-col>
  </el-row>
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
</template>
<script setup lang="ts">
import { set } from 'lodash-es'
import { EChartsOption } from 'echarts'

import { useDesign } from '@/hooks/web/useDesign'
import type { AnalysisTotalTypes } from './types'
import { pieOptions, barOptions, lineOptions } from './echarts-data'

const { t } = useI18n()
const loading = ref(true)
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('panel')
const pieOptionsData = reactive<EChartsOption>(pieOptions) as EChartsOption

let totalState = reactive<AnalysisTotalTypes>({
  users: 0,
  messages: 0,
  moneys: 0,
  shoppings: 0
})

const getCount = async () => {
  const data = {
    users: 102400,
    messages: 81212,
    moneys: 9280,
    shoppings: 13600
  }
  totalState = Object.assign(totalState, data)
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
  set(pieOptionsData, 'series.data', data)
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
  await Promise.all([getCount(), getUserAccessSource(), getWeeklyUserActivity(), getMonthlySales()])
  loading.value = false
}

getAllApi()
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-panel;

.#{$prefix-cls} {
  &__item {
    &--peoples {
      color: #40c9c6;
    }

    &--message {
      color: #36a3f7;
    }

    &--money {
      color: #f4516c;
    }

    &--shopping {
      color: #34bfa3;
    }

    &:hover {
      :deep(.#{$namespace}-icon) {
        color: #fff !important;
      }
      .#{$prefix-cls}__item--icon {
        transition: all 0.38s ease-out;
      }
      .#{$prefix-cls}__item--peoples {
        background: #40c9c6;
      }
      .#{$prefix-cls}__item--message {
        background: #36a3f7;
      }
      .#{$prefix-cls}__item--money {
        background: #f4516c;
      }
      .#{$prefix-cls}__item--shopping {
        background: #34bfa3;
      }
    }
  }
}
</style>
