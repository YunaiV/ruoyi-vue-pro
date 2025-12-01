<script lang="ts" setup>
import type { DiyComponent, DiyComponentLibrary, PageConfig } from './util';

import { onMounted, ref, unref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { cloneDeep, isEmpty, isString } from '@vben/utils';

import { useQRCode } from '@vueuse/integrations/useQRCode';
import {
  ElAside,
  ElCard,
  ElContainer,
  ElHeader,
  ElImage,
  ElScrollbar,
  ElTag,
  ElText,
  ElTooltip,
} from 'element-plus';
import draggable from 'vuedraggable';

import statusBarImg from '#/assets/imgs/diy/statusBar.png';

import ComponentContainer from './components/component-container.vue';
import ComponentLibrary from './components/component-library.vue';
import { componentConfigs, components } from './components/mobile';
import { component as NAVIGATION_BAR_COMPONENT } from './components/mobile/navigation-bar/config';
import { component as PAGE_CONFIG_COMPONENT } from './components/mobile/page-config/config';
import { component as TAB_BAR_COMPONENT } from './components/mobile/tab-bar/config';

/** 页面装修详情页 */
defineOptions({
  name: 'DiyPageDetail',
  components,
});

/** 定义属性 */
const props = defineProps({
  modelValue: { type: [String, Object], required: true }, // 页面配置，支持 Json 字符串
  title: { type: String, default: '' }, // 标题
  libs: { type: Array<DiyComponentLibrary>, default: () => [] }, // 组件库
  showNavigationBar: { type: Boolean, default: true }, // 是否显示顶部导航栏
  showTabBar: { type: Boolean, default: false }, // 是否显示底部导航菜单
  showPageConfig: { type: Boolean, default: true }, // 是否显示页面配置
  previewUrl: { type: String, default: '' }, // 预览地址：提供了预览地址，才会显示预览按钮
});

const emits = defineEmits(['reset', 'save', 'update:modelValue']); // 工具栏操作

const qrcode = useQRCode(props.previewUrl, {
  errorCorrectionLevel: 'H',
  margin: 4,
}); // 预览二维码

const componentLibrary = ref(); // 左侧组件库
const pageConfigComponent = ref<DiyComponent<any>>(
  cloneDeep(PAGE_CONFIG_COMPONENT),
); // 页面设置组件
const navigationBarComponent = ref<DiyComponent<any>>(
  cloneDeep(NAVIGATION_BAR_COMPONENT),
); // 顶部导航栏
const tabBarComponent = ref<DiyComponent<any>>(cloneDeep(TAB_BAR_COMPONENT)); // 底部导航菜单

const selectedComponent = ref<DiyComponent<any>>(); // 选中的组件，默认选中顶部导航栏
const selectedComponentIndex = ref<number>(-1); // 选中的组件索引
const pageComponents = ref<DiyComponent<any>[]>([]); // 组件列表

/**
 * 监听传入的页面配置
 * 解析出 pageConfigComponent 页面整体的配置，navigationBarComponent、pageComponents、tabBarComponent 页面上、中、下的配置
 */
watch(
  () => props.modelValue,
  () => {
    const modelValue =
      isString(props.modelValue) && !isEmpty(props.modelValue)
        ? (JSON.parse(props.modelValue) as PageConfig)
        : props.modelValue;
    // noinspection SuspiciousTypeOfGuard
    pageConfigComponent.value.property =
      (typeof modelValue !== 'string' && modelValue?.page) ||
      PAGE_CONFIG_COMPONENT.property;
    // noinspection SuspiciousTypeOfGuard
    navigationBarComponent.value.property =
      (typeof modelValue !== 'string' && modelValue?.navigationBar) ||
      NAVIGATION_BAR_COMPONENT.property;
    // noinspection SuspiciousTypeOfGuard
    tabBarComponent.value.property =
      (typeof modelValue !== 'string' && modelValue?.tabBar) ||
      TAB_BAR_COMPONENT.property;
    // 查找对应的页面组件
    // noinspection SuspiciousTypeOfGuard
    pageComponents.value = (
      (typeof modelValue !== 'string' && modelValue?.components) ||
      []
    ).map((item: any) => {
      const component = componentConfigs[item.id];
      return { ...component, property: item.property };
    });
  },
  {
    immediate: true,
  },
);

/** 选择组件修改其属性后更新它的配置 */
watch(
  selectedComponent,
  (val: any) => {
    if (!val || selectedComponentIndex.value === -1) {
      return;
    }
    // 如果是基础设置页，默认选中的索引改成 -1，为了防止删除组件后切换到此页导致报错
    // https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/792
    if (props.showTabBar) {
      selectedComponentIndex.value = -1;
    }
    pageComponents.value[selectedComponentIndex.value] =
      selectedComponent.value!;
  },
  { deep: true },
);

/** 保存 */
function handleSave() {
  // 发送保存通知，由外部保存
  emits('save');
}

/** 监听配置修改 */
function pageConfigChange() {
  const pageConfig = {
    page: pageConfigComponent.value.property,
    navigationBar: navigationBarComponent.value.property,
    tabBar: tabBarComponent.value.property,
    components: pageComponents.value.map((component) => {
      // 只保留 APP 有用的字段
      return { id: component.id, property: component.property };
    }),
  } as PageConfig;
  if (!props.showTabBar) {
    delete pageConfig.tabBar;
  }
  // 发送数据更新通知
  const modelValue = isString(props.modelValue)
    ? JSON.stringify(pageConfig)
    : pageConfig;
  emits('update:modelValue', modelValue);
}

watch(
  () => [
    pageConfigComponent.value.property,
    navigationBarComponent.value.property,
    tabBarComponent.value.property,
    pageComponents.value,
  ],
  () => {
    pageConfigChange();
  },
  { deep: true },
);

/** 处理页面选中：显示属性表单 */
function handlePageSelected(event: any) {
  if (!props.showPageConfig) {
    return;
  }
  // 配置了样式 page-prop-area 的元素，才显示页面设置
  if (event?.target?.classList?.contains('page-prop-area')) {
    handleComponentSelected(unref(pageConfigComponent));
  }
}

/**
 * 选中组件
 *
 * @param component 组件
 * @param index 组件的索引
 */
function handleComponentSelected(
  component: DiyComponent<any>,
  index: number = -1,
) {
  selectedComponent.value = component;
  selectedComponentIndex.value = index;
}

/** 选中顶部导航栏 */
function handleNavigationBarSelected() {
  handleComponentSelected(unref(navigationBarComponent));
}

/** 选中底部导航菜单 */
function handleTabBarSelected() {
  handleComponentSelected(unref(tabBarComponent));
}

/** 组件变动（拖拽） */
function handleComponentChange(dragEvent: any) {
  // 新增，即从组件库拖拽添加组件
  if (dragEvent.added) {
    const { element, newIndex } = dragEvent.added;
    handleComponentSelected(element, newIndex);
  } else if (dragEvent.moved) {
    // 拖拽排序
    const { newIndex } = dragEvent.moved;
    // 保持选中
    selectedComponentIndex.value = newIndex;
  }
}

/** 交换组件 */
function swapComponent(oldIndex: number, newIndex: number) {
  const temp = pageComponents.value[oldIndex]!;
  pageComponents.value[oldIndex] = pageComponents.value[newIndex]!;
  pageComponents.value[newIndex] = temp;
  // 保持选中
  selectedComponentIndex.value = newIndex;
}

/** 移动组件（上移、下移） */
function handleMoveComponent(index: number, direction: number) {
  const newIndex = index + direction;
  if (newIndex < 0 || newIndex >= pageComponents.value.length) {
    return;
  }
  swapComponent(index, newIndex);
}

/** 复制组件 */
function handleCopyComponent(index: number) {
  const component = pageComponents.value[index];
  if (component) {
    const clonedComponent = cloneDeep(component);
    clonedComponent.uid = Date.now();
    pageComponents.value.splice(index + 1, 0, clonedComponent);
  }
}

/** 删除组件 */
function handleDeleteComponent(index: number) {
  pageComponents.value.splice(index, 1);
  if (index < pageComponents.value.length) {
    // 1. 不是最后一个组件时，删除后选中下面的组件
    const bottomIndex = index;
    const component = pageComponents.value[bottomIndex];
    if (component) {
      handleComponentSelected(component, bottomIndex);
    }
  } else if (pageComponents.value.length > 0) {
    // 2. 不是第一个组件时，删除后选中上面的组件
    const topIndex = index - 1;
    const component = pageComponents.value[topIndex];
    if (component) {
      handleComponentSelected(component, topIndex);
    }
  } else {
    // 3. 组件全部删除之后，显示页面设置
    handleComponentSelected(unref(pageConfigComponent));
  }
}

/** 重置 */
function handleReset() {
  emits('reset');
}

const [PreviewModal, previewModalApi] = useVbenModal({
  showConfirmButton: false,
  showCancelButton: false,
  onCancel() {
    previewModalApi.close();
  },
});

/** 预览 */
function handlePreview() {
  previewModalApi.open();
}

/** 设置默认选中的组件 */
function setDefaultSelectedComponent() {
  if (props.showPageConfig) {
    selectedComponent.value = unref(pageConfigComponent);
  } else if (props.showNavigationBar) {
    selectedComponent.value = unref(navigationBarComponent);
  } else if (props.showTabBar) {
    selectedComponent.value = unref(tabBarComponent);
  }
}

watch(
  () => [props.showPageConfig, props.showNavigationBar, props.showTabBar],
  () => setDefaultSelectedComponent(),
);

/** 初始化 */
onMounted(() => {
  setDefaultSelectedComponent();
});
</script>
<template>
  <div>
    <ElContainer class="editor flex h-full flex-col">
      <!-- 顶部：工具栏 -->
      <ElHeader
        class="editor-header flex !h-[42px] items-center justify-between !border-b !p-0"
        :style="{
          backgroundColor: 'var(--el-bg-color)',
          borderBottomColor: 'var(--el-border-color)',
        }"
      >
        <!-- 左侧操作区 -->
        <slot name="toolBarLeft"></slot>
        <!-- 中心操作区 -->
        <div class="header-center flex flex-1 items-center justify-center">
          <span>{{ title }}</span>
        </div>
        <!-- 右侧操作区 -->
        <div>
          <ElTooltip content="重置">
            <ElButton class="!m-0 !border-l !border-r-0" @click="handleReset">
              <IconifyIcon :size="24" icon="system-uicons:reset-alt" />
            </ElButton>
          </ElTooltip>
          <ElTooltip v-if="previewUrl" content="预览">
            <ElButton class="!m-0 !border-l !border-r-0" @click="handlePreview">
              <IconifyIcon :size="24" icon="ep:view" />
            </ElButton>
          </ElTooltip>
          <ElTooltip content="保存">
            <ElButton class="!m-0 !border-l !border-r-0" @click="handleSave">
              <IconifyIcon :size="24" icon="ep:check" />
            </ElButton>
          </ElTooltip>
        </div>
      </ElHeader>

      <!-- 中心区域 -->
      <ElContainer class="editor-container h-[calc(100vh-135px)]">
        <!-- 左侧：组件库（ComponentLibrary） -->
        <ElAside width="261px" class="editor-left">
          <ComponentLibrary
            v-if="libs && libs.length > 0"
            ref="componentLibrary"
            :list="libs"
          />
        </ElAside>
        <!-- 中心：设计区域（ComponentContainer） -->
        <div
          class="editor-center page-prop-area relative mt-4 flex w-full flex-1 flex-col justify-center overflow-hidden"
          :style="{ backgroundColor: 'var(--app-content-bg-color)' }"
          @click="handlePageSelected"
        >
          <!-- 手机顶部 -->
          <div class="editor-design-top mx-auto flex w-[375px] flex-col">
            <!-- 手机顶部状态栏 -->
            <img alt="" class="h-5 w-[375px] bg-white" :src="statusBarImg" />
            <!-- 手机顶部导航栏 -->
            <ComponentContainer
              v-if="showNavigationBar"
              :active="selectedComponent?.id === navigationBarComponent.id"
              :component="navigationBarComponent"
              :show-toolbar="false"
              class="cursor-pointer"
              @click="handleNavigationBarSelected"
            />
          </div>
          <!-- 绝对定位的组件：例如 弹窗、浮动按钮等 -->
          <div
            v-for="(component, index) in pageComponents"
            :key="index"
            @click="handleComponentSelected(component, index)"
          >
            <component
              :is="component.id"
              v-if="
                component.position === 'fixed' &&
                selectedComponent?.uid === component.uid
              "
              :property="component.property"
            />
          </div>
          <!-- 手机页面编辑区域 -->
          <ElScrollbar
            :view-style="{
              backgroundColor: pageConfigComponent.property.backgroundColor,
              backgroundImage: `url(${pageConfigComponent.property.backgroundImage})`,
            }"
            height="100%"
            view-class="phone-container"
            wrap-class="editor-design-center page-prop-area"
          >
            <draggable
              v-model="pageComponents"
              :animation="200"
              :force-fallback="false"
              class="page-prop-area drag-area"
              filter=".component-toolbar"
              ghost-class="draggable-ghost"
              group="component"
              item-key="index"
              @change="handleComponentChange"
            >
              <template #item="{ element, index }">
                <ComponentContainer
                  v-if="!element.position || element.position === 'center'"
                  :active="selectedComponentIndex === index"
                  :can-move-down="index < pageComponents.length - 1"
                  :can-move-up="index > 0"
                  :component="element"
                  @click="handleComponentSelected(element, index)"
                  @copy="handleCopyComponent(index)"
                  @delete="handleDeleteComponent(index)"
                  @move="
                    (direction: number) => handleMoveComponent(index, direction)
                  "
                />
              </template>
            </draggable>
          </ElScrollbar>
          <!-- 手机底部导航 -->
          <div
            v-if="showTabBar"
            class="editor-design-bottom component mx-auto w-[375px] cursor-pointer"
          >
            <ComponentContainer
              :active="selectedComponent?.id === tabBarComponent.id"
              :component="tabBarComponent"
              :show-toolbar="false"
              @click="handleTabBarSelected"
            />
          </div>
          <!-- 固定布局的组件 操作按钮区 -->
          <div
            class="fixed-component-action-group absolute right-4 top-0 flex flex-col gap-2"
          >
            <ElTag
              v-if="showPageConfig"
              :effect="
                selectedComponent?.uid === pageConfigComponent.uid
                  ? 'dark'
                  : 'plain'
              "
              :type="
                selectedComponent?.uid === pageConfigComponent.uid
                  ? 'primary'
                  : 'info'
              "
              size="large"
              @click="handleComponentSelected(pageConfigComponent)"
            >
              <IconifyIcon :icon="pageConfigComponent.icon" :size="12" />
              <span>{{ pageConfigComponent.name }}</span>
            </ElTag>
            <template v-for="(component, index) in pageComponents" :key="index">
              <ElTag
                v-if="component.position === 'fixed'"
                :effect="
                  selectedComponent?.uid === component.uid ? 'dark' : 'plain'
                "
                :type="
                  selectedComponent?.uid === component.uid ? 'primary' : 'info'
                "
                closable
                size="large"
                @click="handleComponentSelected(component)"
                @close="handleDeleteComponent(index)"
              >
                <IconifyIcon :icon="component.icon" :size="12" />
                <span>{{ component.name }}</span>
              </ElTag>
            </template>
          </div>
        </div>
        <!-- 右侧：属性面板（ComponentContainerProperty） -->
        <ElAside
          v-if="selectedComponent?.property"
          class="editor-right shrink-0 overflow-hidden shadow-[-8px_0_8px_-8px_rgb(0_0_0/0.12)]"
          width="350px"
        >
          <ElCard
            body-class="h-[calc(100%-var(--el-card-padding)-var(--el-card-padding))]"
            class="h-full"
            shadow="never"
          >
            <!-- 组件名称 -->
            <template #header>
              <div class="flex items-center gap-2">
                <IconifyIcon :icon="selectedComponent?.icon" color="gray" />
                <ElText>{{ selectedComponent?.name }}</ElText>
              </div>
            </template>
            <ElScrollbar
              class="m-[calc(0px-var(--el-card-padding))]"
              view-class="p-[var(--el-card-padding)] pb-[calc(var(--el-card-padding)+var(--el-card-padding))] property"
            >
              <component
                :is="`${selectedComponent?.id}Property`"
                :key="selectedComponent?.uid || selectedComponent?.id"
                v-model="selectedComponent.property"
              />
            </ElScrollbar>
          </ElCard>
        </ElAside>
      </ElContainer>
    </ElContainer>

    <!-- 预览弹框 -->
    <PreviewModal title="预览" class="w-700px">
      <div class="flex justify-around">
        <iframe
          :src="previewUrl"
          class="h-[667px] w-[375px] rounded-lg border-4 border-solid p-0.5"
        ></iframe>
        <div class="flex flex-col">
          <ElText>手机扫码预览</ElText>
          <ElImage :src="qrcode" alt="qrcode" />
        </div>
      </div>
    </PreviewModal>
  </div>
</template>
<style lang="scss" scoped>
/* 手机宽度 */
$phone-width: 375px;

.editor {
  .editor-header {
    /* 隐藏工具栏按钮的边框 */
    :deep(.el-radio-button__inner),
    :deep(.el-button) {
      border-top: none !important;
      border-bottom: none !important;
      border-radius: 0 !important;
    }
  }

  /* 中心操作区 */
  .editor-container {
    /* 右侧属性面板 */
    :deep(.editor-right) {
      /* 属性面板顶部：减少内边距 */
      :deep(.el-card__header) {
        padding: 8px 16px;
      }

      /* 属性面板分组 */
      .property-group {
        margin: 0 -20px;

        &.el-card {
          border: none;
        }

        /* 属性分组名称 */
        .el-card__header {
          padding: 8px 32px;
          background: var(--el-bg-color-page);
          border: none;
        }

        .el-card__body {
          border: none;
        }
      }
    }

    /* 中心区域 */
    .editor-center {
      /* 手机页面编辑区域 */
      :deep(.editor-design-center) {
        width: 100%;

        /* 主体内容 */
        .phone-container {
          position: relative;
          width: $phone-width;
          height: 100%;
          margin: 0 auto;
          background-repeat: no-repeat;
          background-size: 100% 100%;

          .drag-area {
            width: 100%;
            height: 100%;
          }
        }
      }

      /* 固定布局的组件 操作按钮区 */
      .fixed-component-action-group {
        :deep(.el-tag) {
          border: none;
          box-shadow: 0 2px 8px 0 rgb(0 0 0 / 10%);

          .el-tag__content {
            display: flex;
            align-items: center;
            justify-content: flex-start;
            width: 100%;

            .el-icon {
              margin-right: 4px;
            }
          }
        }
      }
    }
  }
}
</style>
