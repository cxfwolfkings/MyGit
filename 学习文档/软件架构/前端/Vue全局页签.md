# 全局页签

Layout.vue：监听路由跳转事件，根据路由配置中的 `meta.keepAlive` 属性判断是否需要使用组件缓存

```vue
<template>
  <div class="app-wrapper" :class="classObj">
    <div
      v-if="device === 'mobile' && sidebar.opened"
      class="drawer-bg"
      @click="handleClickOutside"
    ></div>
    <Sidebar class="sidebar-container"></Sidebar>
    <el-scrollbar
      wrapClass="scrollbar-wrapper"
      style="height: 100%"
      id="containerScroll"
    >
      <div class="main-container">
        <NavbarNew></NavbarNew>
        <div class="app-wrap">
          <!-- 此处放置el-tabs代码 -->
          <div class="template-tabs">
            <el-tabs
              v-model="activeIndex"
              type="border-card"
              @tab-click="tabClick"
              v-if="options.length"
              @tab-remove="tabRemove"
              class="template-tab"
            >
              <el-tab-pane
                :key="item.name"
                v-for="item in options"
                :label="item.label"
                :name="item.name"
                :closable="item.closable"
                class="template-tab-pane"
              >
              </el-tab-pane>
            </el-tabs>
          </div>
          <!-- 需要缓存的组件 -->
          <keep-alive v-if="$route.meta.keepAlive" :exclude="excludeNames">
            <router-view></router-view>
          </keep-alive>
          <!-- 不需要缓存的组件 -->
          <transition v-else name="fade" mode="out-in">
            <router-view></router-view>
          </transition>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
import { NavbarNew, Sidebar } from "./components";
import ResizeMixin from "./mixin/ResizeHandler";

export default {
  name: "layout",
  data() {
    return {
      selectedMenu: "",
      excludeNames: ""
    };
  },
  components: {
    NavbarNew,
    Sidebar,
  },
  mixins: [ResizeMixin],
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar;
    },
    device() {
      return this.$store.state.app.device;
    },
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === "mobile",
      };
    },
    options() {
      return this.$store.state.options;
    },
    activeIndex: {
      get() {
        return this.$store.state.activeIndex;
      },
      set(val) {
        this.$store.commit("set_active_index", val);
      },
    },
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch("CloseSideBar", { withoutAnimation: false });
    },
    gotoRoute() {
      const base = this;
      let name = base.activeIndex.split("$$")[0];
      let path = base.activeIndex.split("$$")[1];
      base.selectedMenu = name;
      base.$router.push({ path: path });
    },
    tabClick(tab) {
      const base = this;
      base.$store.commit("set_active_index", tab.name);
      base.gotoRoute();
    },
    tabRemove(targetName) {
      const base = this;
      // 首页不可删除
      if (targetName == "首页$$/index") {
        return;
      }
      base.$store.commit("delete_tabs", targetName);
      // 设置当前激活的路由
      if (base.activeIndex === targetName) {
        if (base.options && base.options.length >= 1) {
          base.$store.commit(
            "set_active_index",
            base.options[base.options.length - 1].name
          );
          base.gotoRoute();
        }
      }
    },
  },
  watch: {
    $route(to) {
      // 监听路由发生改变
      const base = this;
      if (to.path == "/index") {
        base.selectedMenu = "首页";
      }
      let tabName = `${to.meta.title}$$${to.path}`;
      if (to.query.module) {
        tabName += `?module=${to.query.module}`;
      }
      let flag = false;
      for (let option of base.options) {
        if (option.name == tabName) {
          flag = true;
          base.$store.commit("set_active_index", tabName);
          break;
        }
      }
      if (!flag) {
        if (base.options.length >= window.config.maxTabNum) {
          base.$message({
            message: "已经达到最大打开页签数量！",
            type: "warning",
          });
          let path = base.activeIndex.split("$$")[1];
          base.$router.push({ path: path });
        } else {
          base.$store.commit("add_tabs", {
            name: to.name == "Index" ? `首页$$/index` : tabName,
            label: to.name == "Index" ? `首页` : to.meta.title,
            route: to.path,
            closable: to.name == "Index" ? false : true,
          });
          base.$store.commit("set_active_index", tabName);
        }
      }
    },
  },
  mounted: function () {
    // 刷新页面时有缓存存在，所以重新加载首页
    if (this.$router.path != "/index") {
      this.$router.push({ path: "/index" });
    }
  },
};
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
@import "src/styles/mixin.scss";
.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100%;
  width: 100%;
  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}
// 重写样式注意范围，范围大的写在前面，范围小的写在后面，否则范围小的被后面的范围大的覆盖而无效。
/deep/ .el-tabs .el-tabs__content {
  display: none;
}
/deep/ .el-tabs__nav [aria-controls="pane-首页$$/index"] span {
  display: none;
  margin-left: 5px;
}
/deep/ .el-tabs__item .el-icon-close {
  border-radius: 0 !important;
  font-size: 14px !important;
  font-weight: bold;
  line-height: 20px;
  margin-left: 8px;
}
/deep/ .el-tabs__header .el-tabs__item {
  padding-left: 12px !important;
  padding-right: 12px !important;
}
/deep/ .el-tabs__nav [aria-controls="pane-首页$$/index"] {
  padding-right: 12px;
}
/deep/ .el-tabs__nav-scroll {
  border: 0;
  height: 34px;
}
</style>
<style>
.el-input.is-disabled .el-input__inner,
.el-textarea.is-disabled .el-textarea__inner {
  background-color: #f5f7fa;
  color: black;
}
</style>
```

**keepalive Props：**

- include：接受字符串或正则表达式，这里是需要被缓存的组件名
- exclude：接受字符串或正则表达式，这里是不需要缓存的组件名
- max：接受数字，最多可以缓存多少组件实例

通过 include/exclude 双向绑定可以动态调整组件是否“刷新”。

> 注意：使用 include/exclude 属性需要给 vue 组件的 name 赋值（不是给 route 的 name 赋值），否则不生效

路由配置示例：

```js
export default new Router({
  // mode: 'history', //后端支持可开
  scrollBehavior: () => ({ y: 0 }),
  routes: [{
    path: '/projectScheme',
    component: Layout,
    name: 'ProjectScheme',
    meta: { title: '项目方案阶段', keepAlive: true },
    children: [
      { path: 'business', name: 'BusiOpportunity', component: () => import('@/views/projectScheme/busiOpportunity'), meta: { title: '商机情况', keepAlive: true } },
      { path: 'chat', name: 'ChatPlan', component: () => import('@/views/projectScheme/chatPlan'), meta: { title: '交流计划', keepAlive: true } },
      { path: 'review', name: 'SchemeReview', component: () => import('@/views/projectScheme/schemeReview'), meta: { title: '项目评审', keepAlive: true } },
      { path: 'establish', name: 'ProjectEstablish', component: () => import('@/views/projectScheme/projectEstablish'), meta: { title: '项目立项', keepAlive: true } },
      { path: 'approve', name: 'ChatApproved', component: () => import('@/views/projectScheme/chatApprove'), meta: { title: '审批待办', keepAlive: true } }
    ]
  }]
})
```

一般点击菜单时，页面组件都需要重新获取数据。我们可以在菜单路由后面添加标识参数（假设是`?r=y`），页面组件在 activated 函数中做对应处理：

```vue
<template>
  <div v-if="isLoaded">
    <!--  -->  
  </div>
</template>
<script>
export default {
  name: "",
  data() {
    isLoaded: false
  },
  activated() {
    const base = this;
    // 界面组件从缓存中获取，有些子组件即使更新了属性，视图也不会立即刷新，所以通过"isLoaded"重新加载容器
    if (base.$route.query.r == 'y') {
      base.isLoaded = false;
      // Vue.nextTick用于延迟执行一段代码（可以在created函数中获取DOM）
      // 若不加，isLoaded的赋值会被编译器优化为只有最后一个赋值有效
      // 也就是不会有删除再加载的效果
      base.$nextTick(() => {
        base.isLoaded = true;
        // 下面是初始化过程
      });
    }
  }
} 
</script>
```

> 注意：activated, deactivated 这两个生命周期钩子函数一定是要在使用了 keep-alive 组件后才会有的，否则不存在。