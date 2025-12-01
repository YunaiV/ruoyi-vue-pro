<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';

import { NewsType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { formatTime } from '@vben/utils';

import {
  ElButton,
  ElPagination,
  ElRow,
  ElTable,
  ElTableColumn,
} from 'element-plus';

import * as MpDraftApi from '#/api/mp/draft';
import * as MpFreePublishApi from '#/api/mp/freePublish';
import * as MpMaterialApi from '#/api/mp/material';
import News from '#/views/mp/components/wx-news/wx-news.vue';
import VideoPlayer from '#/views/mp/components/wx-video-play/wx-video-play.vue';
import VoicePlayer from '#/views/mp/components/wx-voice-play/wx-voice-play.vue';

// TODO @hw：代码风格，看看 antd 和 ele 是不是统一下； 等antd此组件修改完再调整

/** 微信素材选择 */
defineOptions({ name: 'WxMaterialSelect' });

const props = withDefaults(
  defineProps<{
    accountId: number;
    newsType?: NewsType;
    type: string;
  }>(),
  {
    newsType: NewsType.Published,
  },
);

const emit = defineEmits<{
  (e: 'selectMaterial', item: any): void;
}>();

const loading = ref(false); // 遮罩层
const total = ref(0); // 总条数
const list = ref<any[]>([]); // 数据列表
const queryParams = reactive({
  accountId: props.accountId,
  pageNo: 1,
  pageSize: 10,
}); // 查询参数

/** 选择素材 */
function selectMaterialFun(item: any) {
  emit('selectMaterial', item);
}

/** 获取分页数据 */
async function getPage() {
  loading.value = true;
  try {
    if (props.type === 'news' && props.newsType === NewsType.Published) {
      // 【图文】+ 【已发布】
      await getFreePublishPageFun();
    } else if (props.type === 'news' && props.newsType === NewsType.Draft) {
      // 【图文】+ 【草稿】
      await getDraftPageFun();
    } else {
      // 【素材】
      await getMaterialPageFun();
    }
  } finally {
    loading.value = false;
  }
}

/** 获取素材分页 */
async function getMaterialPageFun() {
  const data = await MpMaterialApi.getMaterialPage({
    ...queryParams,
    type: props.type,
  });
  list.value = data.list;
  total.value = data.total;
}

/** 获取已发布图文分页 */
async function getFreePublishPageFun() {
  const data = await MpFreePublishApi.getFreePublishPage(queryParams);
  data.list.forEach((item: any) => {
    const articles = item.content.newsItem;
    articles.forEach((article: any) => {
      article.picUrl = article.thumbUrl;
    });
  });
  list.value = data.list;
  total.value = data.total;
}

/** 获取草稿图文分页 */
async function getDraftPageFun() {
  const data = await MpDraftApi.getDraftPage(queryParams);
  data.list.forEach((draft: any) => {
    const articles = draft.content.newsItem;
    articles.forEach((article: any) => {
      article.picUrl = article.thumbUrl;
    });
  });
  list.value = data.list;
  total.value = data.total;
}

onMounted(async () => {
  getPage();
});
</script>

<template>
  <div class="pb-30px">
    <!-- 类型：image -->
    <div v-if="props.type === 'image'">
      <div
        class="mx-auto w-full columns-1 [column-gap:10px] md:columns-2 lg:columns-3 xl:columns-4 2xl:columns-5"
        v-loading="loading"
      >
        <div
          class="mb-2.5 break-inside-avoid border border-[#eaeaea] p-2.5"
          v-for="item in list"
          :key="item.mediaId"
        >
          <img class="w-full" :src="item.url" />
          <p class="truncate text-center text-xs leading-[30px]">
            {{ item.name }}
          </p>
          <ElRow class="flex justify-center pt-2.5">
            <ElButton type="success" @click="selectMaterialFun(item)">
              选择
              <IconifyIcon icon="lucide:circle-check" />
            </ElButton>
          </ElRow>
        </div>
      </div>
      <!-- 分页组件 -->
      <ElPagination
        background
        layout="prev, pager, next, sizes, total"
        :total="total"
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        @current-change="getMaterialPageFun"
        @size-change="getMaterialPageFun"
      />
    </div>
    <!-- 类型：voice -->
    <div v-else-if="props.type === 'voice'">
      <!-- 列表 -->
      <ElTable v-loading="loading" :data="list">
        <ElTableColumn label="编号" align="center" prop="mediaId" />
        <ElTableColumn label="文件名" align="center" prop="name" />
        <ElTableColumn label="语音" align="center">
          <template #default="scope">
            <VoicePlayer :url="scope.row.url" />
          </template>
        </ElTableColumn>
        <ElTableColumn
          label="上传时间"
          align="center"
          prop="createTime"
          width="180"
          :formatter="
            (row: any) => formatTime(row.createTime, 'YYYY-MM-DD HH:mm:ss')
          "
        />
        <ElTableColumn label="操作" align="center" fixed="right">
          <template #default="scope">
            <ElButton type="primary" link @click="selectMaterialFun(scope.row)">
              选择
              <IconifyIcon icon="lucide:plus" />
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <!-- 分页组件 -->
      <ElPagination
        background
        layout="prev, pager, next, sizes, total"
        :total="total"
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        @current-change="getPage"
        @size-change="getPage"
      />
    </div>
    <!-- 类型：video -->
    <div v-else-if="props.type === 'video'">
      <!-- 列表 -->
      <ElTable v-loading="loading" :data="list">
        <ElTableColumn label="编号" align="center" prop="mediaId" />
        <ElTableColumn label="文件名" align="center" prop="name" />
        <ElTableColumn label="标题" align="center" prop="title" />
        <ElTableColumn label="介绍" align="center" prop="introduction" />
        <ElTableColumn label="视频" align="center">
          <template #default="scope">
            <VideoPlayer :url="scope.row.url" />
          </template>
        </ElTableColumn>
        <ElTableColumn
          label="上传时间"
          align="center"
          prop="createTime"
          width="180"
          :formatter="
            (row: any) => formatTime(row.createTime, 'YYYY-MM-DD HH:mm:ss')
          "
        />
        <ElTableColumn
          label="操作"
          align="center"
          fixed="right"
          class-name="small-padding fixed-width"
        >
          <template #default="scope">
            <ElButton type="primary" link @click="selectMaterialFun(scope.row)">
              选择
              <IconifyIcon icon="lucide:circle-plus" />
            </ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <!-- 分页组件 -->
      <ElPagination
        background
        layout="prev, pager, next, sizes, total"
        :total="total"
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        @current-change="getMaterialPageFun"
        @size-change="getMaterialPageFun"
      />
    </div>
    <!-- 类型：news -->
    <div v-else-if="props.type === 'news'">
      <div
        class="mx-auto w-full columns-1 [column-gap:10px] md:columns-2 lg:columns-3 xl:columns-4 2xl:columns-5"
        v-loading="loading"
      >
        <div
          class="mb-2.5 break-inside-avoid border border-[#eaeaea] p-2.5"
          v-for="item in list"
          :key="item.mediaId"
        >
          <div v-if="item.content && item.content.newsItem">
            <News :articles="item.content.newsItem" />
            <ElRow class="flex justify-center pt-2.5">
              <ElButton type="success" @click="selectMaterialFun(item)">
                选择
                <IconifyIcon icon="lucide:circle-check" />
              </ElButton>
            </ElRow>
          </div>
        </div>
      </div>
      <!-- 分页组件 -->
      <ElPagination
        background
        layout="prev, pager, next, sizes, total"
        :total="total"
        v-model:current-page="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        @current-change="getMaterialPageFun"
        @size-change="getMaterialPageFun"
      />
    </div>
  </div>
</template>
