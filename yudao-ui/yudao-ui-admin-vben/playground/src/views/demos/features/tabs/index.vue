<script lang="ts" setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';

import { Button, Card, Input } from 'ant-design-vue';

const router = useRouter();
const newTabTitle = ref('');

const {
  closeAllTabs,
  closeCurrentTab,
  closeLeftTabs,
  closeOtherTabs,
  closeRightTabs,
  closeTabByKey,
  refreshTab,
  resetTabTitle,
  setTabTitle,
} = useTabs();

function openTab() {
  // 这里就是路由跳转，也可以用path
  router.push({ name: 'VbenAbout' });
}

function openTabWithParams(id: number) {
  // 这里就是路由跳转，也可以用path
  router.push({ name: 'FeatureTabDetailDemo', params: { id } });
}

function reset() {
  newTabTitle.value = '';
  resetTabTitle();
}
</script>

<template>
  <Page description="用于需要操作标签页的场景" title="标签页">
    <Card class="mb-5" title="打开/关闭标签页">
      <div class="text-foreground/80 mb-3">
        如果标签页存在，直接跳转切换。如果标签页不存在，则打开新的标签页。
      </div>
      <div class="flex flex-wrap gap-3">
        <Button type="primary" @click="openTab"> 打开 "关于" 标签页 </Button>
        <Button type="primary" @click="closeTabByKey('/vben-admin/about')">
          关闭 "关于" 标签页
        </Button>
      </div>
    </Card>

    <Card class="mb-5" title="标签页操作">
      <div class="text-foreground/80 mb-3">用于动态控制标签页的各种操作</div>
      <div class="flex flex-wrap gap-3">
        <Button type="primary" @click="closeCurrentTab()">
          关闭当前标签页
        </Button>
        <Button type="primary" @click="closeLeftTabs()">
          关闭左侧标签页
        </Button>
        <Button type="primary" @click="closeRightTabs()">
          关闭右侧标签页
        </Button>
        <Button type="primary" @click="closeAllTabs()"> 关闭所有标签页 </Button>
        <Button type="primary" @click="closeOtherTabs()">
          关闭其他标签页
        </Button>
        <Button type="primary" @click="refreshTab()"> 刷新当前标签页 </Button>
      </div>
    </Card>

    <Card class="mb-5" title="动态标题">
      <div class="text-foreground/80 mb-3">
        该操作不会影响页面标题，仅修改Tab标题
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <Input
          v-model:value="newTabTitle"
          class="w-40"
          placeholder="请输入新标题"
        />
        <Button type="primary" @click="() => setTabTitle(newTabTitle)">
          修改
        </Button>
        <Button @click="reset"> 重置 </Button>
      </div>
    </Card>

    <Card class="mb-5" title="最大打开数量">
      <div class="text-foreground/80 mb-3">
        限制带参数的tab打开的最大数量，由 `route.meta.maxNumOfOpenTab` 控制
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <template v-for="item in 5" :key="item">
          <Button type="primary" @click="openTabWithParams(item)">
            打开{{ item }}详情页
          </Button>
        </template>
      </div>
    </Card>
  </Page>
</template>
