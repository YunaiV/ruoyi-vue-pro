<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="110px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入联系人名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="下次联系时间" prop="nextTime">
        <el-date-picker v-model="queryParams.nextTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
<!--      <el-form-item label="电话" prop="telephone">-->
<!--        <el-input v-model="queryParams.telephone" placeholder="请输入电话" clearable @keyup.enter.native="handleQuery"/>-->
<!--      </el-form-item>-->
<!--      <el-form-item label="电子邮箱" prop="email">-->
<!--        <el-input v-model="queryParams.email" placeholder="请输入电子邮箱" clearable @keyup.enter.native="handleQuery"/>-->
<!--      </el-form-item>-->
<!--      <el-form-item label="职务" prop="post">-->
<!--        <el-input v-model="queryParams.post" placeholder="请输入职务" clearable @keyup.enter.native="handleQuery"/>-->
<!--      </el-form-item>-->
      <el-form-item label="客户编号" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
<!--      <el-form-item label="地址" prop="address">-->
<!--        <el-input v-model="queryParams.address" placeholder="请输入地址" clearable @keyup.enter.native="handleQuery"/>-->
<!--      </el-form-item>-->
<!--      <el-form-item label="备注" prop="remark">-->
<!--        <el-input v-model="queryParams.remark" placeholder="请输入备注" clearable @keyup.enter.native="handleQuery"/>-->
<!--      </el-form-item>-->
      <el-form-item label="负责人用户编号" prop="ownerUserId">
        <el-input v-model="queryParams.ownerUserId" placeholder="请输入负责人用户编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
<!--      <el-form-item label="创建时间" prop="createTime">-->
<!--        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"-->
<!--                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />-->
<!--      </el-form-item>-->
<!--      <el-form-item label="最后跟进时间" prop="lastTime">-->
<!--        <el-date-picker v-model="queryParams.lastTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"-->
<!--                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />-->
<!--      </el-form-item>-->
<!--      <el-form-item label="更新人" prop="updator">-->
<!--        <el-input v-model="queryParams.updator" placeholder="请输入更新人" clearable @keyup.enter.native="handleQuery"/>-->
<!--      </el-form-item>-->
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['crm:contact:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['crm:contact:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="姓名" align="center" prop="name" />
      <el-table-column label="下次联系时间" align="center" prop="nextTime" >
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.nextTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号" align="center" prop="mobile" />
      <el-table-column label="电话" align="center" prop="telephone" />
      <el-table-column label="电子邮箱" align="center" prop="email" />
      <el-table-column label="职务" align="center" prop="post" />
      <el-table-column label="客户编号" align="center" prop="customerId" />
      <el-table-column label="地址" align="center" prop="address" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="负责人用户编号" align="center" prop="ownerUserId" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最后跟进时间" align="center" prop="lastTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.lastTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新人" align="center" prop="updator" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['crm:contact:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['crm:contact:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入联系人名称" />
        </el-form-item>
        <el-form-item label="下次联系时间" prop="nextTime">
          <el-date-picker clearable v-model="form.nextTime" type="date" value-format="timestamp" placeholder="选择下次联系时间" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="电话" prop="telephone">
          <el-input v-model="form.telephone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入电子邮箱" />
        </el-form-item>
        <el-form-item label="职务" prop="post">
          <el-input v-model="form.post" placeholder="请输入职务" />
        </el-form-item>
        <el-form-item label="客户编号" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入客户编号" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="负责人用户编号" prop="ownerUserId">
          <el-input v-model="form.ownerUserId" placeholder="请输入负责人用户编号" />
        </el-form-item>
        <el-form-item label="最后跟进时间" prop="lastTime">
          <el-date-picker clearable v-model="form.lastTime" type="date" value-format="timestamp" placeholder="选择最后跟进时间" />
        </el-form-item>
        <el-form-item label="更新人" prop="updator">
          <el-input v-model="form.updator" placeholder="请输入更新人" />
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
import { createContact, updateContact, deleteContact, getContact, getContactPage, exportContactExcel } from "@/api/crm/contact/contact";

export default {
  name: "Contact",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // crm联系人列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        nextTime: [],
        mobile: null,
        telephone: null,
        email: null,
        post: null,
        customerId: null,
        address: null,
        remark: null,
        ownerUserId: null,
        createTime: [],
        lastTime: [],
        updator: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
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
      getContactPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
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
        name: undefined,
        nextTime: undefined,
        mobile: undefined,
        telephone: undefined,
        email: undefined,
        post: undefined,
        customerId: undefined,
        address: undefined,
        remark: undefined,
        ownerUserId: undefined,
        lastTime: undefined,
        updator: undefined,
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
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加联系人";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getContact(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改联系人";
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
          updateContact(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createContact(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除联系人编号为"' + id + '"的数据项?').then(function() {
          return deleteContact(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.$modal.confirm('是否确认导出所有联系人数据项?').then(() => {
          this.exportLoading = true;
          return exportContactExcel(params);
        }).then(response => {
          this.$download.excel(response, '联系人.xls');
          this.exportLoading = false;
        }).catch(() => {});
    }
  }
};
</script>
