<!-- add by 星语：参考 vben2 的方式，增加 TableAction 组件 -->
<script setup lang="ts">
import type { DropdownMixedOption } from 'naive-ui/es/dropdown/src/interface';

import type { PropType } from 'vue';

import type { ActionItem, PopConfirm } from './typing';

import { computed, unref, watch } from 'vue';

import { useAccess } from '@vben/access';
import { IconifyIcon } from '@vben/icons';
import { isBoolean, isFunction } from '@vben/utils';

import { NButton, NDropdown, NPopconfirm, NSpace } from 'naive-ui';

const props = defineProps({
  actions: {
    type: Array as PropType<ActionItem[]>,
    default() {
      return [];
    },
  },
  dropDownActions: {
    type: Array as PropType<ActionItem[]>,
    default() {
      return [];
    },
  },
  divider: {
    type: Boolean,
    default: true,
  },
});

const { hasAccessByCodes } = useAccess();

/** 检查是否显示 */
function isIfShow(action: ActionItem): boolean {
  const ifShow = action.ifShow;
  let isIfShow = true;
  if (isBoolean(ifShow)) {
    isIfShow = ifShow;
  }
  if (isFunction(ifShow)) {
    isIfShow = ifShow(action);
  }
  if (isIfShow) {
    isIfShow =
      hasAccessByCodes(action.auth || []) || (action.auth || []).length === 0;
  }
  return isIfShow;
}

/** 处理按钮 actions */
const getActions = computed(() => {
  const actions = props.actions || [];
  return actions.filter((action: ActionItem) => isIfShow(action));
});

/** 处理下拉菜单 actions */
const getDropdownList = computed((): DropdownMixedOption[] => {
  const dropDownActions = props.dropDownActions || [];
  return dropDownActions
    .filter((action: ActionItem) => isIfShow(action))
    .map((action: ActionItem, index: number) => ({
      label: action.label || '',
      onClick: () => action.onClick?.(),
      key: getActionKey(action, index),
      disabled: action.disabled,
      divider: action.divider || false,
    }));
});

/** Space 组件的 size */
const spaceSize = computed(() => {
  const actions = unref(getActions);
  return actions?.some((item: ActionItem) => item.type === 'primary') ? 4 : 8;
});

/** 获取 PopConfirm 属性 */
function getPopConfirmProps(popConfirm: PopConfirm) {
  if (!popConfirm) return {};

  const attrs: Record<string, any> = {};

  // 复制基本属性，排除函数
  Object.keys(popConfirm).forEach((key) => {
    if (key !== 'confirm' && key !== 'cancel' && key !== 'icon') {
      attrs[key] = popConfirm[key as keyof PopConfirm];
    }
  });

  // 单独处理事件函数
  if (popConfirm.confirm && isFunction(popConfirm.confirm)) {
    attrs.positiveConfirm = popConfirm.confirm;
  }
  if (popConfirm.cancel && isFunction(popConfirm.cancel)) {
    attrs.negativeCancel = popConfirm.cancel;
  }
  if (popConfirm.okText) {
    attrs.positiveText = popConfirm.okText;
  }
  if (popConfirm.cancelText) {
    attrs.negativeText = popConfirm.cancelText;
  }

  return attrs;
}

/** 获取 Button 属性 */
function getButtonProps(action: ActionItem) {
  return {
    type: action.type || 'primary',
    quaternary: action.quaternary || false,
    text: action.text || false,
    disabled: action.disabled,
    loading: action.loading,
    size: action.size,
  };
}

// /** 获取 Tooltip 属性 */
// function getTooltipProps(tooltip: any | string) {
//   if (!tooltip) return {};
//   return typeof tooltip === 'string' ? { title: tooltip } : { ...tooltip };
// }

/** 处理菜单点击 */
function handleMenuClick(key: number) {
  const action = getDropdownList.value.find((item) => item.key === key);
  if (action && action.onClick && isFunction(action.onClick)) {
    action.onClick();
  }
}

/** 生成稳定的 key */
function getActionKey(action: ActionItem, index: number) {
  return `${action.label || ''}-${action.type || ''}-${index}`;
}

/** 处理按钮点击 */
function handleButtonClick(action: ActionItem) {
  if (action.onClick && isFunction(action.onClick)) {
    action.onClick();
  }
}

function handlePopconfirmClick(popconfirm: PopConfirm, type: string) {
  if (type === 'positive') {
    popconfirm.confirm();
  } else if (type === 'negative' && popconfirm.cancel) {
    popconfirm.cancel();
  }
}

/** 监听props变化，强制重新计算 */
watch(
  () => [props.actions, props.dropDownActions],
  () => {
    // 这里不需要额外处理，computed会自动重新计算
  },
  { deep: true },
);
</script>

<template>
  <div class="flex">
    <NSpace :size="spaceSize">
      <template
        v-for="(action, index) in getActions"
        :key="getActionKey(action, index)"
      >
        <NPopconfirm
          v-if="action.popConfirm"
          v-bind="getPopConfirmProps(action.popConfirm)"
          @positive-click="handlePopconfirmClick(action.popConfirm, 'positive')"
          @negative-click="handlePopconfirmClick(action.popConfirm, 'negative')"
        >
          <template v-if="action.popConfirm.icon" #icon>
            <IconifyIcon :icon="action.popConfirm.icon" />
          </template>
          <template #trigger>
            <NButton v-bind="getButtonProps(action)">
              <template v-if="action.icon" #icon>
                <IconifyIcon :icon="action.icon" />
              </template>
              {{ action.label }}
            </NButton>
          </template>
          <span>{{ getPopConfirmProps(action.popConfirm).title }}</span>
        </NPopconfirm>
        <NButton
          v-else
          v-bind="getButtonProps(action)"
          @click="handleButtonClick(action)"
        >
          <template v-if="action.icon" #icon>
            <IconifyIcon :icon="action.icon" />
          </template>
          {{ action.label }}
        </NButton>
      </template>
    </NSpace>

    <NDropdown
      v-if="getDropdownList.length > 0"
      trigger="click"
      :options="getDropdownList"
      :show-arrow="true"
      @select="handleMenuClick"
    >
      <NButton type="primary" text>
        {{ $t('page.action.more') }}
        <template #icon>
          <IconifyIcon icon="lucide:ellipsis-vertical" />
        </template>
      </NButton>
    </NDropdown>
  </div>
</template>
