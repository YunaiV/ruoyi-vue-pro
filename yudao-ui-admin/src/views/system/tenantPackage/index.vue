<template>
  <div class="app-container">
    <doc-alert title="SaaS 多租户" url="https://doc.iocoder.cn/saas-tenant/" />
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="套餐名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入套餐名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                       :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
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
                   v-hasPermi="['system:tenant-package:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="套餐编号" align="center" prop="id" width="120" />
      <el-table-column label="套餐名" align="center" prop="name" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:tenant-package:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:tenant-package:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="套餐名" prop="name">
          <el-input v-model="form.name" placeholder="请输入套餐名" />
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event)">展开/折叠</el-checkbox>
          <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event)">全选/全不选</el-checkbox>
          <el-tree class="tree-border" :data="menuOptions" show-checkbox ref="menu" node-key="id"
                   :check-strictly="menuCheckStrictly" empty-text="加载中，请稍后" :props="defaultProps"></el-tree>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
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
import { createTenantPackage, updateTenantPackage, deleteTenantPackage, getTenantPackage, getTenantPackagePage} from "@/api/system/tenantPackage";
import {CommonStatusEnum} from "@/utils/constants";
import {listSimpleMenus} from "@/api/system/menu";

export default {
  name: "TenantPackage",
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
      // 租户套餐列表
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
        status: null,
        remark: null,
        createTime: []
      },
      // 表单参数
      form: {},
      menuExpand: false,
      menuNodeAll: false,
      menuCheckStrictly: true,
      defaultProps: {
        label: "name",
        children: "children"
      },
      menuOptions: [], // 菜单列表
      // 表单校验
      rules: {
        name: [{ required: true, message: "套餐名不能为空", trigger: "blur" }],
        status: [{ required: true, message: "状态不能为空", trigger: "blur" }],
        menuIds: [{ required: true, message: "关联的菜单编号不能为空", trigger: "blur" }],
      }
    };
  },
  created() {
    this.getList();
    this.getMenus();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getTenantPackagePage(this.queryParams).then(response => {
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
      // 菜单选择重置
      if (this.$refs.menu !== undefined) {
        this.$refs.menu.setCheckedKeys([]);
      }
      this.menuExpand = false;
      this.menuNodeAll = false;
      this.menuCheckStrictly = true;
      // 表单重置
      this.form = {
        id: undefined,
        name: undefined,
        status: CommonStatusEnum.ENABLE,
        remark: undefined,
        menuIds: undefined,
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
      // 表单重置
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加租户套餐";
      // 设置为非严格，继续使用半选中
      this.menuCheckStrictly = false;
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      this.open = true;
      this.title = "修改租户套餐";
      // 获得菜单列表
      getTenantPackage(id).then(response => {
        this.form = response.data;
        // 设置菜单项
        // 设置为严格，避免设置父节点自动选中子节点，解决半选中问题
        this.menuCheckStrictly = true
        // 设置选中
        this.$refs.menu.setCheckedKeys(response.data.menuIds);
        // 设置为非严格，继续使用半选中
        this.menuCheckStrictly = false
      });
    },
    /** 获得菜单 */
    getMenus() {
      listSimpleMenus().then(response => {
        // 处理 menuOptions 参数
        this.menuOptions = [];
        // 只需要配置
        this.menuOptions.push(...this.handleTree(response.data, "id"));
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
          updateTenantPackage({
            ...this.form,
            menuIds: [...this.$refs.menu.getCheckedKeys(), ...this.$refs.menu.getHalfCheckedKeys()]
          }).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createTenantPackage({
          ...this.form,
          menuIds: [...this.$refs.menu.getCheckedKeys(), ...this.$refs.menu.getHalfCheckedKeys()]
        }).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除租户套餐编号为"' + id + '"的数据项?').then(function() {
          return deleteTenantPackage(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    // 树权限（展开/折叠）
    handleCheckedTreeExpand(value, type) {
      let treeList = this.menuOptions;
      for (let i = 0; i < treeList.length; i++) {
        this.$refs.menu.store.nodesMap[treeList[i].id].expanded = value;
      }
    },
    // 树权限（全选/全不选）
    handleCheckedTreeNodeAll(value) {
      this.$refs.menu.setCheckedNodes(value ? this.menuOptions: []);
    },
    // 树权限（父子联动）
    handleCheckedTreeConnect(value) {
      this.form.menuCheckStrictly = value;
    },
  }
};
</script>
