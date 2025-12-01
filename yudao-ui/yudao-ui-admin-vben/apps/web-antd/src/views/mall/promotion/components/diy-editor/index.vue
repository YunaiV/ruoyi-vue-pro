<script lang="ts" setup>
import type { DiyComponent, DiyComponentLibrary, PageConfig } from './util';

import { onMounted, ref, unref, watch } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { cloneDeep, isEmpty, isString } from '@vben/utils';

import { Button, Card, Col, QRCode, Row, Tag, Tooltip } from 'ant-design-vue';
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

// TODO @xingyu：要不要加这个？
// const qrcode = useQRCode(props.previewUrl, {
//   errorCorrectionLevel: 'H',
//   margin: 4,
// }); // 预览二维码

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
  // 使用深拷贝避免响应式追踪循环警告
  // TODO @xingyu：这个是必须的么？ele 没有哈。
  selectedComponent.value = cloneDeep(component);
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
  <Page auto-content-height>
    <!-- 顶部：工具栏 -->
    <Row class="flex max-h-12 rounded-lg bg-card">
      <!-- 左侧操作区 -->
      <Col :span="8">
        <slot name="toolBarLeft"></slot>
      </Col>
      <!-- 中心操作区 -->
      <Col :span="8">
        <span class="flex h-full items-center justify-center">{{ title }}</span>
      </Col>
      <!-- 右侧操作区 -->
      <Col :span="8">
        <Button.Group
          direction="vertical"
          size="large"
          class="flex justify-end"
        >
          <Tooltip title="重置">
            <Button @click="handleReset">
              <IconifyIcon class="size-5" icon="lucide:refresh-cw" />
            </Button>
          </Tooltip>
          <Tooltip v-if="previewUrl" title="预览">
            <Button @click="handlePreview">
              <IconifyIcon class="size-5" icon="lucide:eye" />
            </Button>
          </Tooltip>
          <Tooltip title="保存">
            <Button @click="handleSave">
              <IconifyIcon class="size-5" icon="lucide:check" />
            </Button>
          </Tooltip>
        </Button.Group>
      </Col>
    </Row>
    <!-- 中心区域 -->
    <Row class="mt-4 h-[calc(80vh)]">
      <!-- 左侧：组件库（ComponentLibrary） -->
      <Col :span="6">
        <ComponentLibrary
          v-if="libs && libs.length > 0"
          ref="componentLibrary"
          :list="libs"
        />
      </Col>
      <!-- 中心：设计区域（ComponentContainer） -->
      <Col :span="12">
        <div
          class="relative flex max-h-[calc(80vh)] w-full flex-1 flex-col justify-center overflow-y-auto"
          @click="handlePageSelected"
        >
          <!-- 手机顶部 -->
          <div class="mx-auto flex w-96 flex-col">
            <!-- 手机顶部状态栏 -->
            <img alt="" class="h-6 bg-card" :src="statusBarImg" />
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
          <div
            class="min-h-full w-full"
            :style="{
              // backgroundColor: pageConfigComponent.property.backgroundColor,
              backgroundImage: `url(${pageConfigComponent.property.backgroundImage})`,
            }"
          >
            <div
              class="relative mx-auto my-0 min-h-full w-96 items-center justify-center bg-auto bg-no-repeat"
            >
              <draggable
                v-model="pageComponents"
                :animation="200"
                :force-fallback="false"
                class="min-h-full w-full"
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
                      (direction: number) =>
                        handleMoveComponent(index, direction)
                    "
                  />
                </template>
              </draggable>
            </div>
          </div>
          <!-- 手机底部导航 -->
          <div
            v-if="showTabBar"
            class="bottom-2 mx-auto mb-2 w-96 cursor-pointer"
          >
            <ComponentContainer
              :active="selectedComponent?.id === tabBarComponent.id"
              :component="tabBarComponent"
              :show-toolbar="false"
              @click="handleTabBarSelected"
            />
          </div>
          <!-- 固定布局的组件 操作按钮区 -->
          <div class="absolute right-4 top-0 flex flex-col gap-2">
            <Tag
              v-if="showPageConfig"
              :color="
                selectedComponent?.uid === pageConfigComponent.uid
                  ? 'blue'
                  : 'default'
              "
              class="cursor-pointer"
              size="large"
              @click="handleComponentSelected(pageConfigComponent)"
            >
              <IconifyIcon
                :icon="pageConfigComponent.icon"
                class="mr-2 size-4"
              />
              <span>{{ pageConfigComponent.name }}</span>
            </Tag>
            <template v-for="(component, index) in pageComponents" :key="index">
              <Tag
                v-if="component.position === 'fixed'"
                :color="
                  selectedComponent?.uid === component.uid ? 'blue' : 'default'
                "
                closable
                class="cursor-pointer"
                size="large"
                @click="handleComponentSelected(component)"
                @close="handleDeleteComponent(index)"
              >
                <IconifyIcon :icon="component.icon" class="size-4" />
                <span>{{ component.name }}</span>
              </Tag>
            </template>
          </div>
        </div>
      </Col>
      <!-- 右侧：属性面板（ComponentContainerProperty） -->
      <Col :span="6" v-if="selectedComponent?.property">
        <Card
          class="h-[calc(80vh)] px-2 py-4"
          :body-style="{ padding: 0 }"
          :head-style="{ padding: 0, minHeight: '40px' }"
        >
          <!-- 组件名称 -->
          <template #title>
            <div class="flex h-8 items-center gap-1">
              <IconifyIcon :icon="selectedComponent?.icon" color="gray" />
              <span>{{ selectedComponent?.name }}</span>
            </div>
          </template>
          <div
            class="property mt-0 max-h-[calc(80vh-100px)] overflow-y-auto p-4"
          >
            <component
              :is="`${selectedComponent?.id}Property`"
              :key="selectedComponent?.uid || selectedComponent?.id"
              v-model="selectedComponent.property"
            />
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 预览弹框 -->
    <PreviewModal title="预览" class="w-[700px]">
      <div class="flex justify-around">
        <iframe
          :src="previewUrl"
          class="h-[667px] w-96 rounded-lg border-4 border-solid p-0.5"
        ></iframe>
        <div class="flex flex-col">
          <div class="text-base">手机扫码预览</div>
          <QRCode :value="previewUrl" error-level="H" />
        </div>
      </div>
    </PreviewModal>
  </Page>
  <!-- TODO @xingyu：这里改造完后，类似 web-ele/src/views/mall/promotion/components/diy-editor/index.vue 里的全局样式（递推到子组件）里的就没没了，类似 property-group -->
</template>
