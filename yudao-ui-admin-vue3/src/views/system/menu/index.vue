<template>
   <div class="app-container">
     <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch">
       <el-form-item label="菜单名称" prop="name">
         <el-input v-model="queryParams.name" placeholder="请输入菜单名称" clearable @keyup.enter.native="handleQuery"/>
       </el-form-item>
       <el-form-item label="状态" prop="status">
         <el-select v-model="queryParams.status" placeholder="请选择菜单状态" clearable>
           <el-option v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                      :key="dict.value" :label="dict.label" :value="dict.value"/>
         </el-select>
       </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
         <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd"
               v-hasPermi="['system:menu:create']">新增</el-button>
         </el-col>
         <el-col :span="1.5">
            <el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
         </el-col>
         <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <el-table v-if="refreshTable" v-loading="loading" :data="menuList" row-key="id"
         :default-expand-all="isExpandAll" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
         <el-table-column prop="name" label="菜单名称" :show-overflow-tooltip="true" width="250"/>
         <el-table-column prop="icon" label="图标" align="center" width="100">
            <template #default="scope">
               <svg-icon :icon-class="scope.row.icon" />
            </template>
         </el-table-column>
         <el-table-column prop="sort" label="排序" width="60" />
         <el-table-column prop="permission" label="权限标识" :show-overflow-tooltip="true" />
         <el-table-column prop="component" label="组件路径" :show-overflow-tooltip="true" />
         <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status"/>
            </template>
         </el-table-column>
         <el-table-column label="创建时间" align="center" prop="createTime">
            <template #default="scope">
               <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
         </el-table-column>
         <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
            <template #default="scope">
               <el-button type="text" icon="Edit" @click="handleUpdate(scope.row)"
                  v-hasPermi="['system:menu:update']">修改</el-button>
               <el-button type="text" icon="Plus" @click="handleAdd(scope.row)"
                  v-hasPermi="['system:menu:create']">新增</el-button>
               <el-button type="text" icon="Delete" @click="handleDelete(scope.row)"
                  v-hasPermi="['system:menu:delete']">删除</el-button>
            </template>
         </el-table-column>
      </el-table>

      <!-- 添加或修改菜单对话框 -->
      <el-dialog :title="title" v-model="open" width="600px" :before-close="handleClose" append-to-body>
         <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
            <el-row>
               <el-col :span="24">
                  <el-form-item label="上级菜单">
                    <tree-select v-model:value="form.parentId" :options="menuOptions"  placeholder="选择上级菜单"
                                 :objMap="{ value: 'id', label: 'name', children: 'children' }"/>
                  </el-form-item>
               </el-col>
               <el-col :span="24">
                  <el-form-item label="菜单类型" prop="menuType">
                    <el-radio-group v-model="form.type">
                      <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.SYSTEM_MENU_TYPE)"
                                :key="parseInt(dict.value)" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
                    </el-radio-group>
                  </el-form-item>
               </el-col>
               <el-col :span="24" v-if="form.type !== 3">
                  <el-form-item label="菜单图标" prop="icon">
                     <el-popover placement="bottom-start" :width="460" v-model:visible="showChooseIcon"
                                 trigger="click" @show="showSelectIcon">
                        <template #reference>
                           <el-input v-model="form.icon" placeholder="点击选择图标" @click="showSelectIcon" readonly>
                              <template #prefix>
                                 <svg-icon v-if="form.icon" :icon-class="form.icon" class="el-input__icon"
                                           style="height: 32px;width: 16px;"/>
                                 <el-icon v-else style="height: 32px;width: 16px;"><search /></el-icon>
                              </template>
                           </el-input>
                        </template>
                        <icon-select ref="iconSelectRef" @selected="selected" />
                     </el-popover>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="菜单名称" prop="name">
                     <el-input v-model="form.name" placeholder="请输入菜单名称" />
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item label="显示排序" prop="sort">
                     <el-input-number v-model="form.sort" controls-position="right" :min="0" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.type !== 3">
                  <el-form-item prop="path">
                     <template #label>
                        <span>
                           <el-tooltip content="访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头" placement="top">
                              <i class="el-icon-question"/>
                           </el-tooltip>
                           路由地址
                        </span>
                     </template>
                     <el-input v-model="form.path" placeholder="请输入路由地址" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.type === 2">
                  <el-form-item prop="component">
                     <template #label>
                        <span>
                           <el-tooltip content="访问的组件路径，如：`system/user/index`，默认在`views`目录下" placement="top">
                              <i class="el-icon-question"/>
                           </el-tooltip>
                           组件路径
                        </span>
                     </template>
                     <el-input v-model="form.component" placeholder="请输入组件路径" />
                  </el-form-item>
               </el-col>
               <el-col :span="12" v-if="form.type !== 1">
                  <el-form-item>
                     <el-input v-model="form.permission" placeholder="请输入权限标识" maxlength="100" />
                     <template #label>
                        <span>
                           <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasPermi('system:user:list')`)" placement="top">
                              <i class="el-icon-question"></i>
                           </el-tooltip>
                           权限字符
                        </span>
                     </template>
                  </el-form-item>
               </el-col>
               <el-col :span="12">
                  <el-form-item>
                     <template #label>
                        <span>
                           <el-tooltip content="选择停用则路由将不会出现在侧边栏，也不能被访问" placement="top">
                              <i class="el-icon-question"/>
                           </el-tooltip>
                           菜单状态
                        </span>
                     </template>
                    <el-radio-group v-model="form.status">
                      <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)"
                                :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
                    </el-radio-group>
                  </el-form-item>
               </el-col>
            </el-row>
         </el-form>
         <template #footer>
            <div class="dialog-footer">
               <el-button type="primary" @click="submitForm">确 定</el-button>
               <el-button @click="cancel">取 消</el-button>
            </div>
         </template>
      </el-dialog>
   </div>
</template>

<script setup name="Menu">
import { addMenu, delMenu, getMenu, listMenu, updateMenu } from "@/api/system/menu";
import SvgIcon from "@/components/SvgIcon";
import IconSelect from "@/components/IconSelect";
import { SystemMenuTypeEnum, CommonStatusEnum } from '@/utils/constants'

const { proxy } = getCurrentInstance();

const menuList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const menuOptions = ref([]);
const isExpandAll = ref(false);
const refreshTable = ref(true);
const showChooseIcon = ref(false);
const iconSelectRef = ref(null);

const data = reactive({
  queryParams: {
    name: undefined,
    status: undefined
  },
  // 表单参数
  form: {},
  // 表单校验
  rules: {
    name: [
      { required: true, message: "菜单名称不能为空", trigger: "blur" }
    ],
    sort: [
      { required: true, message: "菜单顺序不能为空", trigger: "blur" }
    ],
    path: [
      { required: true, message: "路由地址不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "状态不能为空", trigger: "blur" }
    ]
  },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询菜单列表 */
function getList() {
  loading.value = true;
  listMenu(queryParams.value).then(response => {
    menuList.value = proxy.handleTree(response.data, "id");
    loading.value = false;
  });
}
/** 查询菜单下拉树结构 */
async function getTreeselect() {
  menuOptions.value = [];
  await listMenu().then(response => {
    menuOptions.value = [];
    const menu = { id: 0, name: '主类目', children: [] };
    menu.children = proxy.handleTree(response.data, "id");
    menuOptions.value.push(menu);
  });
}
/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}
/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    parentId: 0,
    name: undefined,
    icon: undefined,
    type: SystemMenuTypeEnum.DIR,
    sort: undefined,
    status: CommonStatusEnum.ENABLE
  };
  proxy.resetForm("formRef");
}
/** 展示下拉图标 */
function showSelectIcon() {
  iconSelectRef.value.reset();
  showChooseIcon.value = true;
}
/** 选择图标 */
function selected(name) {
  form.value.icon = name;
  showChooseIcon.value = false;
}
/** 关闭弹窗隐藏图标选择 */
function handleClose() {
  cancel();
  showChooseIcon.value = false;
}
/** 搜索按钮操作 */
function handleQuery() {
  getList();
}
/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}
/** 新增按钮操作 */
async function handleAdd(row) {
  reset();
  await getTreeselect();
  if (row != null && row.id) {
    form.value.parentId = row.id;
  } else {
    form.value.parentId = 0;
  }
  open.value = true;
  title.value = "添加菜单";
}
/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    refreshTable.value = true;
  });
}
/** 修改按钮操作 */
async function handleUpdate(row) {
  reset();
  await getTreeselect();
  getMenu(row.id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改菜单";
  });
}
/** 提交按钮 */
function submitForm() {
  console.log('submitForm');
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 若权限类型为目录或者菜单时，进行 path 的校验，避免后续拼接出来的路由无法跳转
      if (form.value.type === SystemMenuTypeEnum.DIR
          || form.value.type === SystemMenuTypeEnum.MENU) {
        // 如果是外链，则不进行校验
        const path = form.value.path
        if (path.indexOf('http://') === -1 || path.indexOf('https://') === -1) {
          // 父权限为根节点，path 必须以 / 开头
          if (form.value.parentId === 0 && path.charAt(0) !== '/') {
            proxy.$modal.msgSuccess('前端必须以 / 开头')
            return
          } else if (form.value.parentId !== 0 && path.charAt(0) === '/') {
            proxy.$modal.msgSuccess('前端不能以 / 开头')
            return
          }
        }
      }

      // 提交
      if (form.value.id !== undefined) {
        updateMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addMenu(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}
/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除名称为"' + row.name + '"的数据项?').then(function() {
    return delMenu(row.id);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
