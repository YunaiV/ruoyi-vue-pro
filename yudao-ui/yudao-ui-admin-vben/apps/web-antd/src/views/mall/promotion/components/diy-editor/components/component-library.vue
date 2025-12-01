<script setup lang="ts">
import type { DiyComponent, DiyComponentLibrary } from '../util';

import { ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import { Collapse } from 'ant-design-vue';
import draggable from 'vuedraggable';

import { componentConfigs } from './mobile/index';

/** 组件库：目前左侧的【基础组件】、【图文组件】部分 */
defineOptions({ name: 'ComponentLibrary' });

/** 组件列表 */
const props = defineProps<{
  list: DiyComponentLibrary[];
}>();

// TODO @xingyu：要不要换成 reactive，和 ele 一致
const groups = ref<any[]>([]); // 组件分组
const extendGroups = ref<string[]>([]); // 展开的折叠面板

/** 监听 list 属性，按照 DiyComponentLibrary 的 name 分组 */
watch(
  () => props.list,
  () => {
    // 清除旧数据
    extendGroups.value = [];
    groups.value = [];
    // 重新生成数据
    props.list.forEach((group) => {
      // 是否展开分组
      if (group.extended) {
        extendGroups.value.push(group.name);
      }
      // 查找组件
      const components = group.components
        .map((name) => componentConfigs[name] as DiyComponent<any>)
        .filter(Boolean);
      if (components.length > 0) {
        groups.value.push({
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
  <div class="z-[1] max-h-[calc(80vh)] shrink-0 select-none overflow-y-auto">
    <Collapse
      v-model:active-key="extendGroups"
      :bordered="false"
      class="bg-card"
    >
      <Collapse.Panel
        v-for="(group, index) in groups"
        :key="group.name"
        :header="group.name"
        :force-render="true"
      >
        <draggable
          class="flex flex-wrap items-center"
          ghost-class="draggable-ghost"
          :item-key="index.toString()"
          :list="group.components"
          :sort="false"
          :group="{ name: 'component', pull: 'clone', put: false }"
          :clone="handleCloneComponent"
          :animation="200"
          :force-fallback="false"
        >
          <template #item="{ element }">
            <div
              class="component flex h-20 w-20 cursor-move flex-col items-center justify-center hover:border-2 hover:border-blue-500"
            >
              <IconifyIcon
                :icon="element.icon"
                class="mb-1 size-8 text-gray-500"
              />
              <span class="mt-1 text-xs">{{ element.name }}</span>
            </div>
          </template>
        </draggable>
      </Collapse.Panel>
    </Collapse>
  </div>
  <!-- TODO @xingyu：ele 里面有一些 style，看看是不是都迁移完了；特别是 drag-area 是全局样式； -->
</template>
