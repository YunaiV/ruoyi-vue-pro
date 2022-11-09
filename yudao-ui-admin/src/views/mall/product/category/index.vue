<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入分类名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['product:category:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-sort" size="mini" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-if="refreshTable" v-loading="loading" :data="list"  row-key="id" :default-expand-all="isExpandAll"
              :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column label="分类名称" prop="name"/>
      <el-table-column label="分类图片" align="center" prop="picUrl">
        <template slot-scope="scope">
          <img v-if="scope.row.picUrl" :src="scope.row.picUrl" alt="分类图片" style="height: 100px"/>
        </template>
      </el-table-column>
      <el-table-column label="分类排序" align="center" prop="sort"/>
      <el-table-column label="开启状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['product:category:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['product:category:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级分类" prop="parentId">
          <Treeselect v-model="form.parentId" :options="parentCategoryOptions" :normalizer="normalizer" :show-count="true"
                      :defaultExpandLevel="1" placeholder="上级分类"/>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称"/>
        </el-form-item>
        <el-form-item label="分类图片" prop="picUrl">
          <ImageUpload v-model="form.picUrl" :limit="1" :is-show-tip="false" />
          <div v-if="form.parentId === 0" style="font-size: 10px">推荐 200x100 图片分辨率</div>
          <div v-else style="font-size: 10px">推荐 100x100 图片分辨率</div>
        </el-form-item>
        <el-form-item label="分类排序" prop="sort">
          <el-input-number v-model="form.sort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="开启状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                      :key="dict.value" :label="parseInt(dict.value)">{{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类描述">
          <el-input v-model="form.description" type="textarea" placeholder="请输入分类描述" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  createProductCategory,
  deleteProductCategory,
  exportCategoryExcel,
  getProductCategory,
  getProductCategoryList,
  updateProductCategory
} from "@/api/mall/product/category";
import Editor from '@/components/Editor';
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
import IconSelect from "@/components/IconSelect";
import ImageUpload from '@/components/ImageUpload';
import {CommonStatusEnum} from "@/utils/constants";

export default {
  name: "ProductCategory",
  components: {
    Editor, Treeselect, ImageUpload
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 商品分类列表
      list: [],
      // 商品分类树选项
      parentCategoryOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否展开，默认全部折叠
      isExpandAll: false,
      // 重新渲染表格状态
      refreshTable: true,
      // 查询参数
      queryParams: {
        name: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        parentId: [{required: true, message: "请选择上级分类", trigger: "blur"}],
        name: [{required: true, message: "分类名称不能为空", trigger: "blur"}],
        picUrl: [{required: true, message: "分类图片不能为空", trigger: "blur"}],
        sort: [{required: true, message: "分类排序不能为空", trigger: "blur"}],
        status: [{required: true, message: "开启状态不能为空", trigger: "blur"}],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      // 执行查询
      getProductCategoryList(params).then(response => {
        this.list = this.handleTree(response.data, "id", "parentId");
        this.loading = false;
        // 属性下拉框
        this.parentCategoryOptions = [];
        const menu = {id: 0, name: '顶级分类', children: []};
        menu.children = this.handleTree(response.data, "id", "parentId");
        this.parentCategoryOptions.push(menu);
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        parentId: undefined,
        name: undefined,
        pirUrl: undefined,
        sort: 0,
        description: undefined,
        status: CommonStatusEnum.ENABLE,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 展开/折叠操作 */
    toggleExpandAll() {
      this.refreshTable = false;
      this.isExpandAll = !this.isExpandAll;
      this.$nextTick(() => {
        this.refreshTable = true;
      });
    },
    /** 转换菜单数据结构 */
    normalizer(node) {
      if (node.children && !node.children.length) {
        delete node.children;
      }
      return {
        id: node.id,
        label: node.name,
        children: node.children
      };
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加商品分类";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getProductCategory(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改商品分类";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateProductCategory(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createProductCategory(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除商品分类编号为"' + id + '"的数据项?').then(function () {
        return deleteProductCategory(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    }
  }
};
</script>
