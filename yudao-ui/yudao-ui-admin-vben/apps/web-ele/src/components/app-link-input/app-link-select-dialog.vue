<script lang="ts" setup>
import type { ButtonInstance, ScrollbarInstance } from 'element-plus';

import type { AppLink } from './data';

import { nextTick, ref } from 'vue';

import { getUrlNumberValue } from '@vben/utils';

import { ElScrollbar } from 'element-plus';

import ProductCategorySelect from '#/views/mall/product/category/components/product-category-select.vue';

import { APP_LINK_GROUP_LIST, APP_LINK_TYPE_ENUM } from './data';

// APP 链接选择弹框
defineOptions({ name: 'AppLinkSelectDialog' });
// 处理绑定值更新
const emit = defineEmits<{
  appLinkChange: [appLink: AppLink];
  change: [link: string];
}>();
// 选中的分组，默认选中第一个
const activeGroup = ref(APP_LINK_GROUP_LIST[0]?.name);
// 选中的 APP 链接
const activeAppLink = ref({} as AppLink);

/** 打开弹窗 */
const dialogVisible = ref(false);
const open = (link: string) => {
  activeAppLink.value.path = link;
  dialogVisible.value = true;
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
    nextTick(() => handleGroupSelected(group.name));
  }
};
defineExpose({ open });

// 处理 APP 链接选中
const handleAppLinkSelected = (appLink: AppLink) => {
  if (!isSameLink(appLink.path, activeAppLink.value.path)) {
    activeAppLink.value = appLink;
  }
  switch (appLink.type) {
    case APP_LINK_TYPE_ENUM.PRODUCT_CATEGORY_LIST: {
      detailSelectDialog.value.visible = true;
      detailSelectDialog.value.type = appLink.type;
      // 返显
      detailSelectDialog.value.id =
        getUrlNumberValue(
          'id',
          `http://127.0.0.1${activeAppLink.value.path}`,
        ) || undefined;
      break;
    }
    default: {
      break;
    }
  }
};

const handleSubmit = () => {
  dialogVisible.value = false;
  emit('change', activeAppLink.value.path);
  emit('appLinkChange', activeAppLink.value);
};

// 分组标题引用列表
const groupTitleRefs = ref<HTMLInputElement[]>([]);
/**
 * 处理右侧链接列表滚动
 * @param {object} param0 滚动事件参数
 * @param {number} param0.scrollTop 滚动条的位置
 */
const handleScroll = ({ scrollTop }: { scrollTop: number }) => {
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
};

// 右侧滚动条
const linkScrollbar = ref<ScrollbarInstance>();
// 处理分组选中
const handleGroupSelected = (group: string) => {
  activeGroup.value = group;
  const titleRef = groupTitleRefs.value.find(
    (item: HTMLInputElement) => item.textContent === group,
  );
  if (titleRef) {
    // 滚动分组标题
    linkScrollbar.value?.setScrollTop(titleRef.offsetTop);
  }
};

// 分组滚动条
const groupScrollbar = ref<ScrollbarInstance>();
// 分组引用列表
const groupBtnRefs = ref<ButtonInstance[]>([]);
// 自动滚动分组按钮，确保分组按钮保持在可视区域内
const scrollToGroupBtn = (group: string) => {
  const groupBtn = groupBtnRefs.value
    .map((btn: ButtonInstance) => btn.ref)
    .find((ref: HTMLButtonElement | undefined) => ref?.textContent === group);
  if (groupBtn) {
    groupScrollbar.value?.setScrollTop(groupBtn.offsetTop);
  }
};

// 是否为相同的链接（不比较参数，只比较链接）
const isSameLink = (link1: string, link2: string) => {
  return link2 ? link1.split('?')[0] === link2.split('?')[0] : false;
};

// 详情选择对话框
const detailSelectDialog = ref<{
  id?: number;
  type?: APP_LINK_TYPE_ENUM;
  visible: boolean;
}>({
  visible: false,
  id: undefined,
  type: undefined,
});
// 处理详情选择
const handleProductCategorySelected = (id: number) => {
  const url = new URL(activeAppLink.value.path, 'http://127.0.0.1');
  // 修改 id 参数
  url.searchParams.set('id', `${id}`);
  // 排除域名
  activeAppLink.value.path = `${url.pathname}${url.search}`;
  // 关闭对话框
  detailSelectDialog.value.visible = false;
  // 重置 id
  detailSelectDialog.value.id = undefined;
};
</script>
<template>
  <el-dialog v-model="dialogVisible" title="选择链接" width="65%">
    <div class="flex h-[500px] gap-2">
      <!-- 左侧分组列表 -->
      <ElScrollbar
        wrap-class="h-full"
        ref="groupScrollbar"
        view-class="flex flex-col"
      >
        <el-button
          v-for="(group, groupIndex) in APP_LINK_GROUP_LIST"
          :key="groupIndex"
          class="ml-0 mr-4 w-[90px] justify-start"
          :class="[{ active: activeGroup === group.name }]"
          ref="groupBtnRefs"
          :text="activeGroup !== group.name"
          :type="activeGroup === group.name ? 'primary' : 'default'"
          @click="handleGroupSelected(group.name)"
        >
          {{ group.name }}
        </el-button>
      </ElScrollbar>
      <!-- 右侧链接列表 -->
      <ElScrollbar
        class="h-full flex-1"
        @scroll="handleScroll"
        ref="linkScrollbar"
      >
        <div
          v-for="(group, groupIndex) in APP_LINK_GROUP_LIST"
          :key="groupIndex"
        >
          <!-- 分组标题 -->
          <div class="font-bold" ref="groupTitleRefs">{{ group.name }}</div>
          <!-- 链接列表 -->
          <el-tooltip
            v-for="(appLink, appLinkIndex) in group.links"
            :key="appLinkIndex"
            :content="appLink.path"
            placement="bottom"
            :show-after="300"
          >
            <el-button
              class="mb-2 ml-0 mr-2"
              :type="
                isSameLink(appLink.path, activeAppLink.path)
                  ? 'primary'
                  : 'default'
              "
              @click="handleAppLinkSelected(appLink)"
            >
              {{ appLink.name }}
            </el-button>
          </el-tooltip>
        </div>
      </ElScrollbar>
    </div>
    <!-- 底部对话框操作按钮 -->
    <template #footer>
      <el-button type="primary" @click="handleSubmit">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="detailSelectDialog.visible" title="" width="50%">
    <el-form class="min-h-[200px]">
      <el-form-item
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
      </el-form-item>
    </el-form>
  </el-dialog>
</template>
<style lang="scss" scoped>
:deep(.el-button + .el-button) {
  margin-left: 0 !important;
}
</style>
