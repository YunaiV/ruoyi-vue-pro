<script lang="ts" setup>
import type { AppLink } from './data';

import { nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { getUrlNumberValue } from '@vben/utils';

import { Button, Form, FormItem, Tooltip } from 'ant-design-vue';

import { ProductCategorySelect } from '#/views/mall/product/category/components/';

import { APP_LINK_GROUP_LIST, APP_LINK_TYPE_ENUM } from './data';

/** APP 链接选择弹框 */
defineOptions({ name: 'AppLinkSelectDialog' });

const emit = defineEmits<{
  appLinkChange: [appLink: AppLink];
  change: [link: string];
}>();

const activeGroup = ref(APP_LINK_GROUP_LIST[0]?.name); // 选中的分组，默认选中第一个
const activeAppLink = ref({} as AppLink); // 选中的 APP 链接

const linkScrollbar = ref<HTMLDivElement>(); // 右侧滚动条
const groupTitleRefs = ref<HTMLInputElement[]>([]); // 分组标题引用列表
const groupScrollbar = ref<HTMLDivElement>(); // 分组滚动条
const groupBtnRefs = ref<HTMLButtonElement[]>([]); // 分组引用列表

const detailSelectDialog = ref<{
  id?: number;
  type?: APP_LINK_TYPE_ENUM;
}>({
  id: undefined,
  type: undefined,
}); // 详情选择对话框

const [Modal, modalApi] = useVbenModal({
  onConfirm() {
    emit('change', activeAppLink.value.path);
    emit('appLinkChange', activeAppLink.value);
    modalApi.close();
  },
});

const [DetailSelectModal, detailSelectModalApi] = useVbenModal({
  onConfirm() {
    detailSelectModalApi.close();
  },
});

defineExpose({ open });

/** 打开弹窗 */
async function open(link: string) {
  activeAppLink.value.path = link;
  modalApi.open();
  // 滚动到当前的链接
  const group = APP_LINK_GROUP_LIST.find((group) =>
    group.links.some((linkItem) => {
      const sameLink = isSameLink(linkItem.path, link);
      if (sameLink) {
        activeAppLink.value = { ...linkItem, path: link };
      }
      return sameLink;
    }),
  );
  if (group) {
    // 使用 nextTick 的原因：可能 Dom 还没生成，导致滚动失败
    await nextTick();
    handleGroupSelected(group.name);
  }
}

/** 处理 APP 链接选中 */
function handleAppLinkSelected(appLink: AppLink) {
  if (!isSameLink(appLink.path, activeAppLink.value.path)) {
    activeAppLink.value = appLink;
  }
  switch (appLink.type) {
    case APP_LINK_TYPE_ENUM.PRODUCT_CATEGORY_LIST: {
      detailSelectDialog.value.type = appLink.type;
      // 返显
      detailSelectDialog.value.id =
        getUrlNumberValue(
          'id',
          `http://127.0.0.1${activeAppLink.value.path}`,
        ) || undefined;
      detailSelectModalApi.open();
      break;
    }
    default: {
      break;
    }
  }
}

/**
 * 处理右侧链接列表滚动
 *
 * @param {Event} event 滚动事件
 * @param {number} event.target.scrollTop 滚动条的位置
 */
function handleScroll(event: Event) {
  const scrollTop = (event.target as HTMLDivElement).scrollTop;
  const titleEl = groupTitleRefs.value.find((titleEl: HTMLInputElement) => {
    // 获取标题的位置信息
    const { offsetHeight, offsetTop } = titleEl;
    // 判断标题是否在可视范围内
    return scrollTop >= offsetTop && scrollTop < offsetTop + offsetHeight;
  });
  // 只需处理一次
  if (titleEl && activeGroup.value !== titleEl.textContent) {
    activeGroup.value = titleEl.textContent || '';
    // 同步左侧的滚动条位置
    scrollToGroupBtn(activeGroup.value);
  }
}

/** 处理分组选中 */
function handleGroupSelected(group: string) {
  activeGroup.value = group;
  const titleRef = groupTitleRefs.value.find(
    (item: HTMLInputElement) => item.textContent === group,
  );
  if (titleRef && linkScrollbar.value) {
    // 滚动分组标题
    linkScrollbar.value.scrollTop = titleRef.offsetTop;
  }
}

/** 自动滚动分组按钮，确保分组按钮保持在可视区域内 */
function scrollToGroupBtn(group: string) {
  const groupBtn = groupBtnRefs.value.find(
    (ref: HTMLButtonElement | undefined) => ref?.textContent === group,
  );
  if (groupBtn && groupScrollbar.value) {
    groupScrollbar.value.scrollTop = groupBtn.offsetTop;
  }
}

/** 是否为相同的链接（不比较参数，只比较链接） */
function isSameLink(link1: string, link2: string) {
  return link2 ? link1?.split('?')[0] === link2.split('?')[0] : false;
}

/** 处理详情选择 */
function handleProductCategorySelected(id: number) {
  // 生成 activeAppLink
  const url = new URL(activeAppLink.value.path, 'http://127.0.0.1');
  url.searchParams.set('id', `${id}`);
  activeAppLink.value.path = `${url.pathname}${url.search}`;

  // 关闭对话框，并重置 id
  detailSelectModalApi.close();
  detailSelectDialog.value.id = undefined;
}
</script>
<template>
  <Modal title="选择链接" class="w-[65%]">
    <div class="flex h-[500px] gap-2">
      <!-- 左侧分组列表 -->
      <div
        class="flex h-full flex-col overflow-y-auto border-r border-gray-200 pr-2"
        ref="groupScrollbar"
      >
        <Button
          v-for="(group, groupIndex) in APP_LINK_GROUP_LIST"
          :key="groupIndex"
          class="!ml-0 mb-1 mr-4 !justify-start"
          :class="[{ active: activeGroup === group.name }]"
          ref="groupBtnRefs"
          :type="activeGroup === group.name ? 'primary' : 'default'"
          @click="handleGroupSelected(group.name)"
        >
          {{ group.name }}
        </Button>
      </div>
      <!-- 右侧链接列表 -->
      <div
        class="h-full flex-1 overflow-y-auto pl-2"
        @scroll="handleScroll"
        ref="linkScrollbar"
      >
        <div
          v-for="(group, groupIndex) in APP_LINK_GROUP_LIST"
          :key="groupIndex"
          class="mb-4 border-b border-gray-100 pb-4 last:mb-0 last:border-b-0"
        >
          <!-- 分组标题 -->
          <div class="mb-2 font-bold" ref="groupTitleRefs">
            {{ group.name }}
          </div>
          <!-- 链接列表 -->
          <Tooltip
            v-for="(appLink, appLinkIndex) in group.links"
            :key="appLinkIndex"
            :title="appLink.path"
            placement="bottom"
            :mouse-enter-delay="0.3"
          >
            <Button
              class="mb-2 ml-0 mr-2"
              :type="
                isSameLink(appLink.path, activeAppLink.path)
                  ? 'primary'
                  : 'default'
              "
              @click="handleAppLinkSelected(appLink)"
            >
              {{ appLink.name }}
            </Button>
          </Tooltip>
        </div>
      </div>
    </div>
  </Modal>

  <DetailSelectModal title="选择分类" class="w-[65%]">
    <Form class="min-h-[200px]">
      <FormItem
        label="选择分类"
        v-if="
          detailSelectDialog.type === APP_LINK_TYPE_ENUM.PRODUCT_CATEGORY_LIST
        "
      >
        <ProductCategorySelect
          v-model="detailSelectDialog.id"
          :parent-id="0"
          @update:model-value="handleProductCategorySelected"
        />
      </FormItem>
    </Form>
  </DetailSelectModal>
</template>
