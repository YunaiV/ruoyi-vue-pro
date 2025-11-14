# Upload Components - Naive UI 版本

本目录包含已重构为 Naive UI 的上传组件。

## 组件列表

### 1. ImageUpload - 图片上传组件

- **文件**: `image-upload.vue`
- **功能**: 专门用于图片上传的组件，支持图片预览
- **特性**:
  - 支持单图/多图上传
  - 支持图片预览（使用 NModal + NImage）
  - 支持拖拽上传
  - 自动校验文件类型和大小
  - 支持自定义上传 API
  - 支持进度显示

### 2. FileUpload - 文件上传组件

- **文件**: `file-upload.vue`
- **功能**: 通用文件上传组件
- **特性**:
  - 支持单文件/多文件上传
  - 支持拖拽上传区域
  - 支持文件预览和下载
  - 自动校验文件类型和大小
  - 支持自定义上传 API
  - 支持进度显示
  - 支持返回文本内容（用于配置文件等）

### 3. InputUpload - 输入框上传组件

- **文件**: `input-upload.vue`
- **功能**: 结合输入框和文件上传的组件
- **特性**:
  - 支持文本输入框或文本域
  - 支持通过上传文件自动填充内容
  - 使用 NGrid 布局，响应式设计

## 使用示例

### ImageUpload 图片上传

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { ImageUpload } from '#/components/upload';

const imageUrl = ref('');
</script>

<template>
  <ImageUpload
    v-model="imageUrl"
    :max-number="1"
    :max-size="2"
    :accept="['jpg', 'jpeg', 'png', 'gif']"
  />
</template>
```

### FileUpload 文件上传

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { FileUpload } from '#/components/upload';

const fileUrl = ref('');
</script>

<template>
  <FileUpload
    v-model="fileUrl"
    :max-number="3"
    :max-size="5"
    :accept="['pdf', 'doc', 'docx']"
    drag
  />
</template>
```

### InputUpload 输入框上传

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { InputUpload } from '#/components/upload';

const configContent = ref('');
</script>

<template>
  <InputUpload
    v-model="configContent"
    input-type="textarea"
    :file-upload-props="{
      accept: ['json', 'yaml', 'yml'],
      maxSize: 1,
    }"
  />
</template>
```

## Props 说明

### 通用 Props (FileUploadProps)

| 属性             | 类型                 | 默认值  | 说明               |
| ---------------- | -------------------- | ------- | ------------------ |
| modelValue/value | `string \| string[]` | -       | v-model 绑定值     |
| accept           | `string[]`           | `[]`    | 接受的文件类型     |
| maxSize          | `number`             | `2`     | 文件最大大小（MB） |
| maxNumber        | `number`             | `1`     | 最大文件数量       |
| multiple         | `boolean`            | `false` | 是否支持多选       |
| disabled         | `boolean`            | `false` | 是否禁用           |
| drag             | `boolean`            | `false` | 是否支持拖拽上传   |
| directory        | `string`             | -       | 上传目录           |
| api              | `Function`           | -       | 自定义上传 API     |
| showDescription  | `boolean`            | -       | 是否显示描述文本   |

### ImageUpload 特有 Props

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| listType | `string` | `'picture-card'` | 列表类型 |
| accept | `string[]` | `['jpg', 'jpeg', 'png', 'gif', 'webp']` | 接受的图片类型 |
| showDescription | `boolean` | `true` | 是否显示描述文本 |

### InputUpload 特有 Props

| 属性            | 类型                    | 默认值    | 说明             |
| --------------- | ----------------------- | --------- | ---------------- |
| inputType       | `'input' \| 'textarea'` | `'input'` | 输入框类型       |
| inputProps      | `InputProps`            | -         | 输入框属性       |
| fileUploadProps | `FileUploadProps`       | -         | 文件上传组件属性 |

## Events

| 事件名 | 参数 | 说明 |
| --- | --- | --- |
| update:value | `value: string \| string[]` | 值更新事件 |
| update:modelValue | `value: string \| string[]` | v-model 更新事件 |
| change | `value: string \| string[]` | 值变化事件 |
| delete | `file: UploadFileInfo` | 删除文件事件 |
| preview | `file: UploadFileInfo` | 预览文件事件（仅 FileUpload） |
| returnText | `text: string` | 返回文件文本内容（仅 FileUpload） |

## 辅助工具

### useUpload

- **文件**: `use-upload.ts`
- **功能**: 提供上传相关的工具函数
- **主要方法**:
  - `httpRequest`: 统一的文件上传请求方法
  - `getUploadUrl`: 获取上传 URL

### useUploadType

- **功能**: 处理上传类型相关的逻辑
- **主要方法**:
  - `getStringAccept`: 获取 accept 字符串
  - `getHelpText`: 获取帮助文本

## 技术栈

- **UI 框架**: Naive UI
- **核心组件**:
  - NUpload
  - NImage
  - NImageGroup
  - NModal
  - NButton
  - NGrid
  - NInput
- **工具库**:
  - @vueuse/core
  - @vben/utils

## 注意事项

1. 文件状态使用 Naive UI 的状态值：`'pending' | 'uploading' | 'finished' | 'error' | 'removed'`
2. 所有文件 ID 使用 Naive UI 的 `id` 字段，而不是 `uid`
3. 上传前会自动校验文件类型和大小
4. 支持两种上传模式：
   - 客户端直接上传（S3）
   - 通过后端上传
5. 支持自定义上传 API，如果不提供则使用默认的上传接口

## 迁移指南

从 Ant Design Vue 迁移到 Naive UI 的主要变化：

1. **组件导入**:

   ```typescript
   // 旧
   import { Upload } from 'ant-design-vue';

   // 新
   import { NUpload } from 'naive-ui';
   ```

2. **文件列表类型**:

   ```typescript
   // 旧
   import type { UploadFile } from 'ant-design-vue';

   // 新
   import type { UploadFileInfo } from 'naive-ui';
   ```

3. **状态值**:

   ```typescript
   // 旧
   status: 'done';

   // 新
   status: 'finished';
   ```

4. **事件回调**:

   ```typescript
   // 旧
   @remove="handleRemove"
   function handleRemove(file: UploadFile) { }

   // 新
   @remove="handleRemove"
   function handleRemove(options: { file: UploadFileInfo; fileList: UploadFileInfo[] }) { }
   ```

5. **自定义上传**:

   ```typescript
   // 旧
   customRequest(info: UploadRequestOption) {
     info.onSuccess!(res);
   }

   // 新
   customRequest(options: UploadCustomRequestOptions) {
     options.onFinish();
   }
   ```

## 更新日志

### v1.0.0 (2025-01-16)

- ✅ 将所有上传组件从 Ant Design Vue 重构为 Naive UI
- ✅ 保持原有功能和 API 兼容性
- ✅ 优化代码结构和类型定义
- ✅ 修复所有 linter 错误
- ✅ 添加完整的文档说明
