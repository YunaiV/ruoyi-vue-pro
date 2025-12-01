<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import {
  Button,
  Card,
  Col,
  Empty,
  Pagination,
  Popconfirm,
  Row,
  Tag,
  Tooltip,
} from 'ant-design-vue';

import { getProductPage } from '#/api/iot/product/product';

// TODO @haohao：应该是 card-view.vue；

// TODO @haohao：命名不太对；可以简化下；
defineOptions({ name: 'ProductCardView' });

const props = defineProps<Props>();

const emit = defineEmits<{
  create: [];
  delete: [row: any];
  detail: [productId: number];
  edit: [row: any];
  thingModel: [productId: number];
}>();

interface Props {
  categoryList: any[];
  searchParams?: {
    name: string;
    productKey: string;
  };
}

const loading = ref(false);
const list = ref<any[]>([]);
const total = ref(0);
const queryParams = ref({
  pageNo: 1,
  pageSize: 12,
});

// TODO @haohao：注释的优化；
// 获取分类名称
function getCategoryName(categoryId: number) {
  const category = props.categoryList.find((c: any) => c.id === categoryId);
  return category?.name || '未分类';
}

// 获取产品列表
async function getList() {
  loading.value = true;
  try {
    const data = await getProductPage({
      ...queryParams.value,
      ...props.searchParams,
    });
    list.value = data.list || [];
    total.value = data.total || 0;
  } finally {
    loading.value = false;
  }
}

// 处理页码变化
function handlePageChange(page: number, pageSize: number) {
  queryParams.value.pageNo = page;
  queryParams.value.pageSize = pageSize;
  getList();
}

// 获取设备类型颜色
function getDeviceTypeColor(deviceType: number) {
  const colors: Record<number, string> = {
    0: 'blue',
    1: 'green',
  };
  return colors[deviceType] || 'default';
}

defineExpose({
  reload: getList,
  search: () => {
    queryParams.value.pageNo = 1;
    getList();
  },
});

/** 初始化 */
onMounted(() => {
  getList();
});
</script>

<template>
  <div class="product-card-view">
    <!-- 产品卡片列表 -->
    <div v-loading="loading" class="min-h-96">
      <Row v-if="list.length > 0" :gutter="[16, 16]">
        <Col
          v-for="item in list"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="12"
          :lg="6"
          class="mb-4"
        >
          <!-- TODO @haohao：卡片之间的上下距离，太宽了。 -->
          <Card :body-style="{ padding: '20px' }" class="product-card h-full">
            <!-- 顶部标题区域 -->
            <div class="mb-4 flex items-start">
              <!-- TODO @haohao：图标太大了；看看是不是参考 vue3 + element-plus 搞小点；然后标题居中。 -->
              <div class="product-icon">
                <IconifyIcon
                  :icon="item.icon || 'ant-design:inbox-outlined'"
                  class="text-3xl"
                />
              </div>
              <div class="ml-3 min-w-0 flex-1">
                <div class="product-title">{{ item.name }}</div>
              </div>
            </div>
            <!-- 内容区域 -->
            <div class="mb-4 flex items-start">
              <div class="info-list flex-1">
                <div class="info-item">
                  <span class="info-label">产品分类</span>
                  <span class="info-value text-primary">
                    {{ getCategoryName(item.categoryId) }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="info-label">产品类型</span>
                  <Tag
                    :color="getDeviceTypeColor(item.deviceType)"
                    class="info-tag m-0"
                  >
                    {{
                      getDictLabel(
                        DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE,
                        item.deviceType,
                      )
                    }}
                  </Tag>
                </div>
                <div class="info-item">
                  <span class="info-label">产品标识</span>
                  <!-- TODO @haohao：展示 ？有点奇怪，要不小手？ -->
                  <Tooltip :title="item.productKey || item.id" placement="top">
                    <span class="info-value product-key">
                      {{ item.productKey || item.id }}
                    </span>
                  </Tooltip>
                </div>
              </div>
              <!-- TODO @haohao：这里是不是有 image？然后默认 icon -->
              <!-- TODO @haohao：高度太高了。建议和左侧（产品分类 + 产品类型 + 产品标识）高度保持一致 -->
              <div class="product-3d-icon">
                <IconifyIcon
                  icon="ant-design:box-plot-outlined"
                  class="text-2xl"
                />
              </div>
            </div>
            <!-- 按钮组 -->
            <div class="action-buttons">
              <Button
                size="small"
                class="action-btn action-btn-edit"
                @click="emit('edit', item)"
              >
                <!-- TODO @haohao：按钮尽量用中立的按钮，方便迁移 ele；  -->
                <IconifyIcon icon="ant-design:edit-outlined" class="mr-1" />
                编辑
              </Button>
              <Button
                size="small"
                class="action-btn action-btn-detail"
                @click="emit('detail', item.id)"
              >
                <IconifyIcon icon="ant-design:eye-outlined" class="mr-1" />
                详情
              </Button>
              <Button
                size="small"
                class="action-btn action-btn-model"
                @click="emit('thingModel', item.id)"
              >
                <IconifyIcon
                  icon="ant-design:apartment-outlined"
                  class="mr-1"
                />
                物模型
              </Button>
              <Tooltip v-if="item.status === 1" title="启用状态的产品不能删除">
                <Button
                  size="small"
                  danger
                  disabled
                  class="action-btn action-btn-delete !w-8"
                >
                  <IconifyIcon
                    icon="ant-design:delete-outlined"
                    class="text-sm"
                  />
                </Button>
              </Tooltip>
              <Popconfirm
                v-else
                :title="`确认删除产品 ${item.name} 吗?`"
                @confirm="emit('delete', item)"
              >
                <Button
                  size="small"
                  danger
                  class="action-btn action-btn-delete !w-8"
                >
                  <IconifyIcon
                    icon="ant-design:delete-outlined"
                    class="text-sm"
                  />
                </Button>
              </Popconfirm>
            </div>
          </Card>
        </Col>
      </Row>
      <!-- 空状态 -->
      <Empty v-else description="暂无产品数据" class="my-20" />
    </div>

    <!-- 分页 -->
    <!-- TODO @haohao：放到最右侧好点 -->
    <div v-if="list.length > 0" class="flex justify-center">
      <Pagination
        v-model:current="queryParams.pageNo"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :show-total="(total) => `共 ${total} 条`"
        show-quick-jumper
        show-size-changer
        :page-size-options="['12', '24', '36', '48']"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
/** TODO @haohao：看看哪些可以 tindwind 掉 */
.product-card-view {
  .product-card {
    height: 100%;
    overflow: hidden;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 16px rgb(0 0 0 / 8%);
      transform: translateY(-2px);
    }

    :deep(.ant-card-body) {
      display: flex;
      flex-direction: column;
      height: 100%;
    }

    // 产品图标
    .product-icon {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      color: white;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
    }

    // 产品标题
    .product-title {
      overflow: hidden;
      text-overflow: ellipsis;
      font-size: 16px;
      font-weight: 600;
      line-height: 1.5;
      white-space: nowrap;
    }

    // 信息列表
    .info-list {
      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        font-size: 13px;

        &:last-child {
          margin-bottom: 0;
        }

        .info-label {
          flex-shrink: 0;
          margin-right: 8px;
          opacity: 0.65;
        }

        .info-value {
          overflow: hidden;
          text-overflow: ellipsis;
          font-weight: 500;
          white-space: nowrap;

          &.text-primary {
            color: #1890ff;
          }
        }

        .product-key {
          display: inline-block;
          max-width: 150px;
          overflow: hidden;
          text-overflow: ellipsis;
          font-family: 'Courier New', monospace;
          font-size: 12px;
          vertical-align: middle;
          white-space: nowrap;
          cursor: help;
          opacity: 0.85;
        }

        .info-tag {
          font-size: 12px;
        }
      }
    }

    // 3D 图标
    .product-3d-icon {
      display: flex;
      flex-shrink: 0;
      align-items: center;
      justify-content: center;
      width: 100px;
      height: 100px;
      color: #667eea;
      background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
      border-radius: 8px;
      opacity: 0.8;
    }

    // 按钮组
    .action-buttons {
      display: flex;
      gap: 8px;
      padding-top: 12px;
      margin-top: auto;
      border-top: 1px solid var(--ant-color-split);

      .action-btn {
        flex: 1;
        height: 32px;
        font-size: 13px;
        border-radius: 6px;
        transition: all 0.2s;

        &.action-btn-edit {
          color: #1890ff;
          border-color: #1890ff;

          &:hover {
            color: white;
            background: #1890ff;
          }
        }

        &.action-btn-detail {
          color: #52c41a;
          border-color: #52c41a;

          &:hover {
            color: white;
            background: #52c41a;
          }
        }

        &.action-btn-model {
          color: #722ed1;
          border-color: #722ed1;

          &:hover {
            color: white;
            background: #722ed1;
          }
        }

        &.action-btn-delete {
          flex: 0 0 32px;
          padding: 0;
        }
      }
    }
  }
}

// 夜间模式适配
html.dark {
  .product-card-view {
    .product-card {
      &:hover {
        box-shadow: 0 4px 16px rgb(0 0 0 / 30%);
      }

      .product-title {
        color: rgb(255 255 255 / 85%);
      }

      .info-list {
        .info-label {
          color: rgb(255 255 255 / 65%);
        }

        .info-value {
          color: rgb(255 255 255 / 85%);
        }

        .product-key {
          color: rgb(255 255 255 / 75%);
        }
      }

      .product-3d-icon {
        color: #8b9cff;
        background: linear-gradient(135deg, #667eea25 0%, #764ba225 100%);
      }
    }
  }
}
</style>
