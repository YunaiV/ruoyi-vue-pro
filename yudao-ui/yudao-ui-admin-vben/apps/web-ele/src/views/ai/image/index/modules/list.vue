<script setup lang="ts">
import type { AiImageApi } from '#/api/ai/image';

import { onMounted, onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

import { confirm, useVbenDrawer } from '@vben/common-ui';
import { AiImageStatusEnum } from '@vben/constants';
import { downloadFileFromImageUrl } from '@vben/utils';

import { useDebounceFn } from '@vueuse/core';
import { ElButton, ElCard, ElMessage, ElPagination } from 'element-plus';

import {
  deleteImageMy,
  getImageListMyByIds,
  getImagePageMy,
  midjourneyAction,
} from '#/api/ai/image';

import ImageCard from './card.vue';
import ImageDetail from './detail.vue';

const emits = defineEmits(['onRegeneration']);
const router = useRouter();
const [Drawer, drawerApi] = useVbenDrawer({
  title: '图片详情',
  footer: false,
});
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
}); // 图片分页相关的参数
const pageTotal = ref<number>(0); // page size
const imageList = ref<AiImageApi.Image[]>([]); // image 列表
const imageListRef = ref<any>(); // ref

const inProgressImageMap = ref<{}>({}); // 监听的 image 映射，一般是生成中（需要轮询），key 为 image 编号，value 为 image
const inProgressTimer = ref<any>(); // 生成中的 image 定时器，轮询生成进展
const showImageDetailId = ref<number>(0); // 图片详情的图片编号

/** 处理查看绘图作品 */
function handleViewPublic() {
  router.push({
    name: 'AiImageSquare',
  });
}

/** 查看图片的详情  */
async function handleDetailOpen() {
  drawerApi.open();
}
/** 获得 image 图片列表 */
async function getImageList() {
  const loading = ElMessage({
    message: `加载中...`,
    type: 'info',
    duration: 0,
  });
  try {
    // 1. 加载图片列表
    const { list, total } = await getImagePageMy(queryParams);
    imageList.value = list;
    pageTotal.value = total;

    // 2. 计算需要轮询的图片
    const newWatImages: any = {};
    imageList.value.forEach((item: any) => {
      if (item.status === AiImageStatusEnum.IN_PROGRESS) {
        newWatImages[item.id] = item;
      }
    });
    inProgressImageMap.value = newWatImages;
  } finally {
    // 关闭正在"加载中"的 Loading
    loading.close();
  }
}

const debounceGetImageList = useDebounceFn(getImageList, 80);
/** 轮询生成中的 image 列表 */
async function refreshWatchImages() {
  const imageIds = Object.keys(inProgressImageMap.value).map(Number);
  if (imageIds.length === 0) {
    return;
  }
  const list = (await getImageListMyByIds(imageIds)) as AiImageApi.Image[];
  const newWatchImages: any = {};
  list.forEach((image) => {
    if (image.status === AiImageStatusEnum.IN_PROGRESS) {
      newWatchImages[image.id] = image;
    } else {
      const index = imageList.value.findIndex(
        (oldImage) => image.id === oldImage.id,
      );
      if (index !== -1) {
        // 更新 imageList
        imageList.value[index] = image;
      }
    }
  });
  inProgressImageMap.value = newWatchImages;
}

/** 图片的点击事件 */
async function handleImageButtonClick(
  type: string,
  imageDetail: AiImageApi.Image,
) {
  // 详情
  if (type === 'more') {
    showImageDetailId.value = imageDetail.id;
    await handleDetailOpen();
    return;
  }
  // 删除
  if (type === 'delete') {
    await confirm(`是否删除照片?`);
    await deleteImageMy(imageDetail.id);
    await getImageList();
    ElMessage.success('删除成功!');
    return;
  }
  // 下载
  if (type === 'download') {
    await downloadFileFromImageUrl({
      fileName: imageDetail.model,
      source: imageDetail.picUrl,
    });
    return;
  }
  // 重新生成
  if (type === 'regeneration') {
    emits('onRegeneration', imageDetail);
  }
}

/** 处理 Midjourney 按钮点击事件  */
async function handleImageMidjourneyButtonClick(
  button: AiImageApi.ImageMidjourneyButtons,
  imageDetail: AiImageApi.Image,
) {
  // 1. 构建 params 参数
  const data = {
    id: imageDetail.id,
    customId: button.customId,
  } as AiImageApi.ImageMidjourneyAction;
  // 2. 发送 action
  await midjourneyAction(data);
  // 3. 刷新列表
  await getImageList();
}

defineExpose({ getImageList });

/** 组件挂在的时候 */
onMounted(async () => {
  // 获取 image 列表
  await getImageList();
  // 自动刷新 image 列表
  inProgressTimer.value = setInterval(async () => {
    await refreshWatchImages();
  }, 1000 * 3);
});

/** 组件取消挂在的时候 */
onUnmounted(async () => {
  if (inProgressTimer.value) {
    clearInterval(inProgressTimer.value);
  }
});
</script>

<template>
  <Drawer class="w-2/5">
    <ImageDetail :id="showImageDetailId" />
  </Drawer>
  <ElCard
    class="flex h-full w-full flex-col"
    :body-style="{
      margin: 0,
      padding: 0,
      height: '100%',
      position: 'relative',
      display: 'flex',
      flexDirection: 'column',
    }"
  >
    <template #header>
      绘画任务
      <ElButton @click="handleViewPublic">绘画作品</ElButton>
    </template>

    <div
      class="flex flex-1 flex-wrap content-start overflow-y-auto p-3 pb-28 pt-5"
      ref="imageListRef"
    >
      <ImageCard
        v-for="image in imageList"
        :key="image.id"
        :detail="image"
        @on-btn-click="handleImageButtonClick"
        @on-mj-btn-click="handleImageMidjourneyButtonClick"
        class="mb-3 mr-3"
      />
    </div>

    <div
      class="sticky bottom-0 z-50 flex h-16 items-center justify-center bg-card shadow-sm"
    >
      <ElPagination
        :total="pageTotal"
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 30, 40, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="debounceGetImageList"
        @current-change="debounceGetImageList"
      />
    </div>
  </ElCard>
</template>
