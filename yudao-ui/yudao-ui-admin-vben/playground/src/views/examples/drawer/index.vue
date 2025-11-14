<script lang="ts" setup>
import type { DrawerPlacement, DrawerState } from '@vben/common-ui';

import { Page, useVbenDrawer } from '@vben/common-ui';

import { Button, Card } from 'ant-design-vue';

import DocButton from '../doc-button.vue';
import AutoHeightDemo from './auto-height-demo.vue';
import BaseDemo from './base-demo.vue';
import DynamicDemo from './dynamic-demo.vue';
import FormDrawerDemo from './form-drawer-demo.vue';
import inContentDemo from './in-content-demo.vue';
import SharedDataDemo from './shared-data-demo.vue';

defineOptions({ name: 'DrawerExample' });
const [BaseDrawer, baseDrawerApi] = useVbenDrawer({
  // 连接抽离的组件
  connectedComponent: BaseDemo,
  // placement: 'left',
});

const [InContentDrawer, inContentDrawerApi] = useVbenDrawer({
  // 连接抽离的组件
  connectedComponent: inContentDemo,
  // placement: 'left',
});

const [AutoHeightDrawer, autoHeightDrawerApi] = useVbenDrawer({
  connectedComponent: AutoHeightDemo,
});

const [DynamicDrawer, dynamicDrawerApi] = useVbenDrawer({
  connectedComponent: DynamicDemo,
});

const [SharedDataDrawer, sharedDrawerApi] = useVbenDrawer({
  connectedComponent: SharedDataDemo,
});

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: FormDrawerDemo,
});

function openBaseDrawer(placement: DrawerPlacement = 'right') {
  baseDrawerApi.setState({ placement }).open();
}

function openBlurDrawer() {
  baseDrawerApi.setState({ overlayBlur: 5 }).open();
}

function openInContentDrawer(placement: DrawerPlacement = 'right') {
  const state: Partial<DrawerState> = { class: '', placement };
  if (placement === 'top') {
    // 页面顶部区域的层级只有200，所以设置一个低于200的值，抽屉从顶部滑出来的时候才比较合适
    state.zIndex = 199;
  }
  inContentDrawerApi.setState(state).open();
}

function openMaxContentDrawer() {
  // 这里只是用来演示方便。实际上自己使用的时候可以直接将这些配置卸载Drawer的属性里
  inContentDrawerApi.setState({ class: 'w-full', placement: 'right' }).open();
}

function openAutoHeightDrawer() {
  autoHeightDrawerApi.open();
}

function openDynamicDrawer() {
  dynamicDrawerApi.open();
}

function handleUpdateTitle() {
  dynamicDrawerApi.setState({ title: '外部动态标题' }).open();
}

function openSharedDrawer() {
  sharedDrawerApi
    .setData({
      content: '外部传递的数据 content',
      payload: '外部传递的数据 payload',
    })
    .open();
}

function openFormDrawer() {
  formDrawerApi
    .setData({
      // 表单值
      values: { field1: 'abc', field2: '123' },
    })
    .open();
}
</script>

<template>
  <Page
    auto-content-height
    description="抽屉组件通常用于在当前页面上显示一个覆盖层，用以展示重要信息或提供用户交互界面。"
    title="抽屉组件示例"
  >
    <template #extra>
      <DocButton path="/components/common-ui/vben-drawer" />
    </template>
    <BaseDrawer />
    <InContentDrawer />
    <AutoHeightDrawer />
    <DynamicDrawer />
    <SharedDataDrawer />
    <FormDrawer />

    <Card class="mb-4" title="基本使用">
      <p class="mb-3">一个基础的抽屉示例</p>
      <Button class="mb-2" type="primary" @click="openBaseDrawer('right')">
        右侧打开
      </Button>
      <Button
        class="mb-2 ml-2"
        type="primary"
        @click="openBaseDrawer('bottom')"
      >
        底部打开
      </Button>
      <Button class="mb-2 ml-2" type="primary" @click="openBaseDrawer('left')">
        左侧打开
      </Button>
      <Button class="mb-2 ml-2" type="primary" @click="openBaseDrawer('top')">
        顶部打开
      </Button>
      <Button class="mb-2 ml-2" type="primary" @click="openBlurDrawer">
        遮罩层模糊效果
      </Button>
    </Card>

    <Card class="mb-4" title="在内容区域打开">
      <p class="mb-3">指定抽屉在内容区域打开，不会覆盖顶部和左侧菜单等区域</p>
      <Button class="mb-2" type="primary" @click="openInContentDrawer('right')">
        右侧打开
      </Button>
      <Button
        class="mb-2 ml-2"
        type="primary"
        @click="openInContentDrawer('bottom')"
      >
        底部打开
      </Button>
      <Button
        class="mb-2 ml-2"
        type="primary"
        @click="openInContentDrawer('left')"
      >
        左侧打开
      </Button>
      <Button
        class="mb-2 ml-2"
        type="primary"
        @click="openInContentDrawer('top')"
      >
        顶部打开
      </Button>
      <Button class="mb-2 ml-2" type="primary" @click="openMaxContentDrawer">
        内容区域全屏打开
      </Button>
    </Card>

    <Card class="mb-4" title="内容高度自适应滚动">
      <p class="mb-3">可根据内容自动计算滚动高度</p>
      <Button type="primary" @click="openAutoHeightDrawer">打开抽屉</Button>
    </Card>

    <Card class="mb-4" title="动态配置示例">
      <p class="mb-3">通过 setState 动态调整抽屉数据</p>
      <Button type="primary" @click="openDynamicDrawer">打开抽屉</Button>
      <Button class="ml-2" type="primary" @click="handleUpdateTitle">
        从外部修改标题并打开
      </Button>
    </Card>

    <Card class="mb-4" title="内外数据共享示例">
      <p class="mb-3">通过共享 sharedData 来进行数据交互</p>
      <Button type="primary" @click="openSharedDrawer">
        打开抽屉并传递数据
      </Button>
    </Card>

    <Card class="mb-4" title="表单抽屉示例">
      <p class="mb-3">打开抽屉并设置表单schema以及数据</p>
      <Button type="primary" @click="openFormDrawer">
        打开抽屉并设置表单schema以及数据
      </Button>
    </Card>
  </Page>
</template>
