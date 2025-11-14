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

onMounted(() => {
  getList();
});

// 暴露方法供父组件调用
defineExpose({
  reload: getList,
  search: () => {
    queryParams.value.pageNo = 1;
    getList();
  },
});
</script>

<template>
  <div class="product-card-view">
    <!-- 产品卡片列表 -->
    <div v-loading="loading" class="min-h-[400px]">
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
          <Card :body-style="{ padding: '20px' }" class="product-card h-full">
            <!-- 顶部标题区域 -->
            <div class="mb-4 flex items-start">
              <div class="product-icon">
                <IconifyIcon
                  :icon="item.icon || 'ant-design:inbox-outlined'"
                  class="text-[32px]"
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
                  <Tooltip :title="item.productKey || item.id" placement="top">
                    <span class="info-value product-key">
                      {{ item.productKey || item.id }}
                    </span>
                  </Tooltip>
                </div>
              </div>
              <div class="product-3d-icon">
                <IconifyIcon
                  icon="ant-design:box-plot-outlined"
                  class="text-[80px]"
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
              <Popconfirm
                :title="`确认删除产品 ${item.name} 吗?`"
                @confirm="emit('delete', item)"
              >
                <Button
                  size="small"
                  danger
                  class="action-btn action-btn-delete"
                >
                  <IconifyIcon icon="ant-design:delete-outlined" />
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
    <div v-if="list.length > 0" class="mt-6 flex justify-center">
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
.product-card-view {
  .product-card {
    height: 100%;
    overflow: hidden;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      border-color: #d9d9d9;
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
      color: #1f2937;
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
          color: #6b7280;
        }

        .info-value {
          overflow: hidden;
          text-overflow: ellipsis;
          font-weight: 500;
          color: #1f2937;
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
          color: #374151;
          white-space: nowrap;
          cursor: help;
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
    }

    // 按钮组
    .action-buttons {
      display: flex;
      gap: 8px;
      padding-top: 12px;
      margin-top: auto;
      border-top: 1px solid #f0f0f0;

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
</style>
