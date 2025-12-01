<script lang="ts" setup>
import type {
  WorkbenchProjectItem,
  WorkbenchQuickNavItem,
  WorkbenchTodoItem,
  WorkbenchTrendItem,
} from '@vben/common-ui';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import {
  AnalysisChartCard,
  WorkbenchHeader,
  WorkbenchProject,
  WorkbenchQuickNav,
  WorkbenchTodo,
  WorkbenchTrends,
} from '@vben/common-ui';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';
import { openWindow } from '@vben/utils';

import AnalyticsVisitsSource from '../analytics/analytics-visits-source.vue';

const userStore = useUserStore();

// 这是一个示例数据，实际项目中需要根据实际情况进行调整
// url 也可以是内部路由，在 navTo 方法中识别处理，进行内部跳转
// 例如：url: /dashboard/workspace
const projectItems: WorkbenchProjectItem[] = [
  {
    color: '#6DB33F',
    content: 'github.com/YunaiV/ruoyi-vue-pro',
    date: '2025-01-02',
    group: 'Spring Boot 单体架构',
    icon: 'simple-icons:springboot',
    title: 'ruoyi-vue-pro',
    url: 'https://github.com/YunaiV/ruoyi-vue-pro',
  },
  {
    color: '#409EFF',
    content: 'github.com/yudaocode/yudao-ui-admin-vue3',
    date: '2025-02-03',
    group: 'Vue3 + element-plus 管理后台',
    icon: 'ep:element-plus',
    title: 'yudao-ui-admin-vue3',
    url: 'https://github.com/yudaocode/yudao-ui-admin-vue3',
  },
  {
    color: '#ff4d4f',
    content: 'github.com/yudaocode/yudao-mall-uniapp',
    date: '2025-03-04',
    group: 'Vue3 + uniapp 商城手机端',
    icon: 'icon-park-outline:mall-bag',
    title: 'yudao-mall-uniapp',
    url: 'https://github.com/yudaocode/yudao-mall-uniapp',
  },
  {
    color: '#1890ff',
    content: 'github.com/YunaiV/yudao-cloud',
    date: '2025-04-05',
    group: 'Spring Cloud 微服务架构',
    icon: 'material-symbols:cloud-outline',
    title: 'yudao-cloud',
    url: 'https://github.com/YunaiV/yudao-cloud',
  },
  {
    color: '#e18525',
    content: 'github.com/yudaocode/yudao-ui-admin-vben',
    date: '2025-05-06',
    group: 'Vue3 + vben5(antd) 管理后台',
    icon: 'devicon:antdesign',
    title: 'yudao-ui-admin-vben',
    url: 'https://github.com/yudaocode/yudao-ui-admin-vben',
  },
  {
    color: '#2979ff',
    content: 'github.com/yudaocode/yudao-ui-admin-uniapp',
    date: '2025-06-01',
    group: 'Vue3 + uniapp 管理手机端',
    icon: 'ant-design:mobile',
    title: 'yudao-ui-admin-uniapp',
    url: 'https://github.com/yudaocode/yudao-ui-admin-uniapp',
  },
];

// 同样，这里的 url 也可以使用以 http 开头的外部链接
const quickNavItems: WorkbenchQuickNavItem[] = [
  {
    color: '#1fdaca',
    icon: 'ion:home-outline',
    title: '首页',
    url: '/',
  },
  {
    color: '#ff6b6b',
    icon: 'lucide:shopping-bag',
    title: '商城中心',
    url: '/mall',
  },
  {
    color: '#7c3aed',
    icon: 'tabler:ai',
    title: 'AI 大模型',
    url: '/ai',
  },
  {
    color: '#3fb27f',
    icon: 'simple-icons:erpnext',
    title: 'ERP 系统',
    url: '/erp',
  },
  {
    color: '#4daf1bc9',
    icon: 'simple-icons:civicrm',
    title: 'CRM 系统',
    url: '/crm',
  },
  {
    color: '#1a73e8',
    icon: 'fa-solid:hdd',
    title: 'IoT 物联网',
    url: '/iot',
  },
];

const todoItems = ref<WorkbenchTodoItem[]>([
  {
    completed: false,
    content: `系统支持 JDK 8/17/21，Vue 2/3`,
    date: '2024-07-15 09:30:00',
    title: '技术兼容性',
  },
  {
    completed: false,
    content: `后端提供 Spring Boot 2.7/3.2 + Cloud 双架构`,
    date: '2024-08-30 14:20:00',
    title: '架构灵活性',
  },
  {
    completed: false,
    content: `全部开源，个人与企业可 100% 直接使用，无需授权`,
    date: '2024-07-25 16:45:00',
    title: '开源免授权',
  },
  {
    completed: false,
    content: `国内使用最广泛的快速开发平台，远超 10w+ 企业使用`,
    date: '2024-07-10 11:15:00',
    title: '广泛企业认可',
  },
]);
const trendItems: WorkbenchTrendItem[] = [
  {
    avatar: 'svg:avatar-1',
    content: `在 <a>开源组</a> 创建了项目 <a>Vue</a>`,
    date: '刚刚',
    title: '威廉',
  },
  {
    avatar: 'svg:avatar-2',
    content: `关注了 <a>威廉</a> `,
    date: '1个小时前',
    title: '艾文',
  },
  {
    avatar: 'svg:avatar-3',
    content: `发布了 <a>个人动态</a> `,
    date: '1天前',
    title: '克里斯',
  },
  {
    avatar: 'svg:avatar-4',
    content: `发表文章 <a>如何编写一个Vite插件</a> `,
    date: '2天前',
    title: 'Vben',
  },
  {
    avatar: 'svg:avatar-1',
    content: `回复了 <a>杰克</a> 的问题 <a>如何进行项目优化？</a>`,
    date: '3天前',
    title: '皮特',
  },
  {
    avatar: 'svg:avatar-2',
    content: `关闭了问题 <a>如何运行项目</a> `,
    date: '1周前',
    title: '杰克',
  },
  {
    avatar: 'svg:avatar-3',
    content: `发布了 <a>个人动态</a> `,
    date: '1周前',
    title: '威廉',
  },
  {
    avatar: 'svg:avatar-4',
    content: `推送了代码到 <a>Github</a>`,
    date: '2021-04-01 20:00',
    title: '威廉',
  },
  {
    avatar: 'svg:avatar-4',
    content: `发表文章 <a>如何编写使用 Admin Vben</a> `,
    date: '2021-03-01 20:00',
    title: 'Vben',
  },
];

const router = useRouter();

// 这是一个示例方法，实际项目中需要根据实际情况进行调整
// This is a sample method, adjust according to the actual project requirements
function navTo(nav: WorkbenchProjectItem | WorkbenchQuickNavItem) {
  if (nav.url?.startsWith('http')) {
    openWindow(nav.url);
    return;
  }
  if (nav.url?.startsWith('/')) {
    router.push(nav.url).catch((error) => {
      console.error('Navigation failed:', error);
    });
  } else {
    console.warn(`Unknown URL for navigation item: ${nav.title} -> ${nav.url}`);
  }
}
</script>

<template>
  <div class="p-5">
    <WorkbenchHeader
      :avatar="userStore.userInfo?.avatar || preferences.app.defaultAvatar"
    >
      <template #title>
        早安, {{ userStore.userInfo?.nickname }}, 开始您一天的工作吧！
      </template>
      <template #description> 今日晴，20℃ - 32℃！ </template>
    </WorkbenchHeader>

    <div class="mt-5 flex flex-col lg:flex-row">
      <div class="mr-4 w-full lg:w-3/5">
        <WorkbenchProject :items="projectItems" title="项目" @click="navTo" />
        <WorkbenchTrends :items="trendItems" class="mt-5" title="最新动态" />
      </div>
      <div class="w-full lg:w-2/5">
        <WorkbenchQuickNav
          :items="quickNavItems"
          class="mt-5 lg:mt-0"
          title="快捷导航"
          @click="navTo"
        />
        <WorkbenchTodo :items="todoItems" class="mt-5" title="待办事项" />
        <AnalysisChartCard class="mt-5" title="访问来源">
          <AnalyticsVisitsSource />
        </AnalysisChartCard>
      </div>
    </div>
  </div>
</template>
