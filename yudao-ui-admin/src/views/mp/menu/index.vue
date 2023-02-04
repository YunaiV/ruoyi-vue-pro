<!--
MIT License

Copyright (c) 2020 www.joolun.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
  芋道源码：
  ① less 切到 scss，减少对 less 和 less-loader 的依赖
  ②
-->
<template>
  <div class="app-container">
    <doc-alert title="公众号菜单" url="https://doc.iocoder.cn/mp/menu/" />

    <!-- 搜索工作栏 -->
    <el-form ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公众号" prop="accountId">
        <el-select v-model="accountId" placeholder="请选择公众号">
          <el-option v-for="item in accounts" :key="parseInt(item.id)" :label="item.name" :value="parseInt(item.id)" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="public-account-management clearfix" v-loading="loading">
      <!--左边配置菜单-->
      <div class="left">
        <div class="weixin-hd">
            <div class="weixin-title">{{ name }}</div>
        </div>
        <div class="weixin-menu menu_main clearfix">
          <div class="menu_bottom" v-for="(item, i) of menuList" :key="i" >
            <!-- 一级菜单 -->
            <div @click="menuClick(i, item)" class="menu_item el-icon-s-fold" :class="{'active': isActive === i}">{{item.name}}</div>
            <!-- 以下为二级菜单-->
            <div class="submenu" v-if="isSubMenuFlag === i">
              <div class="subtitle menu_bottom" v-if="item.children" v-for="(subItem, k) in item.children" :key="k">
                <div class="menu_subItem" :class="{'active': isSubMenuActive === i + '' + k}" @click="subMenuClick(subItem, i, k)">
                  {{subItem.name}}
                </div>
              </div>
              <!-- 二级菜单加号， 当长度 小于 5 才显示二级菜单的加号  -->
              <div class="menu_bottom menu_addicon" v-if="!item.children || item.children.length < 5" @click="addSubMenu(i,item)">
                <i class="el-icon-plus" />
              </div>
            </div>
          </div>
          <!-- 一级菜单加号 -->
          <div class="menu_bottom menu_addicon" v-if="this.menuList.length < 3" @click="addMenu"><i class="el-icon-plus"></i></div>
        </div>
        <div class="save_div">
            <el-button class="save_btn" type="success" size="small" @click="handleSave" v-hasPermi="['mp:menu:save']">保存并发布菜单</el-button>
            <el-button class="save_btn" type="danger" size="small" @click="handleDelete" v-hasPermi="['mp:menu:delete']">清空菜单</el-button>
        </div>
      </div>
      <!--右边配置-->
      <div v-if="showRightFlag" class="right">
          <div class="configure_page">
            <div class="delete_btn">
                <el-button size="mini"  type="danger" icon="el-icon-delete" @click="deleteMenu(tempObj)">删除当前菜单</el-button>
            </div>
            <div>
                <span>菜单名称：</span>
                <el-input class="input_width" v-model="tempObj.name" placeholder="请输入菜单名称" :maxlength="nameMaxLength" clearable />
            </div>
            <div v-if="showConfigureContent">
              <div class="menu_content">
                <span>菜单标识：</span>
                <el-input class="input_width" v-model="tempObj.menuKey" placeholder="请输入菜单 KEY" clearable />
              </div>
              <div class="menu_content">
                <span>菜单内容：</span>
                <el-select v-model="tempObj.type" clearable placeholder="请选择" class="menu_option">
                    <el-option v-for="item in menuOptions" :label="item.label" :value="item.value" :key="item.value" />
                </el-select>
              </div>
              <div class="configur_content" v-if="tempObj.type === 'view'">
                <span>跳转链接：</span>
                <el-input class="input_width" v-model="tempObj.url" placeholder="请输入链接" clearable />
              </div>
              <div class="configur_content" v-if="tempObj.type === 'miniprogram'">
                <div class="applet">
                  <span>小程序的 appid ：</span>
                  <el-input class="input_width" v-model="tempObj.miniProgramAppId" placeholder="请输入小程序的appid" clearable />
                </div>
                <div class="applet">
                  <span>小程序的页面路径：</span>
                  <el-input class="input_width" v-model="tempObj.miniProgramPagePath"
                            placeholder="请输入小程序的页面路径，如：pages/index" clearable />
                </div>
                <div class="applet">
                  <span>小程序的备用网页：</span>
                  <el-input class="input_width" v-model="tempObj.url" placeholder="不支持小程序的老版本客户端将打开本网页" clearable />
                </div>
                <p class="blue">tips:需要和公众号进行关联才可以把小程序绑定带微信菜单上哟！</p>
              </div>
              <div class="configur_content" v-if="tempObj.type === 'article_view_limited'">
                <el-row>
                  <div class="select-item" v-if="tempObj && tempObj.replyArticles">
                    <wx-news :articles="tempObj.replyArticles" />
                    <el-row class="ope-row">
                        <el-button type="danger" icon="el-icon-delete" circle @click="deleteMaterial" />
                    </el-row>
                  </div>
                  <div v-else>
                    <el-row>
                      <el-col :span="24" style="text-align: center">
                        <el-button type="success" @click="openMaterial">
                          素材库选择<i class="el-icon-circle-check el-icon--right"></i>
                        </el-button>
                      </el-col>
                    </el-row>
                  </div>
                  <el-dialog title="选择图文" :visible.sync="dialogNewsVisible" width="90%">
                    <wx-material-select :objData="{type: 'news', accountId: this.accountId}" @selectMaterial="selectMaterial" />
                  </el-dialog>
                </el-row>
              </div>
              <div class="configur_content" v-if="tempObj.type === 'click' || tempObj.type === 'scancode_waitmsg'">
                <wx-reply-select :objData="tempObj.reply" v-if="hackResetWxReplySelect" />
              </div>
          </div>
        </div>
      </div>
      <!-- 一进页面就显示的默认页面，当点击左边按钮的时候，就不显示了-->
      <div v-else class="right">
          <p>请选择菜单配置</p>
      </div>
    </div>
  </div>
</template>

<script>
import WxReplySelect from '@/views/mp/components/wx-reply/main.vue'
import WxNews from '@/views/mp/components/wx-news/main.vue';
import WxMaterialSelect from '@/views/mp/components/wx-material-select/main.vue'
import { deleteMenu, getMenuList, saveMenu } from "@/api/mp/menu";
import { getSimpleAccounts } from "@/api/mp/account";

export default {
  name: 'mpMenu',
  components: {
      WxReplySelect,
      WxNews,
      WxMaterialSelect
  },
  data(){
    return {
      // ======================== 列表查询 ========================
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 查询参数
      accountId: undefined,
      name:'', // 公众号名
      menuList: {
        children: [],
      },

      // ======================== 菜单操作 ========================
      isActive: -1,// 一级菜单点中样式
      isSubMenuActive: -1, // 一级菜单点中样式
      isSubMenuFlag: -1, // 二级菜单显示标志

      // ======================== 菜单编辑 ========================
      showRightFlag: false, // 右边配置显示默认详情还是配置详情
      nameMaxLength: 0, // 菜单名称最大长度；1 级是 4 字符；2 级是 7 字符；
      showConfigureContent: true, // 是否展示配置内容；如果有子菜单，就不显示配置内容
      hackResetWxReplySelect: false, // 重置 WxReplySelect 组件
      tempObj: {}, // 右边临时变量，作为中间值牵引关系
      tempSelfObj: { // 一些临时值放在这里进行判断，如果放在 tempObj，由于引用关系，menu 也会多了多余的参数
      },
      dialogNewsVisible: false, // 跳转图文时的素材选择弹窗
      menuOptions: [{
        value: 'view',
        label: '跳转网页'
      }, {
        value: 'miniprogram',
        label: '跳转小程序'
      }, {
        value: 'click',
        label: '点击回复'
      }, {
        value: 'article_view_limited',
        label: '跳转图文消息'
      }, {
        value: 'scancode_push',
        label: '扫码直接返回结果'
      }, {
        value: 'scancode_waitmsg',
        label: '扫码回复'
      }, {
        value: 'pic_sysphoto',
        label: '系统拍照发图'
      }, {
          value: 'pic_photo_or_album',
          label: '拍照或者相册'
      }, {
          value: 'pic_weixin',
          label: '微信相册'
      }, {
          value: 'location_select',
          label: '选择地理位置'
      }],

      // 公众号账号列表
      accounts: [],
    }
  },
  created() {
    getSimpleAccounts().then(response => {
      this.accounts = response.data;
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.setAccountId(this.accounts[0].id);
      }
      // 加载数据
      this.getList();
    })
  },
  methods: {
    // ======================== 列表查询 ========================
    /** 设置账号编号 */
    setAccountId(accountId) {
      this.accountId = accountId;
      this.name = this.accounts.find(item => item.id === accountId)?.name;
    },
    getList() {
      this.loading = false;
      getMenuList(this.accountId).then(response => {
        response.data = this.convertMenuList(response.data);
        this.menuList = this.handleTree(response.data, "id");
      }).finally(() => {
        this.loading = false;
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.resetForm();
      // 默认选中第一个
      if (this.accountId) {
        this.setAccountId(this.accountId)
      }
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm();
      // 默认选中第一个
      if (this.accounts.length > 0) {
        this.setAccountId(this.accounts[0].id)
      }
      this.handleQuery()
    },
    // 将后端返回的 menuList，转换成前端的 menuList
    convertMenuList(list) {
      const menuList = [];
      list.forEach(item => {
        const menu = {
          ...item,
        };
        if (item.type === 'click' || item.type === 'scancode_waitmsg') {
          this.$delete(menu, 'replyMessageType');
          this.$delete(menu, 'replyContent');
          this.$delete(menu, 'replyMediaId');
          this.$delete(menu, 'replyMediaUrl');
          this.$delete(menu, 'replyDescription');
          this.$delete(menu, 'replyArticles');
          menu.reply = {
            type: item.replyMessageType,
            accountId: item.accountId,
            content: item.replyContent,
            mediaId: item.replyMediaId,
            url: item.replyMediaUrl,
            title: item.replyTitle,
            description: item.replyDescription,
            thumbMediaId: item.replyThumbMediaId,
            thumbMediaUrl: item.replyThumbMediaUrl,
            articles: item.replyArticles,
            musicUrl: item.replyMusicUrl,
            hqMusicUrl: item.replyHqMusicUrl,
          }
        }
        menuList.push(menu);
      });
      return menuList;
    },
    // 重置表单，清空表单数据
    resetForm() {
      // 菜单操作
      this.isActive = -1;
      this.isSubMenuActive = -1;
      this.isSubMenuFlag = -1;

      // 菜单编辑
      this.showRightFlag = false;
      this.nameMaxLength = 0;
      this.showConfigureContent = 0;
      this.hackResetWxReplySelect = true;
      this.hackResetWxReplySelect = false;
      this.tempObj = {};
      this.tempSelfObj = {};
      this.dialogNewsVisible = false;
    },

    // ======================== 菜单操作 ========================
    // 一级菜单点击事件
    menuClick(i, item) {
      // 右侧的表单相关
      this.resetEditor();
      this.showRightFlag = true; // 右边菜单
      this.tempObj = item; // 这个如果放在顶部，flag 会没有。因为重新赋值了。
      this.tempSelfObj.grand = "1"; // 表示一级菜单
      this.tempSelfObj.index = i; // 表示一级菜单索引
      this.nameMaxLength = 4
      this.showConfigureContent = !(item.children && item.children.length > 0); // 有子菜单，就不显示配置内容

      // 左侧的选中
      this.isActive = i; // 一级菜单选中样式
      this.isSubMenuFlag = i; // 二级菜单显示标志
      this.isSubMenuActive = -1; // 二级菜单去除选中样式
    },
    // 二级菜单点击事件
    subMenuClick(subItem, index, k) {
      // 右侧的表单相关
      this.resetEditor();
      this.showRightFlag = true; // 右边菜单
      this.tempObj = subItem; // 将点击的数据放到临时变量，对象有引用作用
      this.tempSelfObj.grand = "2"; // 表示二级菜单
      this.tempSelfObj.index = index; // 表示一级菜单索引
      this.tempSelfObj.secondIndex = k; // 表示二级菜单索引
      this.nameMaxLength = 7
      this.showConfigureContent = true;

      // 左侧的选中
      this.isActive = -1; // 一级菜单去除样式
      this.isSubMenuActive = index + "" + k; // 二级菜单选中样式
    },
    // 添加横向一级菜单
    addMenu() {
      const menuKeyLength = this.menuList.length;
      const addButton = {
        name: "菜单名称",
        children: [],
        reply: { // 用于存储回复内容
          'type': 'text',
          'accountId': this.accountId // 保证组件里，可以使用到对应的公众号
        }
      }
      this.$set(this.menuList, menuKeyLength, addButton)
      this.menuClick(this.menuKeyLength - 1, addButton)
    },
    // 添加横向二级菜单；item 表示要操作的父菜单
    addSubMenu(i, item) {
      // 清空父菜单的属性，因为它只需要 name 属性即可
      if (!item.children || item.children.length <= 0) {
        this.$set( item, 'children',[])
        this.$delete( item, 'type')
        this.$delete( item, 'menuKey')
        this.$delete( item, 'miniProgramAppId')
        this.$delete( item, 'miniProgramPagePath')
        this.$delete( item, 'url')
        this.$delete( item, 'reply')
        this.$delete( item, 'articleId')
        this.$delete( item, 'replyArticles')
        // 关闭配置面板
        this.showConfigureContent = false
      }

      let subMenuKeyLength = item.children.length; // 获取二级菜单key长度
      let addButton = {
        name: "子菜单名称",
        reply: { // 用于存储回复内容
          'type': 'text',
          'accountId': this.accountId // 保证组件里，可以使用到对应的公众号
        }
      }
      this.$set(item.children, subMenuKeyLength, addButton);
      this.subMenuClick(item.children[subMenuKeyLength], i, subMenuKeyLength)
    },
    // 删除当前菜单
    deleteMenu(item) {
      this.$modal.confirm('确定要删除吗?').then(() => {
        // 删除数据
        if (this.tempSelfObj.grand === "1") { // 一级菜单的删除方法
          this.menuList.splice(this.tempSelfObj.index, 1);
        } else if (this.tempSelfObj.grand === "2") {  // 二级菜单的删除方法
          this.menuList[this.tempSelfObj.index].children.splice(this.tempSelfObj.secondIndex, 1);
        }
        // 提示
        this.$modal.msgSuccess("删除成功");

        // 处理菜单的选中
        this.tempObj = {};
        this.showRightFlag = false;
        this.isActive = -1;
        this.isSubMenuActive = -1;
      }).catch(() => {});
    },

    // ======================== 菜单编辑 ========================
    handleSave() {
      this.$modal.confirm('确定要保证并发布该菜单吗？').then(() => {
        this.loading = true
        return saveMenu(this.accountId, this.convertMenuFormList());
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("发布成功");
      }).finally(() => {
        this.loading = false
      });
    },
    // 表单 Editor 重置
    resetEditor() {
      this.hackResetWxReplySelect = false // 销毁组件
      this.$nextTick(() => {
        this.hackResetWxReplySelect = true // 重建组件
      })
    },
    handleDelete() {
      this.$modal.confirm('确定要清空所有菜单吗？').then(() => {
        this.loading = true
        return deleteMenu(this.accountId);
      }).then(() => {
        this.handleQuery();
        this.$modal.msgSuccess("清空成功");
      }).catch(() => {}).finally(() => {
        this.loading = false
      });
    },
    // 将前端的 menuList，转换成后端接收的 menuList
    convertMenuFormList() {
      const menuList = [];
      this.menuList.forEach(item => {
        let menu = this.convertMenuForm(item);
        menuList.push(menu);
        // 处理子菜单
        if (!item.children || item.children.length <= 0) {
          return;
        }
        menu.children = [];
        item.children.forEach(subItem => {
          menu.children.push(this.convertMenuForm(subItem))
        })
      })
      return menuList;
    },
    // 将前端的 menu，转换成后端接收的 menu
    convertMenuForm(menu) {
      let result = {
        ...menu,
        children: undefined, // 不处理子节点
        reply: undefined, // 稍后复制
      }
      if (menu.type === 'click' || menu.type === 'scancode_waitmsg') {
        result.replyMessageType = menu.reply.type;
        result.replyContent = menu.reply.content;
        result.replyMediaId = menu.reply.mediaId;
        result.replyMediaUrl = menu.reply.url;
        result.replyTitle = menu.reply.title;
        result.replyDescription = menu.reply.description;
        result.replyThumbMediaId = menu.reply.thumbMediaId;
        result.replyThumbMediaUrl = menu.reply.thumbMediaUrl;
        result.replyArticles = menu.reply.articles;
        result.replyMusicUrl = menu.reply.musicUrl;
        result.replyHqMusicUrl = menu.reply.hqMusicUrl;
      }
      return result;
    },
    // ======================== 菜单编辑（素材选择） ========================
    openMaterial() {
      this.dialogNewsVisible = true
    },
    selectMaterial(item) {
      const articleId = item.articleId;
      const articles = item.content.newsItem;
      // 提示，针对多图文
      if (articles.length > 1) {
        this.$alert('您选择的是多图文，将默认跳转第一篇', '提示', {
          confirmButtonText: '确定'
        })
      }
      this.dialogNewsVisible = false

      // 设置菜单的回复
      this.tempObj.articleId = articleId;
      this.tempObj.replyArticles = [];
      articles.forEach(article => {
        this.tempObj.replyArticles.push({
          title: article.title,
          description: article.digest,
          picUrl: article.picUrl,
          url: article.url,
        })
      })
    },
    deleteMaterial() {
        this.$delete(this.tempObj,'articleId')
        this.$delete(this.tempObj,'replyArticles')
    },
  },
}
</script>
<!--本组件样式-->
<style lang="scss" scoped="scoped">
/* 公共颜色变量 */
.clearfix{*zoom:1;}
.clearfix::after{content: "";display: table; clear: both;}
div{
  text-align: left;
}
.weixin-hd{
  color: #fff;
  text-align: center;
  position: relative;
  bottom: 426px;
  left:0px;
  width: 300px;
  height:64px;
  background: transparent url("assets/menu_head.png") no-repeat 0 0;
  background-position: 0 0;
  background-size: 100%
}
.weixin-title{
  color:#fff;
  font-size:14px;
  width:100%;
  text-align: center;
  position:absolute;
  top: 33px;
  left: 0px;
}
.weixin-menu{
  background: transparent url("assets/menu_foot.png") no-repeat 0 0;
  padding-left: 43px;
  font-size: 12px
}
.menu_option{
  width: 40%!important;
}
.public-account-management{
  min-width: 1200px;
  width: 1200px;
  margin: 0 auto;
  .left{
      float: left;
      display: inline-block;
      width: 350px;
      height: 715px;
      background: url("assets/iphone_backImg.png") no-repeat;
      background-size: 100% auto;
      padding: 518px 25px 88px;
      position: relative;
      box-sizing: border-box;
      /*第一级菜单*/
      .menu_main{
          .menu_bottom{
              position: relative;
              float: left;
              display: inline-block;
              box-sizing: border-box;
              width: 85.5px;
              text-align: center;
              border: 1px solid #ebedee;
              background-color: #fff;
              cursor: pointer;
              &.menu_addicon{
                  height: 46px;
                  line-height: 46px;
              }
              .menu_item{
                  height: 44px;
                  line-height: 44px;
                  text-align: center;
                  box-sizing: border-box;
                  width: 100%;
                  &.active{
                      border: 1px solid #2bb673;
                  }
              }
              .menu_subItem{
                  height: 44px;
                  line-height: 44px;
                  text-align: center;
                  box-sizing: border-box;
                  &.active{
                      border: 1px solid #2bb673;
                  }
              }
          }
          i{
              color:#2bb673;
          }
          /*第二级菜单*/
          .submenu{
              position: absolute;
              width: 85.5px;
              bottom: 45px;
              .subtitle{
                  background-color: #fff;
                  box-sizing: border-box;
              }
          }
      }
      .save_div{
          margin-top: 15px;
          text-align: center;
          .save_btn{
              bottom: 20px;
              left: 100px;
          }
      }
  }
  /*右边菜单内容*/
  .right {
      float: left;
      width: 63%;
      background-color: #e8e7e7;
      padding: 20px;
      margin-left: 20px;
      -webkit-box-sizing: border-box;
      box-sizing: border-box;
      .configure_page {
          .delete_btn {
              text-align: right;
              margin-bottom: 15px;
          }
          .menu_content {
              margin-top: 20px;
          }
          .configur_content {
              margin-top: 20px;
              background-color: #fff;
              padding: 20px 10px;
              border-radius: 5px
          }
          .blue {
              color:#29b6f6;
              margin-top: 10px;
          }
          .applet{
              margin-bottom: 20px;
              span{
                  width: 20%;
              }
          }
          .input_width {
            width: 40%;
          }
          .material{
              .input_width{
                  width: 30%;
              }
              .el-textarea{
                  width: 80%
              }
          }
      }
  }
  .el-input {
    width: 70%;
    margin-right: 2%;
  }
}
</style>
<!--素材样式-->
<style lang="scss" scoped>
.pagination {
    text-align: right;
    margin-right: 25px;
}
.select-item {
    width: 280px;
    padding: 10px;
    margin: 0 auto 10px auto;
    border: 1px solid #eaeaea;
}
.select-item2 {
    padding: 10px;
    margin: 0 auto 10px auto;
    border: 1px solid #eaeaea;
}
.ope-row {
    padding-top: 10px;
    text-align: center;
}
.item-name {
    font-size: 12px;
    overflow: hidden;
    text-overflow:ellipsis;
    white-space: nowrap;
    text-align: center;
}
</style>
