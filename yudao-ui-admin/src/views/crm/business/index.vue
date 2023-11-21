<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商机名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入商机名称" clearable @keyup.enter.native="handleQuery"/>
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
                   v-hasPermi="['crm:business:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['crm:business:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="商机名称" align="center" prop="name" />
      <el-table-column label="客户名称" align="center" prop="customerId" />
      <el-table-column label="商机金额" align="center" prop="price" />
      <el-table-column label="预计成交日期" align="center" prop="dealTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.dealTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="商机状态类型" align="center" prop="statusTypeId" width="120">
        <template v-slot="scope">
          <el-tag> {{getBusinessStatusTypeName(scope.row.statusTypeId)}} </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="商机状态" align="center" prop="statusId" width="100">
        <template v-slot="scope">
          <el-tag> {{getBusinessStatusName(scope.row.statusId)}} </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="负责人" align="center" prop="ownerUserId" />
      <el-table-column label="创建人" align="center" prop="creator" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['crm:business:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['crm:business:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商机名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商机名称" />
        </el-form-item>
        <el-form-item label="客户编号" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入客户编号" />
        </el-form-item>
        <el-form-item label="商机状态类型" prop="statusTypeId">
          <el-select v-model="form.statusTypeId" placeholder="请选择商机状态类型" clearable size="small" @change="changeBusinessStatusType">
            <el-option v-for="item in businessStatusTypeList" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="商机状态" prop="statusId">
          <el-select v-model="form.statusId" placeholder="请选择商机状态" clearable size="small">
            <el-option v-for="item in businessStatusList" :key="item.id" :label="item.name" :value="item.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="预计成交日期" prop="dealTime">
          <el-date-picker clearable v-model="form.dealTime" type="date" value-format="timestamp" placeholder="选择预计成交日期" />
        </el-form-item>
        <el-form-item label="商机金额" prop="price">
          <el-input v-model="form.price" placeholder="请输入商机金额" />
        </el-form-item>
        <el-form-item label="整单折扣(%)" prop="discountPercent">
          <el-input v-model="form.discountPercent" placeholder="请输入整单折扣" />
        </el-form-item>
        <el-form-item label="产品总金额" prop="productPrice">
          <el-input v-model="form.productPrice" placeholder="请输入产品总金额" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
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
import { createBusiness, updateBusiness, deleteBusiness, getBusiness, getBusinessPage, exportBusinessExcel } from "@/api/crm/business";
import { getBusinessStatusListByTypeId, getBusinessStatusList } from "@/api/crm/businessStatus";
import { getBusinessStatusTypeList } from "@/api/crm/businessStatusType";

export default {
  name: "Business",
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
      // 商机列表
      list: [],
      // 根据类型ID获取的商机状态列表
      businessStatusList: [],
      // 所有商机状态列表
      businessStatusAllList: [],
      // 商机状态类型列表
      businessStatusTypeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        statusTypeId: null,
        statusId: null,
        contactNextTime: [],
        customerId: null,
        dealTime: [],
        price: null,
        discountPercent: null,
        productPrice: null,
        remark: null,
        ownerUserId: null,
        createTime: [],
        roUserIds: null,
        rwUserIds: null,
        endStatus: null,
        endRemark: null,
        contactLastTime: [],
        followUpStatus: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "商机名称不能为空", trigger: "blur" }],
        customerId: [{ required: true, message: "客户编号不能为空", trigger: "blur" }],
        roUserIds: [{ required: true, message: "只读权限的用户编号数组不能为空", trigger: "blur" }],
        rwUserIds: [{ required: true, message: "读写权限的用户编号数组不能为空", trigger: "blur" }],
        endStatus: [{ required: true, message: "1赢单2输单3无效不能为空", trigger: "blur" }],
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
      getBusinessPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
      //查询商机状态类型集合
      getBusinessStatusTypeList().then(response => {
        this.businessStatusTypeList = response.data;
      });
      //查询商机状态类型集合
      getBusinessStatusList().then(response => {
        this.businessStatusAllList = response.data;
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
        statusTypeId: undefined,
        statusId: undefined,
        contactNextTime: undefined,
        customerId: undefined,
        dealTime: undefined,
        price: undefined,
        discountPercent: undefined,
        productPrice: undefined,
        remark: undefined,
        ownerUserId: undefined,
        roUserIds: undefined,
        rwUserIds: undefined,
        endStatus: undefined,
        endRemark: undefined,
        contactLastTime: undefined,
        followUpStatus: undefined,
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
      this.title = "添加商机";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getBusiness(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改商机";
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
          updateBusiness(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createBusiness(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除商机编号为"' + id + '"的数据项?').then(function() {
          return deleteBusiness(id);
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
      this.$modal.confirm('是否确认导出所有商机数据项?').then(() => {
          this.exportLoading = true;
          return exportBusinessExcel(params);
        }).then(response => {
          this.$download.excel(response, '商机.xls');
          this.exportLoading = false;
        }).catch(() => {});
    },
    /** 选择商机状态类型事件 */
    changeBusinessStatusType (id) {
      //查询商机状态集合
      getBusinessStatusListByTypeId(id).then(response => {
        this.businessStatusList = response.data;
      });
    },
    /** 商机状态类型名格式化 */
    getBusinessStatusTypeName(typeId) {
      for (const item of this.businessStatusTypeList) {
        if (item.id === typeId) {
          return item.name;
        }
      }
      return '未知';
    },
    /** 商机状态名格式化 */
    getBusinessStatusName(id) {
      for (const item of this.businessStatusAllList) {
        if (item.id === id) {
          return item.name;
        }
      }
      return '未知';
    }
  }
};
</script>
