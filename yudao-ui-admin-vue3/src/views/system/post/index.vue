<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import XEUtils from 'xe-utils'
import { useI18n } from '@/hooks/web/useI18n'
import { VxeFormEvents, VxeFormItemProps, VxeGrid, VxeGridInstance, VxeGridProps } from 'vxe-table'
import * as PostApi from '@/api/system/post'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { ContentWrap } from '@/components/ContentWrap'
import { PostPageReqVO, PostVO } from '@/api/system/post/types'
import { rules, allSchemas } from './post.data'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n() // 国际化

const xGrid = ref<VxeGridInstance>()
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 遮罩层

const gridOptions = reactive<VxeGridProps>({
  loading: false,
  rowConfig: {
    keyField: 'id',
    isHover: true
  },
  toolbarConfig: {
    buttons: [{ code: 'insert_actived', name: '新增' }]
  },
  printConfig: {
    columns: [
      { field: 'name' },
      { field: 'code' },
      { field: 'sort' },
      { field: 'status' },
      { field: 'createTime' }
    ]
  },
  formConfig: {
    titleWidth: 100,
    titleAlign: 'right',
    items: [
      {
        field: 'name',
        title: '岗位名称',
        span: 8,
        itemRender: { name: '$input', props: { placeholder: '请输入岗位名称' } }
      },
      {
        field: 'code',
        title: '岗位编码',
        span: 8,
        itemRender: { name: '$input', props: { placeholder: '请输入岗位编码' } }
      },
      {
        field: 'status',
        title: t('common.status'),
        span: 8,
        itemRender: { name: '$select', options: getIntDictOptions(DICT_TYPE.COMMON_STATUS) }
      },
      {
        span: 24,
        align: 'center',
        collapseNode: true,
        itemRender: {
          name: '$buttons',
          children: [
            { props: { type: 'submit', content: t('common.query'), status: 'primary' } },
            { props: { type: 'reset', content: t('common.reset') } }
          ]
        }
      }
    ]
  },
  columns: [
    { type: 'seq', title: t('common.index'), width: 100 },
    { field: 'name', title: '岗位名称' },
    { field: 'code', title: '岗位编码' },
    { field: 'sort', title: '岗位顺序' },
    {
      field: 'status',
      title: t('common.status'),
      slots: {
        default: 'status_default'
      }
    },
    {
      field: 'createTime',
      title: t('common.createTime'),
      width: 160,
      sortable: true,
      formatter({ cellValue }) {
        return XEUtils.toDateString(cellValue, 'yyyy-MM-dd HH:ss:mm')
      }
    },
    {
      field: 'action',
      title: t('table.action'),
      width: '240px',
      showOverflow: true,
      slots: {
        default: 'action_default'
      }
    }
  ],
  pagerConfig: {
    border: false,
    background: false,
    perfect: true,
    pageSize: 10,
    pagerCount: 7,
    pageSizes: [5, 10, 15, 20, 50, 100, 200, 500],
    layouts: ['PrevJump', 'PrevPage', 'Jump', 'PageCount', 'NextPage', 'NextJump', 'Sizes', 'Total']
  },
  proxyConfig: {
    seq: true, // 启用动态序号代理（分页之后索引自动计算为当前页的起始序号）
    props: {
      result: 'list',
      total: 'total'
    },
    ajax: {
      query: ({ page, form }) => {
        const queryParams: PostPageReqVO = Object.assign({}, form)
        queryParams.pageSize = page.pageSize
        queryParams.pageNo = page.currentPage
        return new Promise(async (resolve) => {
          resolve(await PostApi.getPostPageApi(queryParams))
        })
      }
    }
  }
})
const formData = ref<PostVO>({
  name: '',
  code: '',
  sort: 0,
  status: 0,
  remark: '',
  createTime: ''
})
const formItems = reactive<VxeFormItemProps[]>([
  {
    field: 'id',
    title: 'id',
    visible: false
  },
  {
    field: 'name',
    title: '岗位名称',
    span: 8,
    itemRender: { name: '$input', props: { placeholder: '请输入岗位名称' } }
  },
  {
    field: 'code',
    title: '岗位编码',
    span: 8,
    itemRender: { name: '$input', props: { placeholder: '请输入岗位编码' } }
  },
  {
    field: 'sort',
    title: '岗位顺序',
    span: 8,
    itemRender: { name: '$input', props: { type: 'number', placeholder: '请输入岗位顺序' } }
  },
  {
    field: 'status',
    title: t('common.status'),
    span: 8,
    itemRender: {
      name: '$select',
      options: getIntDictOptions(DICT_TYPE.COMMON_STATUS),
      props: { placeholder: '请选择' }
    }
  },
  {
    align: 'center',
    span: 24,
    itemRender: {
      name: '$buttons',
      children: [
        { props: { type: 'submit', content: t('action.save'), status: 'primary' } },
        { props: { type: 'reset', content: t('common.reset') } }
      ]
    }
  }
])
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
// 详情操作
const handleDetail = (row: PostVO) => {
  setDialogTile('detail')
  detailRef.value = row
}
// 新增操作
const handleCreate = () => {
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await PostApi.getPostApi(rowId)
  formData.value = res
}
// 删除操作
const handleDelete = (rowId: number) => {
  ElMessageBox.confirm(t('common.delMessage'), t('common.confirmTitle'), {
    confirmButtonText: t('common.ok'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(async () => {
      await PostApi.deletePostApi(rowId)
    })
    .finally(() => {
      ElMessage.success(t('common.delSuccess'))
      const $grid = xGrid.value
      $grid?.commitProxy('query')
    })
}
// 提交按钮
const submitForm: VxeFormEvents.Submit = async () => {
  actionLoading.value = true
  // 提交请求
  try {
    const data = formData.value as PostVO
    if (actionType.value === 'create') {
      await PostApi.createPostApi(data)
      ElMessage.success(t('common.createSuccess'))
    } else {
      await PostApi.updatePostApi(data)
      ElMessage.success(t('common.updateSuccess'))
    }
    // 操作成功，重新加载列表
    dialogVisible.value = false
  } finally {
    actionLoading.value = false
    const $grid = xGrid.value
    $grid?.commitProxy('query')
  }
}
</script>
<template>
  <ContentWrap>
    <vxe-grid ref="xGrid" v-bind="gridOptions">
      <template #toolbar_buttons>
        <el-button type="primary" v-hasPermi="['system:post:create']" @click="handleCreate">
          <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.add') }}
        </el-button>
      </template>
      <template #status_default="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #action_default="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['system:post:update']"
          @click="handleUpdate(row.id)"
        >
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:post:update']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:post:delete']"
          @click="handleDelete(row.id)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
      </template>
    </vxe-grid>
  </ContentWrap>
  <vxe-modal
    v-model="dialogVisible"
    id="myModal6"
    width="800"
    height="400"
    min-width="460"
    min-height="320"
    show-zoom
    resize
    remember
    storage
    transfer
    show-footer
  >
    <template #title>
      <span>{{ dialogTitle }}</span>
    </template>
    <template #default>
      <!-- 对话框(添加 / 修改) -->
      <vxe-form
        v-if="['create', 'update'].includes(actionType)"
        :data="formData"
        :items="formItems"
        :rules="rules"
        titleColon
        @submit="submitForm"
      />
      <Descriptions
        v-if="actionType === 'detail'"
        :schema="allSchemas.detailSchema"
        :data="detailRef"
      >
        <template #status="{ row }">
          <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
        <template #createTime="{ row }">
          <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </template>
      </Descriptions>
    </template>
  </vxe-modal>
</template>
