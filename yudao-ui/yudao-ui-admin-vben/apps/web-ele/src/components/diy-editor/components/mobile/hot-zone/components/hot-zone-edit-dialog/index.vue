<script setup lang="ts">
import type { ControlDot } from './controller';

import type { AppLink } from '#/components/app-link-input/data';
import type { HotZoneItemProperty } from '#/components/diy-editor/components/mobile/HotZone/config';

import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElButton, ElDialog, ElImage } from 'element-plus';

import {
  CONTROL_DOT_LIST,
  CONTROL_TYPE_ENUM,
  HOT_ZONE_MIN_SIZE,
  useDraggable,
  zoomIn,
  zoomOut,
} from './controller';

/** 热区编辑对话框 */
defineOptions({ name: 'HotZoneEditDialog' });

// 定义属性
const props = defineProps({
  modelValue: {
    type: Array<HotZoneItemProperty>,
    default: () => [],
  },
  imgUrl: {
    type: String,
    default: '',
  },
});
const emit = defineEmits(['update:modelValue']);
const formData = ref<HotZoneItemProperty[]>([]);

// 弹窗的是否显示
const dialogVisible = ref(false);
// 打开弹窗
const open = () => {
  // 放大
  formData.value = zoomIn(props.modelValue);
  dialogVisible.value = true;
};
// 提供 open 方法，用于打开弹窗
defineExpose({ open });

// 热区容器
const container = ref<HTMLDivElement>();

// 增加热区
const handleAdd = () => {
  formData.value.push({
    width: HOT_ZONE_MIN_SIZE,
    height: HOT_ZONE_MIN_SIZE,
    top: 0,
    left: 0,
  } as HotZoneItemProperty);
};
// 删除热区
const handleRemove = (hotZone: HotZoneItemProperty) => {
  formData.value = formData.value.filter((item) => item !== hotZone);
};

// 移动热区
const handleMove = (item: HotZoneItemProperty, e: MouseEvent) => {
  useDraggable(item, e, (left, top, _, __, moveWidth, moveHeight) => {
    setLeft(item, left + moveWidth);
    setTop(item, top + moveHeight);
  });
};

// 调整热区大小、位置
const handleResize = (
  item: HotZoneItemProperty,
  ctrlDot: ControlDot,
  e: MouseEvent,
) => {
  useDraggable(item, e, (left, top, width, height, moveWidth, moveHeight) => {
    ctrlDot.types.forEach((type) => {
      switch (type) {
        case CONTROL_TYPE_ENUM.HEIGHT: {
          {
            // 左移时，宽度为减少
            const direction = ctrlDot.types.includes(CONTROL_TYPE_ENUM.TOP)
              ? -1
              : 1;
            setHeight(item, height + moveHeight * direction);
          }
          break;
        }
        case CONTROL_TYPE_ENUM.LEFT: {
          setLeft(item, left + moveWidth);
          break;
        }
        case CONTROL_TYPE_ENUM.TOP: {
          setTop(item, top + moveHeight);
          break;
        }
        case CONTROL_TYPE_ENUM.WIDTH: {
          {
            // 上移时，高度为减少
            const direction = ctrlDot.types.includes(CONTROL_TYPE_ENUM.LEFT)
              ? -1
              : 1;
            setWidth(item, width + moveWidth * direction);
          }
          break;
        }
      }
    });
  });
};

// 设置X轴坐标
const setLeft = (item: HotZoneItemProperty, left: number) => {
  // 不能超出容器
  if (left >= 0 && left <= container.value!.offsetWidth - item.width) {
    item.left = left;
  }
};
// 设置Y轴坐标
const setTop = (item: HotZoneItemProperty, top: number) => {
  // 不能超出容器
  if (top >= 0 && top <= container.value!.offsetHeight - item.height) {
    item.top = top;
  }
};
// 设置宽度
const setWidth = (item: HotZoneItemProperty, width: number) => {
  // 不能小于最小宽度 && 不能超出容器右边
  if (
    width >= HOT_ZONE_MIN_SIZE &&
    item.left + width <= container.value!.offsetWidth
  ) {
    item.width = width;
  }
};
// 设置高度
const setHeight = (item: HotZoneItemProperty, height: number) => {
  // 不能小于最小高度 && 不能超出容器底部
  if (
    height >= HOT_ZONE_MIN_SIZE &&
    item.top + height <= container.value!.offsetHeight
  ) {
    item.height = height;
  }
};

// 处理对话框关闭
const handleSubmit = () => {
  // 会自动触发handleClose
  dialogVisible.value = false;
};

// 处理对话框关闭
const handleClose = () => {
  // 缩小
  const list = zoomOut(formData.value);
  emit('update:modelValue', list);
};

const activeHotZone = ref<HotZoneItemProperty>();
const appLinkDialogRef = ref();
const handleShowAppLinkDialog = (hotZone: HotZoneItemProperty) => {
  activeHotZone.value = hotZone;
  appLinkDialogRef.value.open(hotZone.url);
};
const handleAppLinkChange = (appLink: AppLink) => {
  if (!appLink || !activeHotZone.value) return;
  activeHotZone.value.name = appLink.name;
  activeHotZone.value.url = appLink.path;
};
</script>

<template>
  <ElDialog
    v-model="dialogVisible"
    title="设置热区"
    width="780"
    @close="handleClose"
  >
    <div ref="container" class="w-750px relative h-full">
      <ElImage
        :src="imgUrl"
        class="w-750px pointer-events-none h-full select-none"
      />
      <div
        v-for="(item, hotZoneIndex) in formData"
        :key="hotZoneIndex"
        class="hot-zone"
        :style="{
          width: `${item.width}px`,
          height: `${item.height}px`,
          top: `${item.top}px`,
          left: `${item.left}px`,
        }"
        @mousedown="handleMove(item, $event)"
        @dblclick="handleShowAppLinkDialog(item)"
      >
        <span class="pointer-events-none select-none">{{
          item.name || '双击选择链接'
        }}</span>
        <IconifyIcon
          icon="ep:close"
          class="delete"
          :size="14"
          @click="handleRemove(item)"
        />

        <!-- 8个控制点 -->
        <span
          class="ctrl-dot"
          v-for="(dot, dotIndex) in CONTROL_DOT_LIST"
          :key="dotIndex"
          :style="dot.style"
          @mousedown="handleResize(item, dot, $event)"
        ></span>
      </div>
    </div>
    <template #footer>
      <ElButton @click="handleAdd" type="primary" plain>
        <IconifyIcon icon="ep:plus" class="mr-5px" />
        添加热区
      </ElButton>
      <ElButton @click="handleSubmit" type="primary" plain>
        <IconifyIcon icon="ep:check" class="mr-5px" />
        确定
      </ElButton>
    </template>
  </ElDialog>
  <AppLinkSelectDialog
    ref="appLinkDialogRef"
    @app-link-change="handleAppLinkChange"
  />
</template>

<style scoped lang="scss">
.hot-zone {
  position: absolute;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--el-color-primary);
  cursor: move;
  background: var(--el-color-primary-light-7);
  border: 1px solid var(--el-color-primary);
  opacity: 0.8;

  /* 控制点 */
  .ctrl-dot {
    position: absolute;
    z-index: 11;
    width: 8px;
    height: 8px;
    background-color: #fff;
    border: inherit;
    border-radius: 50%;
  }

  .delete {
    position: absolute;
    top: 0;
    right: 0;
    display: none;
    padding: 2px 2px 6px 6px;
    color: #fff;
    text-align: right;
    cursor: pointer;
    background-color: var(--el-color-primary);
    border-radius: 0 0 0 80%;
  }

  &:hover {
    .delete {
      display: block;
    }
  }
}
</style>
