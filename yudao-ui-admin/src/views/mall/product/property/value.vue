<template>
  <div class="app-container">
    <doc-alert title="功能开启" url="https://doc.iocoder.cn/mall/build/" />

    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="属性项" prop="propertyId">
        <el-select v-model="queryParams.propertyId">
          <el-option v-for="item in propertyOptions" :key="item.id" :label="item.name" :value="item.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['system:dict:create']">新增
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="编号" align="center" prop="id"/>
      <el-table-column label="名称" align="center" prop="name"/>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:dict:update']">修改
          </el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:dict:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="属性项">
          <el-input v-model="form.propertyId" :disabled="true"/>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称"/>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
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
  createPropertyValue,
  deletePropertyValue,
  getProperty,
  getPropertyList,
  getPropertyValue,
  getPropertyValuePage,
  updatePropertyValue
} from '@/api/mall/product/property'

export default {
  name: "PropertyValue",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 字典表格数据
      dataList: [],
      // 默认字典类型
      defaultPropertyId: "",
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 类型数据字典
      propertyOptions: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        propertyId: undefined,
        name: undefined,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [
          {required: true, message: "名称不能为空", trigger: "blur"}
        ]
      },

    };
  },
  created() {
    const propertyId = this.$route.params && this.$route.params.propertyId;
    this.getProperty(propertyId);
    this.getPropertyList();
  },
  methods: {
    /** 查询字典类型详细 */
    getProperty(propertyId) {
      getProperty(propertyId).then(response => {
        this.queryParams.propertyId = response.data.id;
        this.defaultPropertyId = response.data.id;
        this.getList();
      });
    },
    /** 查询字典类型列表 */
    getPropertyList() {
      getPropertyList().then(response => {
        this.propertyOptions = response.data
      });
    },
    /** 查询字典数据列表 */
    getList() {
      this.loading = true;
      getPropertyValuePage(this.queryParams).then(response => {
        this.dataList = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        propertyId: undefined,
        name: undefined,
        remark: undefined
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
      this.queryParams.propertyId = this.defaultPropertyId;
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加属性值";
      this.form.propertyId = this.queryParams.propertyId;
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getPropertyValue(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改属性值";
      });
    },
    /** 提交按钮 */
    submitForm: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updatePropertyValue(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            createPropertyValue(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id;
      this.$modal.confirm('是否确认删除字典编码为"' + ids + '"的数据项?').then(function () {
        return deletePropertyValue(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$modal.confirm('是否确认导出所有数据项?').then(() => {
        this.exportLoading = true;
        return exportData(queryParams);
      }).then(response => {
        this.$download.excel(response, '字典数据.xls');
        this.exportLoading = false;
      }).catch(() => {
      });
    }
  }
};
</script>
