<script setup lang="ts">
import type {
  ComponentStyle,
  DiyComponent,
} from '#/components/diy-editor/util';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElButton, ElTooltip } from 'element-plus';

import { components } from '#/components/diy-editor/components/mobile';
import VerticalButtonGroup from '#/components/vertical-button-group/index.vue';

/**
 * 组件容器：目前在中间部分
 * 用于包裹组件，为组件提供 背景、外边距、内边距、边框等样式
 */
defineOptions({ name: 'ComponentContainer', components });

const props = defineProps({
  component: {
    type: Object as () => DiyComponentWithStyle,
    required: true,
  },
  active: {
    type: Boolean,
    default: false,
  },
  canMoveUp: {
    type: Boolean,
    default: false,
  },
  canMoveDown: {
    type: Boolean,
    default: false,
  },
  showToolbar: {
    type: Boolean,
    default: true,
  },
});

const emits = defineEmits<{
  (e: 'move', direction: number): void;
  (e: 'copy'): void;
  (e: 'delete'): void;
}>();

type DiyComponentWithStyle = DiyComponent<any> & {
  property: { style?: ComponentStyle };
};
/**
 * 组件样式
 */
const style = computed(() => {
  const componentStyle = props.component.property.style;
  if (!componentStyle) {
    return {};
  }
  return {
    marginTop: `${componentStyle.marginTop || 0}px`,
    marginBottom: `${componentStyle.marginBottom || 0}px`,
    marginLeft: `${componentStyle.marginLeft || 0}px`,
    marginRight: `${componentStyle.marginRight || 0}px`,
    paddingTop: `${componentStyle.paddingTop || 0}px`,
    paddingRight: `${componentStyle.paddingRight || 0}px`,
    paddingBottom: `${componentStyle.paddingBottom || 0}px`,
    paddingLeft: `${componentStyle.paddingLeft || 0}px`,
    borderTopLeftRadius: `${componentStyle.borderTopLeftRadius || 0}px`,
    borderTopRightRadius: `${componentStyle.borderTopRightRadius || 0}px`,
    borderBottomRightRadius: `${componentStyle.borderBottomRightRadius || 0}px`,
    borderBottomLeftRadius: `${componentStyle.borderBottomLeftRadius || 0}px`,
    overflow: 'hidden',
    background:
      componentStyle.bgType === 'color'
        ? componentStyle.bgColor
        : `url(${componentStyle.bgImg})`,
  };
});

/**
 * 移动组件
 * @param direction 移动方向
 */
const handleMoveComponent = (direction: number) => {
  emits('move', direction);
};

/**
 * 复制组件
 */
const handleCopyComponent = () => {
  emits('copy');
};

/**
 * 删除组件
 */
const handleDeleteComponent = () => {
  emits('delete');
};
</script>
<template>
  <div class="component" :class="[{ active }]">
    <div
      :style="{
        ...style,
      }"
    >
      <component :is="component.id" :property="component.property" />
    </div>
    <div class="component-wrap">
      <!-- 左侧：组件名（悬浮的小贴条） -->
      <div class="component-name" v-if="component.name">
        {{ component.name }}
      </div>
      <!-- 右侧：组件操作工具栏 -->
      <div
        class="component-toolbar"
        v-if="showToolbar && component.name && active"
      >
        <VerticalButtonGroup type="primary">
          <ElTooltip content="上移" placement="right">
            <ElButton
              :disabled="!canMoveUp"
              @click.stop="handleMoveComponent(-1)"
            >
              <IconifyIcon icon="ep:arrow-up" />
            </ElButton>
          </ElTooltip>
          <ElTooltip content="下移" placement="right">
            <ElButton
              :disabled="!canMoveDown"
              @click.stop="handleMoveComponent(1)"
            >
              <IconifyIcon icon="ep:arrow-down" />
            </ElButton>
          </ElTooltip>
          <ElTooltip content="复制" placement="right">
            <ElButton @click.stop="handleCopyComponent()">
              <IconifyIcon icon="ep:copy-document" />
            </ElButton>
          </ElTooltip>
          <ElTooltip content="删除" placement="right">
            <ElButton @click.stop="handleDeleteComponent()">
              <IconifyIcon icon="ep:delete" />
            </ElButton>
          </ElTooltip>
        </VerticalButtonGroup>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
$active-border-width: 2px;
$hover-border-width: 1px;
$name-position: -85px;
$toolbar-position: -55px;

/* 组件 */
.component {
  position: relative;
  cursor: move;

  .component-wrap {
    position: absolute;
    top: 0;
    left: -$active-border-width;
    display: block;
    width: 100%;
    height: 100%;

    /* 鼠标放到组件上时 */
    &:hover {
      border: $hover-border-width dashed var(--el-color-primary);
      box-shadow: 0 0 5px 0 rgb(24 144 255 / 30%);

      .component-name {
        top: $hover-border-width;

        /* 防止加了边框之后，位置移动 */
        left: $name-position - $hover-border-width;
      }
    }

    /* 左侧：组件名称 */
    .component-name {
      position: absolute;
      top: $active-border-width;
      left: $name-position;
      display: block;
      width: 80px;
      height: 25px;
      font-size: 12px;
      line-height: 25px;
      color: #6a6a6a;
      text-align: center;
      background: #fff;
      box-shadow:
        0 0 4px #00000014,
        0 2px 6px #0000000f,
        0 4px 8px 2px #0000000a;

      /* 右侧小三角 */
      &::after {
        position: absolute;
        top: 7.5px;
        right: -10px;
        width: 0;
        height: 0;
        content: ' ';
        border: 5px solid transparent;
        border-left-color: #fff;
      }
    }

    /* 右侧：组件操作工具栏 */
    .component-toolbar {
      position: absolute;
      top: 0;
      right: $toolbar-position;
      display: none;

      /* 左侧小三角 */
      &::before {
        position: absolute;
        top: 10px;
        left: -10px;
        width: 0;
        height: 0;
        content: ' ';
        border: 5px solid transparent;
        border-right-color: #2d8cf0;
      }
    }
  }

  /* 组件选中时 */
  &.active {
    margin-bottom: 4px;

    .component-wrap {
      margin-bottom: $active-border-width + $active-border-width;
      border: $active-border-width solid var(--el-color-primary) !important;
      box-shadow: 0 0 10px 0 rgb(24 144 255 / 30%);

      .component-name {
        top: 0 !important;

        /* 防止加了边框之后，位置移动 */
        left: $name-position - $active-border-width !important;
        color: #fff;
        background: var(--el-color-primary);

        &::after {
          border-left-color: var(--el-color-primary);
        }
      }

      .component-toolbar {
        display: block;
      }
    }
  }
}
</style>
