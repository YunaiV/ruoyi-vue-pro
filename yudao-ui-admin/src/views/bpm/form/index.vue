<template>
  <div class="app-container">
    <doc-alert title="工作流" url="https://doc.iocoder.cn/bpm" />

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="表单名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入表单名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['bpm:form:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="表单名" align="center" prop="name" />
      <el-table-column label="开启状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleDetail(scope.row)"
                     v-hasPermi="['bpm:form:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['bpm:form:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['bpm:form:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!--表单配置详情-->
    <el-dialog title="表单详情" :visible.sync="detailOpen" width="50%" append-to-body>
      <div class="test-form">
        <parser :key="new Date().getTime()" :form-conf="detailForm" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {deleteForm, getForm, getFormPage} from "@/api/bpm/form";
import Parser from '@/components/parser/Parser'
import {decodeFields} from "@/utils/formGenerator";

export default {
  name: "Form",
  components: {
    Parser
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 工作流的列表
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
      },
      // 表单详情
      detailOpen: false,
      detailForm: {
        fields: []
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
      // 执行查询
      getFormPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
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
    /** 详情按钮操作 */
    handleDetail(row) {
      getForm(row.id).then(response => {
        // 设置值
        const data = response.data
        this.detailForm = {
          ...JSON.parse(data.conf),
          fields: decodeFields(data.fields)
        }
        // 弹窗打开
        this.detailOpen = true
      })
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({
        path:"/bpm/manager/form/edit"
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({
        path:"/bpm/manager/form/edit",
        query:{
          formId: row.id
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除工作表单的编号为"' + id + '"的数据项?').then(function() {
        return deleteForm(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
