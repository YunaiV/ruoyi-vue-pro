<template>
  <ContentWrap>
    <!-- Search Form -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="Tag Name" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Enter tag name"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="Tag Slug" prop="slug">
        <el-input
          v-model="queryParams.slug"
          placeholder="Enter tag slug"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> Search</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> Reset</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- Action Buttons -->
  <ContentWrap>
    <el-button
      type="primary"
      plain
      @click="openForm('create')"
      v-hasPermi="['cms:tag:create']"
    >
      <Icon icon="ep:plus" class="mr-5px" /> Add Tag
    </el-button>
  </ContentWrap>

  <!-- Tag Table -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="Name" align="center" prop="name" />
      <el-table-column label="Slug" align="center" prop="slug" />
      <el-table-column
        label="Create Time"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="Actions" align="center" width="200px" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['cms:tag:update']"
          >
            Edit
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['cms:tag:delete']"
          >
            Delete
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- Pagination -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- Tag Form Dialog -->
  <TagForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { getTagPage, deleteTag } from '@/api/cms/tag' 
import TagForm from './TagForm.vue'

defineOptions({ name: 'CmsTagManagement' })

const loading = ref(true) 
const list = ref<any[]>([]) 
const total = ref(0) 

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  slug: undefined
})
const queryFormRef = ref() 
const formRef = ref()

/** Get tag list */
const getList = async () => {
  loading.value = true
  try {
    const data = await getTagPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** Search handler */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** Reset search form */
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

/** Open form dialog */
const openForm = (type: string, id?: number) => {
  formRef.value?.open(type, id)
}

/** Delete handler */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this tag?', 'Confirm Delete', {
      type: 'warning'
    })
    await deleteTag(id)
    ElMessage.success('Tag deleted successfully')
    getList() 
  } catch (e) {
    // Catch cancellation or error
  }
}

onMounted(() => {
  getList()
})
</script>
