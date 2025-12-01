<script setup lang="ts">
import type { PropType } from 'vue';

import type { AiModelChatRoleApi } from '#/api/ai/model/chatRole';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  ElAvatar,
  ElButton,
  ElCard,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
} from 'element-plus';

const props = defineProps({
  loading: {
    type: Boolean,
    required: true,
  },
  roleList: {
    type: Array as PropType<AiModelChatRoleApi.ChatRole[]>,
    required: true,
  },
  showMore: {
    type: Boolean,
    required: false,
    default: false,
  },
});

const emits = defineEmits(['onDelete', 'onEdit', 'onUse', 'onPage']);

const tabsRef = ref<any>();

/** 操作：编辑、删除 */
async function handleMoreClick(type: string, role: any) {
  if (type === 'delete') {
    emits('onDelete', role);
  } else {
    emits('onEdit', role);
  }
}

/** 选中 */
function handleUseClick(role: any) {
  emits('onUse', role);
}

/** 滚动 */
async function handleTabsScroll() {
  if (tabsRef.value) {
    const { scrollTop, scrollHeight, clientHeight } = tabsRef.value;
    if (scrollTop + clientHeight >= scrollHeight - 20 && !props.loading) {
      emits('onPage');
    }
  }
}
</script>

<template>
  <div
    class="relative flex h-full flex-wrap content-start items-start overflow-auto pb-36"
    ref="tabsRef"
    @scroll="handleTabsScroll"
  >
    <div class="mb-3 mr-3 inline-block" v-for="role in roleList" :key="role.id">
      <ElCard
        class="relative rounded-lg"
        body-style="position: relative; display: flex; flex-direction: column; justify-content: flex-start; width: 240px; max-width: 240px; padding: 15px;"
      >
        <!-- 头部：头像、名称 -->
        <div class="flex items-center justify-between">
          <div class="flex min-w-0 flex-1 items-center">
            <ElAvatar
              :src="role.avatar"
              class="h-8 w-8 flex-shrink-0 overflow-hidden"
            />
            <div class="ml-2 truncate text-base font-medium">
              {{ role.name }}
            </div>
          </div>
        </div>
        <!-- 描述信息 -->
        <div
          class="mt-2 line-clamp-2 h-10 overflow-hidden text-sm text-gray-600"
        >
          {{ role.description }}
        </div>
        <!-- 底部操作按钮 -->
        <div class="flex items-center justify-end gap-2">
          <ElDropdown v-if="showMore">
            <ElButton size="small">
              <IconifyIcon icon="lucide:ellipsis" />
            </ElButton>
            <template #dropdown>
              <ElDropdownMenu>
                <ElDropdownItem @click="handleMoreClick('delete', role)">
                  <div class="flex items-center">
                    <IconifyIcon icon="lucide:trash" color="red" />
                    <span class="ml-2 text-red-500">删除</span>
                  </div>
                </ElDropdownItem>
                <ElDropdownItem @click="handleMoreClick('edit', role)">
                  <div class="flex items-center">
                    <IconifyIcon icon="lucide:edit" color="#787878" />
                    <span class="ml-2 text-primary">编辑</span>
                  </div>
                </ElDropdownItem>
              </ElDropdownMenu>
            </template>
          </ElDropdown>
          <ElButton type="primary" size="small" @click="handleUseClick(role)">
            使用
          </ElButton>
        </div>
      </ElCard>
    </div>
  </div>
</template>
