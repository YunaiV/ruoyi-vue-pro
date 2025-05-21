<template>
  <ContentWrap>
    <!-- Search Form -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="100px"
    >
      <el-form-item label="Article Title" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="Enter title"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="Category" prop="categoryId">
        <el-select
          v-model="queryParams.categoryId"
          placeholder="Select category"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="category in categoryList"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="Select status"
          clearable
          class="!w-240px"
        >
          <el-option label="Draft" :value="0" />
          <el-option label="Published" :value="1" />
        </el-select>
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
      @click="handleAdd"
      v-hasPermi="['cms:article:create']"
    >
      <Icon icon="ep:plus" class="mr-5px" /> Add Article
    </el-button>
  </ContentWrap>

  <!-- Article Table -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="Title" align="left" prop="title" min-width="200" show-overflow-tooltip />
      <el-table-column label="Category" align="center" prop="categoryName" width="120" />
      <el-table-column label="Status" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? 'Published' : 'Draft' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Views" align="center" prop="views" width="80" />
      <el-table-column
        label="Published At"
        align="center"
        prop="publishedAt"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column
        label="Create Time"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="Actions" align="center" width="220px" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="handleEdit(scope.row.id)"
            v-hasPermi="['cms:article:update']"
          >
            Edit
          </el-button>
          <el-button
            v-if="scope.row.status === 0"
            link
            type="success"
            @click="handlePublish(scope.row.id)"
            v-hasPermi="['cms:article:publish']"
          >
            Publish
          </el-button>
          <el-button
            v-if="scope.row.status === 1"
            link
            type="warning"
            @click="handleUnpublish(scope.row.id)"
            v-hasPermi="['cms:article:unpublish']"
          >
            Unpublish
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['cms:article:delete']"
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
  
  <!-- Note: ArticleForm will be a separate page/route, not a dialog here -->
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { dateFormatter } from '@/utils/formatTime'
import { getArticlePage, deleteArticle, publishArticle, unpublishArticle, type ArticleVO } from '@/api/cms/article'
import { getSimpleCategoryList, type CategoryVO } from '@/api/cms/category' // For category filter

defineOptions({ name: 'CmsArticleManagement' })

const router = useRouter()
const loading = ref(true)
const list = ref<ArticleVO[]>([])
const total = ref(0)
const categoryList = ref<CategoryVO[]>([]) // For category filter dropdown

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: undefined,
  categoryId: undefined,
  status: undefined
})
const queryFormRef = ref()

/** Load category list for filter */
const loadCategoryList = async () => {
  try {
    categoryList.value = await getSimpleCategoryList()
  } catch (e) {
    ElMessage.error('Failed to load categories')
  }
}

/** Get article list */
const getList = async () => {
  loading.value = true
  try {
    const data = await getArticlePage(queryParams)
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

/** Add Article: Navigate to create form page */
const handleAdd = () => {
  router.push({ name: 'CmsArticleCreate' }) // Assuming route name for create form
}

/** Edit Article: Navigate to edit form page */
const handleEdit = (id: number) => {
  router.push({ name: 'CmsArticleEdit', params: { articleId: id } }) // Assuming route name for edit form
}

/** Delete handler */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to delete this article?', 'Confirm Delete', {
      type: 'warning'
    })
    await deleteArticle(id)
    ElMessage.success('Article deleted successfully')
    getList()
  } catch (e) { /* Catch cancellation */ }
}

/** Publish handler */
const handlePublish = async (id: number) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to publish this article?', 'Confirm Publish', {
      type: 'info'
    })
    await publishArticle(id)
    ElMessage.success('Article published successfully')
    getList()
  } catch (e) { /* Catch cancellation */ }
}

/** Unpublish handler */
const handleUnpublish = async (id: number) => {
  try {
    await ElMessageBox.confirm('Are you sure you want to unpublish this article (set to draft)?', 'Confirm Unpublish', {
      type: 'warning'
    })
    await unpublishArticle(id)
    ElMessage.success('Article unpublished successfully')
    getList()
  } catch (e) { /* Catch cancellation */ }
}

onMounted(() => {
  loadCategoryList()
  getList()
})
</script>
