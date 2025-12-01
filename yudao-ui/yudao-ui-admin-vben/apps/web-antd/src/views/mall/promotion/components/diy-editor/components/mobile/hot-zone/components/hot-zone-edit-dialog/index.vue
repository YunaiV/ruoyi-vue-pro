<script setup lang="ts">
import type { HotZoneItemProperty } from '../../config';
import type { ControlDot } from './controller';

import type { AppLink } from '#/views/mall/promotion/components/app-link-input/data';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Image } from 'ant-design-vue';

import { AppLinkSelectDialog } from '#/views/mall/promotion/components';

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

/** 定义属性 */
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

const [Modal, modalApi] = useVbenModal({
  showCancelButton: false,
  onConfirm() {
    const list = zoomOut(formData.value);
    emit('update:modelValue', list);
    modalApi.close();
  },
});

/** 打开弹窗 */
function open() {
  // 放大
  formData.value = zoomIn(props.modelValue);
  modalApi.open();
}

defineExpose({ open }); // 提供 open 方法，用于打开弹窗

const container = ref<HTMLDivElement>(); // 热区容器

/** 增加热区 */
function handleAdd() {
  formData.value.push({
    width: HOT_ZONE_MIN_SIZE,
    height: HOT_ZONE_MIN_SIZE,
    top: 0,
    left: 0,
  } as HotZoneItemProperty);
}

/** 删除热区 */
function handleRemove(hotZone: HotZoneItemProperty) {
  formData.value = formData.value.filter((item) => item !== hotZone);
}

/** 移动热区 */
function handleMove(item: HotZoneItemProperty, e: MouseEvent) {
  useDraggable(item, e, (left, top, _, __, moveWidth, moveHeight) => {
    setLeft(item, left + moveWidth);
    setTop(item, top + moveHeight);
  });
}

/** 调整热区大小、位置 */
function handleResize(
  item: HotZoneItemProperty,
  ctrlDot: ControlDot,
  e: MouseEvent,
) {
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
}

/** 设置 X 轴坐标 */
function setLeft(item: HotZoneItemProperty, left: number) {
  // 不能超出容器
  if (left >= 0 && left <= container.value!.offsetWidth - item.width) {
    item.left = left;
  }
}

/** 设置Y轴坐标 */
function setTop(item: HotZoneItemProperty, top: number) {
  // 不能超出容器
  if (top >= 0 && top <= container.value!.offsetHeight - item.height) {
    item.top = top;
  }
}

/** 设置宽度 */
const setWidth = (item: HotZoneItemProperty, width: number) => {
  // 不能小于最小宽度 && 不能超出容器右边
  if (
    width >= HOT_ZONE_MIN_SIZE &&
    item.left + width <= container.value!.offsetWidth
  ) {
    item.width = width;
  }
};

/** 设置高度 */
const setHeight = (item: HotZoneItemProperty, height: number) => {
  // 不能小于最小高度 && 不能超出容器底部
  if (
    height >= HOT_ZONE_MIN_SIZE &&
    item.top + height <= container.value!.offsetHeight
  ) {
    item.height = height;
  }
};

const activeHotZone = ref<HotZoneItemProperty>();
const appLinkDialogRef = ref();

/** 显示 App 链接选择对话框 */
const handleShowAppLinkDialog = (hotZone: HotZoneItemProperty) => {
  activeHotZone.value = hotZone;
  appLinkDialogRef.value.open(hotZone.url);
};

/** 处理 App 链接选择变更 */
const handleAppLinkChange = (appLink: AppLink) => {
  if (!appLink || !activeHotZone.value) {
    return;
  }
  activeHotZone.value.name = appLink.name;
  activeHotZone.value.url = appLink.path;
};
</script>

<template>
  <Modal title="设置热区" class="w-[780px]">
    <div ref="container" class="w-750px relative h-full">
      <Image
        :src="imgUrl"
        :preview="false"
        class="w-750px pointer-events-none h-full select-none"
      />
      <div
        v-for="(item, hotZoneIndex) in formData"
        :key="hotZoneIndex"
        class="group absolute z-10 flex cursor-move items-center justify-center border text-base opacity-80"
        :style="{
          width: `${item.width}px`,
          height: `${item.height}px`,
          top: `${item.top}px`,
          left: `${item.left}px`,
          color: 'hsl(var(--primary))',
          background:
            'color-mix(in srgb, hsl(var(--primary)) 30%, transparent)',
          borderColor: 'hsl(var(--primary))',
        }"
        @mousedown="handleMove(item, $event)"
        @dblclick="handleShowAppLinkDialog(item)"
      >
        <span class="pointer-events-none select-none">
          {{ item.name || '双击选择链接' }}
        </span>
        <IconifyIcon
          icon="lucide:x"
          class="absolute right-0 top-0 hidden cursor-pointer rounded-bl-[80%] p-[2px_2px_6px_6px] text-right text-white group-hover:block"
          :style="{ backgroundColor: 'hsl(var(--primary))' }"
          :size="14"
          @click="handleRemove(item)"
        />

        <!-- 8 个控制点 -->
        <span
          class="ctrl-dot absolute z-[11] h-2 w-2 rounded-full bg-white"
          v-for="(dot, dotIndex) in CONTROL_DOT_LIST"
          :key="dotIndex"
          :style="{ ...dot.style, border: 'inherit' }"
          @mousedown="handleResize(item, dot, $event)"
        ></span>
      </div>
    </div>
    <template #prepend-footer>
      <Button @click="handleAdd" type="primary" ghost>
        <IconifyIcon icon="lucide:plus" class="mr-5px" />
        添加热区
      </Button>
    </template>
  </Modal>

  <AppLinkSelectDialog
    ref="appLinkDialogRef"
    @app-link-change="handleAppLinkChange"
  />
</template>
