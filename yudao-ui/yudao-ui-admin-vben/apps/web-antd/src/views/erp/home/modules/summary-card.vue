<script lang="ts" setup>
import type { AnalysisOverviewItem } from '@vben/common-ui';

import { computed } from 'vue';

import { AnalysisOverview } from '@vben/common-ui';
import {
  SvgBellIcon,
  SvgCakeIcon,
  SvgCardIcon,
  SvgDownloadIcon,
} from '@vben/icons';

interface Props {
  saleSummary?: {
    monthPrice?: number;
    todayPrice?: number;
    yearPrice?: number;
    yesterdayPrice?: number;
  };
  purchaseSummary?: {
    monthPrice?: number;
    todayPrice?: number;
    yearPrice?: number;
    yesterdayPrice?: number;
  };
}

const props = withDefaults(defineProps<Props>(), {
  saleSummary: () => ({}),
  purchaseSummary: () => ({}),
});

/** 概览数据 */
const overviewItems = computed<AnalysisOverviewItem[]>(() => [
  {
    icon: SvgCardIcon,
    title: '今日销售',
    totalTitle: '今日采购',
    totalValue: props.purchaseSummary?.todayPrice || 0,
    value: props.saleSummary?.todayPrice || 0,
  },
  {
    icon: SvgCakeIcon,
    title: '昨日销售',
    totalTitle: '昨日采购',
    totalValue: props.purchaseSummary?.yesterdayPrice || 0,
    value: props.saleSummary?.yesterdayPrice || 0,
  },
  {
    icon: SvgDownloadIcon,
    title: '本月销售',
    totalTitle: '本月采购',
    totalValue: props.purchaseSummary?.monthPrice || 0,
    value: props.saleSummary?.monthPrice || 0,
  },
  {
    icon: SvgBellIcon,
    title: '今年销售',
    totalTitle: '今年采购',
    totalValue: props.purchaseSummary?.yearPrice || 0,
    value: props.saleSummary?.yearPrice || 0,
  },
]);
</script>

<template>
  <AnalysisOverview :items="overviewItems" />
</template>
