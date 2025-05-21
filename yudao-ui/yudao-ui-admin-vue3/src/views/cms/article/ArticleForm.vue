<template>
  <ContentWrap>
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-tabs v-model="activeTab">
        <el-tab-pane label="Basic Info" name="basic">
          <el-form-item label="Title" prop="title">
            <el-input v-model="formData.title" placeholder="Enter article title" />
          </el-form-item>
          <el-form-item label="Slug" prop="slug">
            <el-input v-model="formData.slug" placeholder="Enter article slug (e.g., my-article-title)" />
          </el-form-item>
          <el-form-item label="Category" prop="categoryId">
            <el-tree-select
              v-model="formData.categoryId"
              :data="categoryTree"
              :props="{ label: 'name', value: 'id', children: 'children' }"
              check-strictly
              filterable
              clearable
              placeholder="Select category"
              class="w-full"
            />
          </el-form-item>
          <el-form-item label="Tags" prop="tagIds">
            <el-select
              v-model="formData.tagIds"
              multiple
              filterable
              placeholder="Select tags"
              class="w-full"
            >
              <el-option
                v-for="tag in tagList"
                :key="tag.id"
                :label="tag.name"
                :value="tag.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="Cover Image URL" prop="coverImageUrl">
            <el-input v-model="formData.coverImageUrl" placeholder="Enter cover image URL" />
            <!-- TODO: Integrate a file upload component here -->
          </el-form-item>
          <el-form-item label="Status" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :label="0">Draft</el-radio>
              <el-radio :label="1">Published</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-tab-pane>

        <el-tab-pane label="Content" name="content">
          <el-form-item label="Article Content" prop="content">
            <!-- Placeholder for Rich Text Editor -->
            <el-input
              type="textarea"
              v-model="formData.content"
              :rows="15"
              placeholder="Enter article content here. (Ideally, use a rich text editor)"
            />
            <!-- <Editor v-model="formData.content" height="500px" /> -->
          </el-form-item>
        </el-tab-pane>

        <el-tab-pane label="SEO" name="seo">
          <el-form-item label="Meta Description" prop="metaDescription">
            <el-input type="textarea" v-model="formData.metaDescription" placeholder="Enter meta description" />
          </el-form-item>
          <el-form-item label="Meta Keywords" prop="metaKeywords">
            <el-input v-model="formData.metaKeywords" placeholder="Enter meta keywords (comma-separated)" />
          </el-form-item>
        </el-tab-pane>
      </el-tabs>

      <el-form-item class="mt-20px">
        <el-button @click="submitForm(0)" type="primary" :loading="formLoading">Save Draft</el-button>
        <el-button @click="submitForm(1)" type="success" :loading="formLoading">Publish</el-button>
        <el-button @click="close">Cancel</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createArticle, updateArticle, getArticle, type ArticleVO } from '@/api/cms/article'
import { getSimpleCategoryList, type CategoryVO } from '@/api/cms/category'
import { getSimpleTagList, type TagVO } from '@/api/cms/tag'
import { handleTree } from '@/utils/tree'

defineOptions({ name: 'CmsArticleForm' })

const route = useRoute()
const router = useRouter()

const formLoading = ref(false)
const activeTab = ref('basic')

const formData = reactive<Partial<ArticleVO>>({
  id: undefined,
  title: '',
  slug: '',
  content: '',
  authorId: undefined, // TODO: Set this from logged-in user
  categoryId: undefined,
  tagIds: [],
  status: 0, // Default to Draft
  publishedAt: undefined,
  coverImageUrl: '',
  metaDescription: '',
  metaKeywords: ''
})

const formRules = reactive({
  title: [{ required: true, message: 'Article title cannot be empty', trigger: 'blur' }],
  slug: [{ required: true, message: 'Article slug cannot be empty', trigger: 'blur' }],
  categoryId: [{ required: true, message: 'Category must be selected', trigger: 'change' }],
  content: [{ required: true, message: 'Article content cannot be empty', trigger: 'blur' }],
  status: [{ required: true, message: 'Status must be selected', trigger: 'change' }]
})

const formRef = ref()
const categoryTree = ref<any[]>([])
const tagList = ref<TagVO[]>([])

/** Load categories for select */
const loadCategories = async () => {
  try {
    const data = await getSimpleCategoryList()
    const rootOption = { id: 0, name: 'Root Category (No Parent)', children: [] } // Or handle null parentId better
    categoryTree.value = [rootOption, ...handleTree(data, 'id', 'parentId')]
  } catch (e) {
    ElMessage.error('Failed to load categories')
  }
}

/** Load tags for select */
const loadTags = async () => {
  try {
    tagList.value = await getSimpleTagList()
  } catch (e) {
    ElMessage.error('Failed to load tags')
  }
}

/** Load article data for editing */
const loadData = async (id: number) => {
  formLoading.value = true
  try {
    const data = await getArticle(id)
    Object.assign(formData, data)
    // Ensure tagIds is an array, even if it's null/undefined from API
    formData.tagIds = data.tagIds || []
  } finally {
    formLoading.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await loadTags()
  const articleId = route.params.articleId as string | undefined
  if (articleId) {
    await loadData(parseInt(articleId))
  } else {
    // New article, reset form (though it's already default)
    // formData.value = { ...initialFormData }
  }
})

/** Submit form */
const submitForm = async (targetStatus?: number) => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      formLoading.value = true
      try {
        const dataToSubmit = { ...formData } as ArticleVO
        if (targetStatus !== undefined) {
          dataToSubmit.status = targetStatus
        }
        
        if (dataToSubmit.id) { // Update
          await updateArticle(dataToSubmit)
          ElMessage.success('Article updated successfully')
        } else { // Create
          await createArticle(dataToSubmit)
          ElMessage.success('Article created successfully')
        }
        closeAndRedirect()
      } finally {
        formLoading.value = false
      }
    }
  })
}

/** Close the form and redirect to list view */
const closeAndRedirect = () => {
  // Assuming a router setup where 'CmsArticleManagement' is the name of the list view route
  router.push({ name: 'CmsArticle' }) // Or the actual route name for the article list
}

/** Close button action */
const close = () => {
  closeAndRedirect()
}
</script>

<style scoped>
/* Add any specific styles for this form if needed */
.mt-20px {
  margin-top: 20px;
}
</style>
