<script lang="ts" setup>
import type { MallPointActivityApi } from '#/api/mall/promotion/point';

import { computed, ref } from 'vue';

import { ContentWrap } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { dateFormatter, fenToYuanFormat } from '@vben/utils';

import { CHANGE_EVENT } from 'element-plus';

import * as PointActivityApi from '#/api/mall/promotion/point';

/**
 * 活动表格选择对话框
 * 1. 单选模式：
 *    1.1 点击表格左侧的单选框时，结束选择，并关闭对话框
 *    1.2 再次打开时，保持选中状态
 * 2. 多选模式：
 *    2.1 点击表格左侧的多选框时，记录选中的活动
 *    2.2 切换分页时，保持活动的选中状态
 *    2.3 点击右下角的确定按钮时，结束选择，关闭对话框
 *    2.4 再次打开时，保持选中状态
 */
defineOptions({ name: 'PointTableSelect' });

defineProps({
  // 多选模式
  multiple: {
    type: Boolean,
    default: false,
  },
});

/** 确认选择时的触发事件 */
const emits = defineEmits<{
  (
    e: 'change',
    v:
      | any
      | MallPointActivityApi.PointActivity
      | MallPointActivityApi.PointActivity[],
  ): void;
}>();
// 列表的总页数
const total = ref(0);
// 列表的数据
const list = ref<MallPointActivityApi.PointActivity[]>([]);
// 列表的加载中
const loading = ref(false);
// 弹窗的是否展示
const dialogVisible = ref(false);
// 查询参数
const queryParams = ref({
  pageNo: 1,
  pageSize: 10,
  name: null,
  status: undefined,
});
const getRedeemedQuantity = computed(
  () => (row: any) => (row.totalStock || 0) - (row.stock || 0),
); // 获得商品已兑换数量
/** 打开弹窗 */
const open = (pointList?: MallPointActivityApi.PointActivity[]) => {
  // 重置
  checkedActivities.value = [];
  checkedStatus.value = {};
  isCheckAll.value = false;
  isIndeterminate.value = false;

  // 处理已选中
  if (pointList && pointList.length > 0) {
    checkedActivities.value = [...pointList];
    checkedStatus.value = Object.fromEntries(
      pointList.map((activityVO) => [activityVO.id, true]),
    );
  }

  dialogVisible.value = true;
  resetQuery();
};
// 提供 open 方法，用于打开弹窗
defineExpose({ open });

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    const data = await PointActivityApi.getPointActivityPage(queryParams.value);
    list.value = data.list;
    total.value = data.total;
    // checkbox绑定undefined会有问题，需要给一个bool值
    list.value.forEach(
      (activityVO) =>
        (checkedStatus.value[activityVO.id] =
          checkedStatus.value[activityVO.id] || false),
    );
    // 计算全选框状态
    calculateIsCheckAll();
  } finally {
    loading.value = false;
  }
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNo = 1;
  getList();
};

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.value = {
    pageNo: 1,
    pageSize: 10,
    name: null,
    status: undefined,
  };
  getList();
};

// 是否全选
const isCheckAll = ref(false);
// 全选框是否处于中间状态：不是全部选中 && 任意一个选中
const isIndeterminate = ref(false);
// 选中的活动
const checkedActivities = ref<MallPointActivityApi.PointActivity[]>([]);
// 选中状态：key为活动ID，value为是否选中
const checkedStatus = ref<Record<string, boolean>>({});

// 选中的活动 activityId
const selectedActivityId = ref();
/** 单选中时触发 */
const handleSingleSelected = (
  pointActivityVO: MallPointActivityApi.PointActivity,
) => {
  emits(CHANGE_EVENT, pointActivityVO);
  // 关闭弹窗
  dialogVisible.value = false;
  // 记住上次选择的ID
  selectedActivityId.value = pointActivityVO.id;
};

/** 多选完成 */
const handleEmitChange = () => {
  // 关闭弹窗
  dialogVisible.value = false;
  emits(CHANGE_EVENT, [...checkedActivities.value]);
};

/** 全选/全不选 */
const handleCheckAll = (checked: boolean) => {
  isCheckAll.value = checked;
  isIndeterminate.value = false;

  list.value.forEach((pointActivity) =>
    handleCheckOne(checked, pointActivity, false),
  );
};

/**
 * 选中一行
 * @param checked 是否选中
 * @param pointActivity 活动
 * @param isCalcCheckAll 是否计算全选
 */
const handleCheckOne = (
  checked: boolean,
  pointActivity: MallPointActivityApi.PointActivity,
  isCalcCheckAll: boolean,
) => {
  if (checked) {
    checkedActivities.value.push(pointActivity);
    checkedStatus.value[pointActivity.id] = true;
  } else {
    const index = findCheckedIndex(pointActivity);
    if (index > -1) {
      checkedActivities.value.splice(index, 1);
      checkedStatus.value[pointActivity.id] = false;
      isCheckAll.value = false;
    }
  }

  // 计算全选框状态
  if (isCalcCheckAll) {
    calculateIsCheckAll();
  }
};

// 查找活动在已选中活动列表中的索引
const findCheckedIndex = (activityVO: MallPointActivityApi.PointActivity) =>
  checkedActivities.value.findIndex((item) => item.id === activityVO.id);

// 计算全选框状态
const calculateIsCheckAll = () => {
  isCheckAll.value = list.value.every(
    (activityVO) => checkedStatus.value[activityVO.id],
  );
  // 计算中间状态：不是全部选中 && 任意一个选中
  isIndeterminate.value =
    !isCheckAll.value &&
    list.value.some((activityVO) => checkedStatus.value[activityVO.id]);
};
</script>

<template>
  <Dialog
    v-model="dialogVisible"
    :append-to-body="true"
    title="选择活动"
    width="70%"
  >
    <ContentWrap>
      <!-- 搜索工作栏 -->
      <el-form
        :inline="true"
        :model="queryParams"
        class="-mb-15px"
        label-width="68px"
      >
        <el-form-item label="活动状态" prop="status">
          <el-select
            v-model="queryParams.status"
            class="!w-240px"
            clearable
            placeholder="请选择活动状态"
          >
            <el-option
              v-for="dict in getDictOptions(DICT_TYPE.COMMON_STATUS, 'number')"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <IconifyIcon class="mr-5px" icon="ep:search" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <IconifyIcon class="mr-5px" icon="ep:refresh" />
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="list" show-overflow-tooltip>
        <!-- 1. 多选模式（不能使用type="selection"，Element会忽略Header插槽） -->
        <el-table-column v-if="multiple" width="55">
          <template #header>
            <el-checkbox
              v-model="isCheckAll"
              :indeterminate="isIndeterminate"
              @change="handleCheckAll"
            />
          </template>
          <template #default="{ row }">
            <el-checkbox
              v-model="checkedStatus[row.id]"
              @change="(checked: boolean) => handleCheckOne(checked, row, true)"
            />
          </template>
        </el-table-column>
        <!-- 2. 单选模式 -->
        <el-table-column v-else label="#" width="55">
          <template #default="{ row }">
            <el-radio
              v-model="selectedActivityId"
              :value="row.id"
              @change="handleSingleSelected(row)"
            >
              <!-- 空格不能省略，是为了让单选框不显示label，如果不指定label不会有选中的效果 -->
              &nbsp;
            </el-radio>
          </template>
        </el-table-column>
        <el-table-column label="活动编号" min-width="80" prop="id" />
        <el-table-column label="商品图片" min-width="80" prop="spuName">
          <template #default="scope">
            <el-image
              :preview-src-list="[scope.row.picUrl]"
              :src="scope.row.picUrl"
              class="h-40px w-40px"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column label="商品标题" min-width="300" prop="spuName" />
        <el-table-column
          :formatter="fenToYuanFormat"
          label="原价"
          min-width="100"
          prop="marketPrice"
        />
        <el-table-column label="原价" min-width="100" prop="marketPrice" />
        <el-table-column
          align="center"
          label="活动状态"
          min-width="100"
          prop="status"
        >
          <template #default="scope">
            <dict-tag
              :type="DICT_TYPE.COMMON_STATUS"
              :value="scope.row.status"
            />
          </template>
        </el-table-column>
        <el-table-column
          align="center"
          label="库存"
          min-width="80"
          prop="stock"
        />
        <el-table-column
          align="center"
          label="总库存"
          min-width="80"
          prop="totalStock"
        />
        <el-table-column
          align="center"
          label="已兑换数量"
          min-width="100"
          prop="redeemedQuantity"
        >
          <template #default="{ row }">
            {{ getRedeemedQuantity(row) }}
          </template>
        </el-table-column>
        <el-table-column
          :formatter="dateFormatter"
          align="center"
          label="创建时间"
          prop="createTime"
          width="180px"
        />
      </el-table>
      <!-- 分页 -->
      <Pagination
        v-model:limit="queryParams.pageSize"
        v-model:page="queryParams.pageNo"
        :total="total"
        @pagination="getList"
      />
    </ContentWrap>
    <template v-if="multiple" #footer>
      <el-button type="primary" @click="handleEmitChange">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
