<script setup lang="ts">
import type { DiyComponent, DiyComponentLibrary } from '../util';

import { reactive, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import { ElAside, ElCollapse, ElCollapseItem, ElScrollbar } from 'element-plus';
import draggable from 'vuedraggable';

import { componentConfigs } from './mobile/index';

/** 组件库：目前左侧的【基础组件】、【图文组件】部分 */
defineOptions({ name: 'ComponentLibrary' });

/** 组件列表 */
const props = defineProps<{
  list: DiyComponentLibrary[];
}>();

const groups = reactive<any[]>([]); // 组件分组
const extendGroups = reactive<string[]>([]); // 展开的折叠面板

/** 监听 list 属性，按照 DiyComponentLibrary 的 name 分组 */
watch(
  () => props.list,
  () => {
    // 清除旧数据
    extendGroups.length = 0;
    groups.length = 0;
    // 重新生成数据
    props.list.forEach((group) => {
      // 是否展开分组
      if (group.extended) {
        extendGroups.push(group.name);
      }
      // 查找组件
      const components = group.components
        .map((name) => componentConfigs[name] as DiyComponent<any>)
        .filter(Boolean);
      if (components.length > 0) {
        groups.push({
          name: group.name,
          components,
        });
      }
    });
  },
  {
    immediate: true,
  },
);

/** 克隆组件 */
function handleCloneComponent(component: DiyComponent<any>) {
  const instance = cloneDeep(component);
  instance.uid = Date.now();
  return instance;
}
</script>

<template>
  <ElAside
    class="editor-left z-[1] shrink-0 select-none shadow-[8px_0_8px_-8px_rgb(0_0_0/0.12)]"
    width="261px"
  >
    <ElScrollbar>
      <ElCollapse v-model="extendGroups">
        <ElCollapseItem
          v-for="group in groups"
          :key="group.name"
          :name="group.name"
          :title="group.name"
        >
          <draggable
            class="flex flex-wrap items-center"
            ghost-class="draggable-ghost"
            item-key="index"
            :list="group.components"
            :sort="false"
            :group="{ name: 'component', pull: 'clone', put: false }"
            :clone="handleCloneComponent"
            :animation="200"
            :force-fallback="false"
          >
            <template #item="{ element }">
              <div>
                <div class="hidden text-white">组件放置区域</div>
                <div
                  class="component flex h-[86px] w-[86px] cursor-move flex-col items-center justify-center border-b border-r [&:nth-of-type(3n)]:border-r-0"
                  :style="{
                    borderColor: 'var(--el-border-color-lighter)',
                  }"
                >
                  <IconifyIcon
                    :icon="element.icon"
                    :size="32"
                    class="mb-1 text-gray-500"
                  />
                  <span class="mt-1 text-xs">{{ element.name }}</span>
                </div>
              </div>
            </template>
          </draggable>
        </ElCollapseItem>
      </ElCollapse>
    </ElScrollbar>
  </ElAside>
</template>

<style scoped lang="scss">
.editor-left {
  :deep(.el-collapse) {
    border-top: none;
  }

  :deep(.el-collapse-item__wrap) {
    border-bottom: none;
  }

  :deep(.el-collapse-item__content) {
    padding-bottom: 0;
  }

  :deep(.el-collapse-item__header) {
    height: 32px;
    padding: 0 24px;
    line-height: 32px;
    background-color: var(--el-bg-color-page);
    border-bottom: none;
  }

  /* 组件 hover 和 active 状态（需要 CSS 变量） */
  .component.active,
  .component:hover {
    color: var(--el-color-white);
    background: var(--el-color-primary);

    :deep(.iconify) {
      color: var(--el-color-white);
    }
  }
}

/* 拖拽区域全局样式 */
.drag-area {
  /* 拖拽到手机区域时的样式 */
  .draggable-ghost {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 40px;

    /* 条纹背景 */
    background: linear-gradient(
      45deg,
      #91a8d5 0,
      #91a8d5 10%,
      #94b4eb 10%,
      #94b4eb 50%,
      #91a8d5 50%,
      #91a8d5 60%,
      #94b4eb 60%,
      #94b4eb
    );
    background-size: 1rem 1rem;
    transition: all 0.5s;

    span {
      display: inline-block;
      width: 140px;
      height: 25px;
      font-size: 12px;
      line-height: 25px;
      color: #fff;
      text-align: center;
      background: #5487df;
    }

    .component {
      display: none; /* 拖拽时隐藏组件 */
    }

    .hidden {
      display: block !important; /* 拖拽时显示占位提示 */
    }
  }
}
</style>
