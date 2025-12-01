<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpMaterialApi } from '#/api/mp/material';

import { reactive, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';
import { NewsType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { Button, Pagination, Row, Spin } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDraftPage } from '#/api/mp/draft';
import { getFreePublishPage } from '#/api/mp/freePublish';
import { getMaterialPage } from '#/api/mp/material';
import { WxNews, WxVideoPlayer, WxVoicePlayer } from '#/views/mp/components';

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

// TODO @dylan：可以把【点击上传】3 个 tab 的按钮，放到右侧的 toolbar 一起，和刷新按钮放在一行；
const voiceGridColumns: VxeTableGridOptions<MpMaterialApi.Material>['columns'] =
  [
    {
      field: 'mediaId',
      title: '编号',
      align: 'center',
      minWidth: 160,
    },
    {
      field: 'name',
      title: '文件名',
      minWidth: 200,
    },
    {
      field: 'voice',
      title: '语音',
      minWidth: 200,
      align: 'center',
      slots: { default: 'voice' },
    },
    {
      field: 'createTime',
      title: '上传时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      align: 'center',
      slots: { default: 'actions' },
    },
  ];

const videoGridColumns: VxeTableGridOptions<MpMaterialApi.Material>['columns'] =
  [
    {
      field: 'mediaId',
      title: '编号',
      minWidth: 160,
    },
    {
      field: 'name',
      title: '文件名',
      minWidth: 200,
    },
    {
      field: 'title',
      title: '标题',
      minWidth: 200,
    },
    {
      field: 'introduction',
      title: '介绍',
      minWidth: 220,
    },
    {
      field: 'video',
      title: '视频',
      minWidth: 220,
      align: 'center',
      slots: { default: 'video' },
    },
    {
      field: 'createTime',
      title: '上传时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      align: 'center',
      slots: { default: 'actions' },
    },
  ];

const [VoiceGrid, voiceGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: voiceGridColumns,
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, { accountId }) => {
          const finalAccountId = accountId ?? queryParams.accountId;
          if (!finalAccountId) {
            return { list: [], total: 0 };
          }
          return await getMaterialPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            accountId: finalAccountId,
            type: 'voice',
          });
        },
      },
    },
    rowConfig: {
      keyField: 'mediaId',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
    },
  } as VxeTableGridOptions<MpMaterialApi.Material>,
});

const [VideoGrid, videoGridApi] = useVbenVxeGrid({
  gridOptions: {
    border: true,
    columns: videoGridColumns,
    height: 'auto',
    keepSource: true,
    pagerConfig: {
      enabled: true,
      pageSize: 10,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }, { accountId }) => {
          const finalAccountId = accountId ?? queryParams.accountId;
          if (finalAccountId === undefined || finalAccountId === null) {
            return { list: [], total: 0 };
          }
          return await getMaterialPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            accountId: finalAccountId,
            type: 'video',
          });
        },
      },
    },
    rowConfig: {
      keyField: 'mediaId',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
    },
  } as VxeTableGridOptions<MpMaterialApi.Material>,
});

function selectMaterialFun(item: any) {
  emit('selectMaterial', item);
}

async function getMaterialPageFun() {
  const data = await getMaterialPage({
    ...queryParams,
    type: props.type,
  });
  list.value = data.list;
  total.value = data.total;
}

async function getFreePublishPageFun() {
  const data = await getFreePublishPage(queryParams);
  data.list.forEach((item: any) => {
    const articles = item.content.newsItem;
    articles.forEach((article: any) => {
      article.picUrl = article.thumbUrl;
    });
  });
  list.value = data.list;
  total.value = data.total;
}

async function getDraftPageFun() {
  const data = await getDraftPage(queryParams);
  data.list.forEach((draft: any) => {
    const articles = draft.content.newsItem;
    articles.forEach((article: any) => {
      article.picUrl = article.thumbUrl;
    });
  });
  list.value = data.list;
  total.value = data.total;
}

async function getPage() {
  if (props.type === 'voice') {
    await voiceGridApi.reload({ accountId: queryParams.accountId });
    return;
  }
  if (props.type === 'video') {
    await videoGridApi.reload({ accountId: queryParams.accountId });
    return;
  }

  loading.value = true;
  try {
    if (props.type === 'news' && props.newsType === NewsType.Published) {
      await getFreePublishPageFun();
    } else if (props.type === 'news' && props.newsType === NewsType.Draft) {
      await getDraftPageFun();
    } else {
      await getMaterialPageFun();
    }
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.accountId,
  (accountId) => {
    queryParams.accountId = accountId;
    queryParams.pageNo = 1;
    getPage();
  },
  { immediate: true },
);

watch(
  () => props.type,
  () => {
    queryParams.pageNo = 1;
    getPage();
  },
);

watch(
  () => props.newsType,
  () => {
    if (props.type === 'news') {
      queryParams.pageNo = 1;
      getPage();
    }
  },
);
</script>

<template>
  <Page :bordered="false" class="pb-8">
    <!-- 类型：image -->
    <template v-if="props.type === 'image'">
      <Spin :spinning="loading">
        <div
          class="mx-auto w-full columns-1 [column-gap:10px] md:columns-2 lg:columns-3 xl:columns-4 2xl:columns-5"
        >
          <div
            v-for="item in list"
            :key="item.mediaId"
            class="mb-2.5 h-72 break-inside-avoid border border-[#eaeaea] p-2.5"
          >
            <img
              class="h-48 w-full object-contain"
              :src="item.url"
              alt="素材图片"
            />
            <p class="truncate text-center text-xs leading-[30px]">
              {{ item.name }}
            </p>
            <Row class="flex justify-center pt-2.5">
              <Button type="primary" @click="selectMaterialFun(item)">
                选择
                <template #icon>
                  <IconifyIcon icon="lucide:circle-check" />
                </template>
              </Button>
            </Row>
          </div>
        </div>
      </Spin>
      <Pagination
        v-model:current="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        class="mt-4"
        @change="getPage"
        @show-size-change="getPage"
      />
    </template>

    <!-- 类型：voice -->
    <template v-else-if="props.type === 'voice'">
      <VoiceGrid>
        <template #voice="{ row }">
          <WxVoicePlayer :url="row.url" />
        </template>
        <template #actions="{ row }">
          <Button type="link" @click="selectMaterialFun(row)">
            选择
            <template #icon>
              <IconifyIcon icon="lucide:plus" />
            </template>
          </Button>
        </template>
      </VoiceGrid>
    </template>

    <!-- 类型：video -->
    <template v-else-if="props.type === 'video'">
      <VideoGrid>
        <template #video="{ row }">
          <WxVideoPlayer :url="row.url" />
        </template>
        <template #actions="{ row }">
          <Button type="link" @click="selectMaterialFun(row)">
            选择
            <template #icon>
              <IconifyIcon icon="lucide:circle-plus" />
            </template>
          </Button>
        </template>
      </VideoGrid>
    </template>

    <!-- 类型：news -->
    <template v-else-if="props.type === 'news'">
      <Spin :spinning="loading">
        <div
          class="mx-auto w-full columns-1 [column-gap:10px] md:columns-2 lg:columns-3 xl:columns-4 2xl:columns-5"
        >
          <div
            v-for="item in list"
            :key="item.mediaId"
            class="mb-2.5 break-inside-avoid border border-[#eaeaea] p-2.5"
          >
            <div v-if="item.content && item.content.newsItem">
              <WxNews :articles="item.content.newsItem" />
              <Row class="flex justify-center pt-2.5">
                <Button type="primary" @click="selectMaterialFun(item)">
                  选择
                  <template #icon>
                    <IconifyIcon icon="lucide:circle-check" />
                  </template>
                </Button>
              </Row>
            </div>
          </div>
        </div>
      </Spin>
      <Pagination
        v-model:current="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        class="mt-4"
        @change="getPage"
        @show-size-change="getPage"
      />
    </template>
  </Page>
</template>
