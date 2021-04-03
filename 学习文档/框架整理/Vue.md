# Vue

1. 简介
   - [安装](#安装)
   - [模板语法](#模板语法)
   - [指令](#指令)
   - [过滤器](#过滤器)
   - [计算属性](#计算属性)
   - [监视](#监视)
   - [动态操作根级响应式属性](#动态操作根级响应式属性)
   - [条件语句](#条件语句)
   - [循环语句](#循环语句)
   - [样式绑定](#样式绑定)
   - [事件处理](#事件处理)
   - [内容分发](#内容分发)
   - [动画](#动画)
   - [混入](#混入)
2. 实战
   - [事件总线](#事件总线)
   - [i18n](#i18n)
   - [wappalyzer](#wappalyzer)
   - [Vuex](#Vuex)
   - [ElementUI插件](#ElementUI插件)
   - [mint-ui](#mint-ui)
   - [构建](#构建)
3. 总结
   - [常见错误](#常见错误)
     - NavigationDuplicated
     - watch不触发、不生效



## 简介

### 安装

由于 npm 安装速度慢，建议使用淘宝定制的cnpm（gzip 压缩支持）命令行工具代替默认的 npm：

```sh
npm install -g cnpm --registry=https://registry.npm.taobao.org

# 最新稳定版
cnpm install vue
```

在用 Vue.js 构建大型应用时推荐使用 NPM 安装。Vue.js 提供一个官方命令行工具`vue-cli`，可用于快速搭建大型单页应用。

```sh
# 全局安装 vue-cli
cnpm install --global vue-cli
# 创建一个基于 webpack 模板的新项目
vue init webpack my-project
# 下面需要进行一些配置，默认回车即可
```

进入项目，安装并运行：

```sh
cd my-project
cnpm install
cnpm run dev
```

成功执行以上命令后访问 `http://localhost:xxxx/`

>注意：Vue.js 不支持 IE8 及其以下 IE 版本。

## 自定义指令

除了默认设置的核心指令（v-model 和 v-show），Vue 也允许注册自定义指令。可以**全局注册**、**局部注册**。

下面我们注册一个全局指令 `v-focus`，该指令的功能是在页面加载时，元素获得焦点：

```html
<div id="app">
  <p>页面载入时，input 元素自动获取焦点：</p>
  <input v-focus>
</div>
<script>
  // 注册一个全局自定义指令 v-focus
  Vue.directive('focus', {
    // 当绑定元素插入到 DOM 中。
    inserted: function (el) {
      // 聚焦元素
      el.focus()
    }
  })
  // 创建根实例
  new Vue({
    el: '#app'
  })
</script>
```

我们也可以在实例使用 directives 选项来注册局部指令，这样指令只能在这个实例中使用：

```html
<div id="app">
  <p>页面载入时，input 元素自动获取焦点：</p>
  <input v-focus>
</div>
<script>
// 创建根实例
new Vue({
  el: '#app',
  directives: {
    // 注册一个局部的自定义指令 v-focus
    focus: {
      // 指令的定义
      inserted: function (el) {
        // 聚焦元素
        el.focus()
      }
    }
  }
})
</script>
```

自定义指令的选项是由几个钩子函数组成的，每个都是可选的。

- **bind**: 只调用一次，指令第一次绑定到元素时调用，用这个钩子函数可以定义一个在绑定时执行一次的初始化动作。
- **inserted**: 被绑定元素插入父节点时调用（父节点存在即可调用，不必存在于 document 中）。
- **update**: 被绑定元素所在的模板更新时调用，而不论绑定值是否变化。通过比较更新前后的绑定值，可以忽略不必要的模板更新（详细的钩子函数参数见下）。
- **componentUpdated**: 被绑定元素所在模板完成一次更新周期时调用。
- **unbind**: 只调用一次， 指令与元素解绑时调用。

**钩子函数参数：**

- el: 指令所绑定的元素，可以用来直接操作 DOM。
- binding: 一个对象，包含以下属性：
  - name: 指令名，不包括 v- 前缀。
  - value: 指令的绑定值，例如：v-my-directive="1 + 1", value 的值是 2。
  - oldValue: 指令绑定的前一个值，仅在 update 和 componentUpdated 钩子中可用。无论值是否改变都可用。
  - expression: 绑定值的字符串形式。 例如 `v-my-directive="1 + 1"`，expression 的值是 "1 + 1"。
  - arg: 传给指令的参数。例如 v-my-directive:foo，arg 的值是 "foo"。
  - modifiers: 一个包含修饰符的对象。例如：`v-my-directive.foo.bar`，修饰符对象modifiers的值是 `{ foo: true, bar: true }`。
  - vnode: Vue 编译生成的虚拟节点，查阅 VNode API 了解更多详情。
  - oldVnode: 上一个虚拟节点，仅在 update 和 componentUpdated 钩子中可用。

### 过滤器

Vue允许你自定义过滤器，被用作一些常见的文本格式化。由“管道符”指示；过滤器函数接受表达式的值作为第一个参数。

```html
<div id="app">
  {{ message | capitalize }}
</div>
<script>
  new Vue({
    el: '#app',
    data: {
      message: 'runoob'
    },
    filters: {
      capitalize: function (value) {
        if (!value) return ''
        value = value.toString()
        return value.charAt(0).toUpperCase() + value.slice(1)
      }
    }
  })
</script>
```

过滤器可以串联：`{{ message | filterA | filterB }}`。过滤器是 JavaScript 函数，因此可以接受参数：`{{ message | filterA('arg1', arg2) }}`。这里，message 是第一个参数，字符串 'arg1' 将传给过滤器作为第二个参数，arg2 表达式的值将被求值然后传给过滤器作为第三个参数。

### 计算属性

模板内的表达式常用于简单的运算，当其过长或逻辑复杂时，会难以维护，计算属性就是用于解决该问题的。

计算属性关键词: `computed`。计算属性在处理一些复杂逻辑时是很有用的。

示例：反转字符串

```html
<div id="app">
  <p>原始字符串: {{ message }}</p>
  <p>计算后反转字符串: {{ reversedMessage }}</p>
  <p>使用方法后反转字符串: {{ reversedMessage2() }}</p>
</div>
<script>
var cnt=1;
var vm = new Vue({
  el: '#app',
  data: {
    message: 'Runoob!'
  },
  computed: {
    // 计算属性的 getter
    reversedMessage: function () {
      // `this` 指向 vm 实例
      cnt+=1;
      return cnt+this.message.split('').reverse().join('')
    }
  },
  methods: {
    reversedMessage2: function () {
      cnt+=1;
      return cnt+this.message.split('').reverse().join('')
    }
  }
})
</script>
```

声明一个计算属性reversedMessage。每一个计算属性都包含－个`getter`和`setter`，上面的示例是计算属性的默认用法，只是利用`getter`来读取。提供的函数将用作属性vm.reversedMessage的getter。vm.reversedMessage依赖于vm.message，在vm.message发生改变时，vm.reversedMessage也会更新。

**computed vs methods:**

我们可以使用 `methods` 来替代 `computed`，效果上两个都是一样的，但是 `computed` 是基于它的依赖缓存，只有相关依赖发生改变时才会重新取值。而使用 `methods`，在重新渲染的时候，函数总会重新调用执行。

可以说使用 `computed` 性能会更好，但是如果你不希望缓存，你可以使用 `methods` 属性。

**setter：**

```js
var vm = new Vue({
  el: '#app',
  data: {
    name: 'Google',
    url: 'http://www.google.com'
  },
  computed: {
    site: {
      // getter
      get: function () {
        return this.name + ' ' + this.url
      },
      // setter
      set: function (newValue) {
        var names = newValue.split(' ')
        this.name = names[0]
        this.url = names[names.length - 1]
      }
    }
  }
})
// 调用 setter， vm.name 和 vm.url 也会被对应更新
vm.site = '菜鸟教程 http://www.runoob.com';
document.write('name: ' + vm.name);
document.write('<br>');
document.write('url: ' + vm.url);
```

计算属性除了简单的文本插值外，还经常用于动态地设置元素的样式名称`class`和内联样式`style`。当使用组件时，计算属性也经常用来动态传递`props`。

计算属性还有两个很实用的小技巧容易被忽略：

- 一是计算属性可以依赖其他计算属性；
- 二是计算属性不仅可以依赖当前Vue实例的数据，还可以依赖其他实例的数据

### 监视

我们可以通过 `watch` 来响应数据的变化。以下实例通过使用 `watch` 实现计数器：

```html
<div id = "computed_props">
  千米 : <input type = "text" v-model = "kilometers">
  米 : <input type = "text" v-model = "meters">
</div>
<p id="info"></p>
<script type = "text/javascript">
  var vm = new Vue({
    el: '#computed_props',
    data: {
      kilometers : 0,
      meters:0
    },
    methods: {},
    computed: {},
    watch: {
      kilometers: function(val) {
        this.kilometers = val;
        this.meters = this.kilometers * 1000
      },
      meters: function (val) {
        this.kilometers = val/ 1000;
        this.meters = val;
      }
    }
  });
  // $watch 是一个实例方法
  vm.$watch('kilometers', function (newValue, oldValue) {
    // 这个回调将在 vm.kilometers 改变后调用
    document.getElementById ("info").innerHTML = "修改前值为: " + oldValue + "，修改后值为: " + newValue;
  })
</script>
```

`watch` 可以对（单个）变量进行监视，也可以深度监视；计算属性 `computed` 可以监视多个值，并且指定返回数据。

Vue 可以添加数据动态响应接口。例如以下实例，我们通过使用 `$watch` 属性来实现数据的监听，`$watch` 必须添加在 Vue 实例之外才能实现正确的响应。

实例中通过点击按钮计数器会加 1。`setTimeout` 设置 10 秒后计算器的值加上 20 。

```html
<div id = "app">
  <p style = "font-size:25px;">计数器: {{ counter }}</p>
  <button @click = "counter++" style = "font-size:25px;">点我</button>
</div>
<script type = "text/javascript">
var vm = new Vue({
  el: '#app',
  data: {
    counter: 1
  }
});
vm.$watch('counter', function(nval, oval) {
  alert('计数器值的变化 :' + oval + ' 变为 ' + nval + '!');
});
setTimeout(
  function(){
    vm.counter += 20;
  },10000
);
</script>
```

### 动态操作根级响应式属性

<b style="color:red">Vue 不允许在已经创建的实例上动态添加新的根级响应式属性。</b>

Vue 不能检测到对象属性的添加或删除，最好的方式就是在初始化实例前声明根级响应式属性，哪怕只是一个空值。

如果我们需要在运行过程中实现属性的添加或删除，则可以使用全局 Vue，`Vue.set` 和 `Vue.delete` 方法。

`Vue.set` 方法用于设置对象的属性，它可以解决 Vue 无法检测添加属性的限制，语法格式如下：

```js
Vue.set( target, key, value )
```

参数说明：

- target: 可以是对象或数组
- key: 可以是字符串或数字
- value: 可以是任何类型

```html
<div id = "app">
   <p style = "font-size:25px;">计数器: {{ products.id }}</p>
   <button @click = "products.id++" style = "font-size:25px;">点我</button>
</div>
<script type = "text/javascript">
var myproduct = { "id":1, name:"book", "price":"20.00" };
  var vm = new Vue({
  el: '#app',
  data: {
    counter: 1,
    products: myproduct
  }
});
vm.products.qty = "1";
console.log(vm);
vm.$watch('counter', function(nval, oval) {
  alert('计数器值的变化 :' + oval + ' 变为 ' + nval + '!');
});
</script>
```

在以上实例中，在开始时创建了一个变量 myproduct，该变量赋值给了 Vue 实例的 data 对象，如果我们想给 myproduct 数组添加一个或多个属性，我们可以在 Vue 实例创建后使用 `vm.products.qty = "1";` 代码。

查看控制台输出：

![x](http://viyitech.cn/public/images/59.png)

在产品中添加了数量属性 qty，但是 `get/set` 方法只可用于 id，name 和 price 属性，却不能在 qty 属性中使用。

我们不能通过添加 Vue 对象来实现响应，Vue 主要在开始时创建所有属性。如果我们要实现这个功能，可以通过 `Vue.set` 来实现：

```html
<div id = "app">
  <p style = "font-size:25px;">计数器: {{ products.id }}</p>
  <button @click = "products.id++" style = "font-size:25px;">点我</button>
</div>
<script type = "text/javascript">
var myproduct = {"id":1, name:"book", "price":"20.00"};
var vm = new Vue({
  el: '#app',
  data: {
    counter: 1,
    products: myproduct
  }
});
Vue.set(myproduct, 'qty', 1);
console.log(vm);
vm.$watch('counter', function(nval, oval) {
  alert('计数器值的变化 :' + oval + ' 变为 ' + nval + '!');
});
</script>
```

从控制台输出的结果可以看出 `get/set` 方法可用于qty 属性。

`Vue.delete` 用于删除动态添加的属性 语法格式：

```js
Vue.delete( target, key )
```

参数说明：

- target: 可以是对象或数组
- key: 可以是字符串或数字

### 条件语句

**v-if**

```html
<div id="app">
  <p v-if="seen">现在你看到我了</p>
  <template v-if="ok">
    <h1>菜鸟教程</h1>
    <p>学的不仅是技术，更是梦想！</p>
    <p>哈哈哈，打字辛苦啊！！！</p>
  </template>
</div>
<script>
new Vue({
  el: '#app',
  data: {
    seen: true,
    ok: true
  }
})
</script>
```

在字符串模板中，如 Handlebars ，我们得像这样写一个条件块：

```html
<!-- Handlebars 模板 -->
{{#if ok}}
  <h1>Yes</h1>
{{/if}}
```

**v-else-if & v-else**

```html
<div id="app">
  <div v-if="type === 'A'">A</div>
  <div v-else-if="type === 'B'">B</div>
  <div v-else-if="type === 'C'">C</div>
  <div v-else>Not A/B/C</div>
</div>
```

**v-show**

`v-if` 是真实的条件渲染，也是惰性的：如果在初始渲染时条件为假，则什么也不做——在条件第一次变为真时才开始局部编译（编译会被缓存起来）。动态添加，当值为 false 时，是完全移除该元素，即 dom 树中不存在该元素。

相比之下，`v-show` 简单得多——元素始终被编译并保留，只是简单地基于 CSS 切换。

一般来说，`v-if` 有更高的切换消耗而 `v-show` 有更高的初始渲染消耗。因此，如果需要频繁切换 `v-show` 较好，如果在运行时条件不大可能改变 `v-if` 较好。

### 循环语句

**v-for**

循环对象：

```html
<div id="app">
  <ul>
    <li v-for="(value, key, index) in object">
      {{ index }}. {{ key }} : {{ value }}
    </li>
  </ul>
</div>
```

循环整数：

```html
<div id="app">
  <ul>
    <li v-for="n in 10">{{ n }}</li>
  </ul>
</div>
```

循环数组：

```html
<div id="app">
  <ul>
    <li v-for="n in [1,3,5]">{{ n }}</li>
  </ul>
</div>
```

`v-for` 默认行为：尝试不改变整体，而是替换元素，所以你需要提供一个 `key` 的特殊属性：

```html
<div v-for="item in items" :key="item.id">{{ item.text }}</div>
```

不仅如此，在迭代属性输出之前，v-for会对属性进行升序排序输出！

遍历对象的时候可以处理嵌套：

```html
<div id="app">
  <ul>
    <li v-for="(value,key,index) in object">
      <p v-if="typeof value !='object'">{{value}}....{{ index }}</p>
      <p v-else>{{key}}....{{index}}</p>
      <ul v-if="typeof value == 'object'">
        <li v-for="(value, key, index) in value">
          {{key}}:{{value}}....{{ index }}
        </li>
      </ul>
    </li>
  </ul>
</div>
```

九九乘法是程序员的最爱:

```html
<div id="app">
  <div v-for="n in 9">
    <b v-for="m in n">
      {{m}}*{{n}}={{m*n}}
    </b>
  </div>
</div>
```



### 样式绑定

`v-bind` 的主要用法是动态更新HTML元素上的属性。

**class属性绑定**

我们可以为 `v-bind:class` 设置一个对象，从而动态的切换 class；也可以直接绑定数据里的一个对象

```html
<style>
.base {
  width: 100px;
  height: 100px;
}
.active {
    background: green;
}
.text-danger {
    background: red;
}
</style>
<div id="app">
  <div class="static" v-bind:class="{ active: isActive, 'text-danger': hasError }"></div>
  <div v-bind:class="classObject"></div>
  <!-- 计算属性 -->
  <div v-bind:class="classCompObject"></div>
  <!-- 数组 -->
  <div v-bind:class="[activeClass, errorClass]"></div>
  <!-- 三元表达式 -->
  <div v-bind:class="[errorClass, isActive ? activeClass : '']"></div>
</div>
<script>
new Vue({
  el: '#app',
  data: {
    isActive: true,
    hasError: true,
    error: {
      value: true,
      type: 'fatal'
    }
    classObject: {
      active: true,
      'text-danger': true
    },
    activeClass: 'active',
    errorClass: 'text-danger'
  },
  computed: {
    classCompObject: function () {
      return {
        base: true,
        active: this.isActive && !this.error.value,
        'text-danger': this.error.value && this.error.type === 'fatal',
      }
    }
  }
})
</script>
```

如果直接在自定义组件上使用 `class` 或 `:class`，样式规则会直接应用到这个组件的根元素上，这种用法仅适用于自定义组件的最外层是一个根元素，否则会无效，当不满足这种条件或需要给具体的子元素设置类名时，应当使用组件的props来传递。这些用法同样适用于绑定内联样式style的内容。

**style（内联样式）**

css属性名称使用 **驼峰命名(camelCase)** 或 **短横分隔命名(kebab-case)**。也可以直接绑定到一个样式对象，让模板更清晰

```html
<div id="app">
  <div v-bind:style="{ color: activeColor, fontSize: fontSize + 'px' }">菜鸟教程</div>
  <div v-bind:style="styleObject">菜鸟教程</div>
  <!-- 可以使用数组将多个样式对象应用到一个元素上 -->
  <div v-bind:style="[baseStyles, overridingStyles]">菜鸟教程</div>
</div>

<script>
new Vue({
  el: '#app',
  data: {
    activeColor: 'green',
    fontSize: 30,
    styleObject: {
      color: 'green',
      fontSize: '30px'
    },
    baseStyles: {
      color: 'green',
      fontSize: '30px'
    },
    overridingStyles: {
      'font-weight': 'bold'
    }
  }
})
</script>
```

**注意：**当 `v-bind:style` 使用需要特定前缀的CSS属性时，如transform，Vue.js会自动侦测并添加相应的前缀。

Mustache（双大括号写法）不能在 HTML 属性中使用，应使用 `v-bind` 指令，这对布尔值的属性也有效——如果条件被求值为 false 的话该属性会被移除。

默认情况下标签自带属性的值是固定的，在为了能够动态的给这些属性添加值，可以使用`v-bind:你要动态变化的值="表达式"`；`v-bind` 用于绑定属性和数据，其缩写为":"，也就是 `v-bind:id === :id`。`v-model` 用在表单控件上的，用于实现双向数据绑定，所以如果你用在除了表单控件以外的标签是没有任何效果的。

动态调节示例：

```html
<div id="dynamic">  
  <div v-bind:style="{color: 'red', fontSize: fontSize + 'px'}">可以动态调节</div>  
  <div v-bind:style="objectStyle"> 不可以动态调节</div>
  {{fontSize}}
  <button @click="++fontSize">+</button>
  <button @click="--fontSize">-</button>
</div>
<script>
var app = new Vue({
  el: '#dynamic',
  data: {
    fontSize: 20,
    objectStyle: {
      color: 'green',
      fontSize: this.fontSize + 'px'
    }
  }
})
</script>
```

### 事件处理

事件监听可以使用 `v-on` 指令

```html
<div id="app">
  <button v-on:click="counter += 1">增加 1</button>
  <p>这个按钮被点击了 {{ counter }} 次。</p>
</div>
```

Vue 提供了一个特殊变量`$event`，用于访问原生 DOM 事件。

Vue.js 为 `v-on` 提供了事件修饰符来处理DOM事件细节，如：`event.preventDefault()` 或 `event.stopPropagation()`。

Vue.js通过由点(.)表示的指令后缀来调用修饰符。

- **.stop**：就是js中的`event.stopPropagation()`的缩写，它是用来阻止冒泡的
- **.prevent**：就是js中`event.preventDefault()`的缩写，它是用来阻止默认行为的；
- **.capture**：在传递的父子事件中，加了这个，无论先点哪个，都先执行这个。
  
  >捕获事件和冒泡事件（默认）是两种事件流，事件捕获是从document到触发事件的那个元素；冒泡事件是从下向上的触发事件；

- **.self**：就是防止父元素（设置了该修饰符）的子元素的事件冒泡到父元素上，只有本身触发时才会执行事件处理程序（函数）；
- **.once**：每次页面重载后只会执行一次。

```html
<!-- 阻止单击事件冒泡 -->
<a v-on:click.stop="doThis"></a>
<!-- 提交事件不再重载页面 -->
<form v-on:submit.prevent="onSubmit"></form>
<!-- 修饰符可以串联  -->
<a v-on:click.stop.prevent="doThat"></a>
<!-- 只有修饰符 -->
<form v-on:submit.prevent></form>
<!-- 添加事件侦听器时使用事件捕获模式 -->
<div v-on:click.capture="doThis">...</div>
<!-- 只当事件在该元素本身（而不是子元素）触发时触发回调 -->
<div v-on:click.self="doThat">...</div>
<!-- click 事件只能点击一次，2.1.4版本新增 -->
<a v-on:click.once="doThis"></a>
```

Vue允许为 `v-on` 在监听键盘事件时添加按键修饰符：

```html
<!-- 只有在 keyCode 是 13 时调用 vm.submit() -->
<input v-on:keyup.13="submit">
```

记住所有的 keyCode 比较困难，所以Vue为最常用的按键提供了别名：

```html
<!-- 同上 -->
<input v-on:keyup.enter="submit">
<!-- 缩写语法 -->
<input @keyup.enter="submit">
```

全部的按键别名：

- .enter
- .tab
- .delete （捕获“删除”和“退格”键）
- .esc
- .space
- .up
- .down
- .left
- .right
- .ctrl
- .alt
- .shift
- .meta

当绑定 `v-on:click` 事件时，想传入参数同时也传入当前元素：

```html
<button v-on:click="say('hi',$event)">say hi</button>
<script>
var app = new Vue({
  el: '###',
  methods:{
    say: function(message, e){
      alert(message);
      console.log(e.currentTarget);
    }
  }
}
</script>
```



### 内容分发

**slot**：通过 `$slots` 可以访问某个具名slot，`this.$slots.default` 包括了所有没有被包含在具名slot中的节点。

### 动画

**CSS 过渡**

Vue 在插入、更新或者移除 DOM 时，提供多种不同方式的应用过渡效果。提供内置过渡封装组件，用于包裹要实现过渡效果的组件。

可以通过以下实例来理解 Vue 的过渡是如何实现的：

```html
<style>
  /* 可以设置不同的进入和离开动画 */
  /* 设置持续时间和动画函数 */
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 2s
  }
  /* .fade-leave-active, 2.1.8 版本以下 */
  .fade-enter,
  .fade-leave-to {
    opacity: 0
  }
</style>
<div id="databinding">
  <button v-on:click="show = !show">点我</button>
  <transition name="fade">
    <p v-show="show" v-bind:style="styleobj">动画实例</p>
  </transition>
</div>
<script type="text/javascript">
var vm = new Vue({
  el: '#databinding',
  data: {
    show: true,
    styleobj: {
      fontSize: '30px',
      color: 'red'
    }
  },
  methods: {}
});
</script>
```

过渡其实就是一个淡入淡出的效果。Vue在元素显示与隐藏的过渡中，提供了 6 个 class 来切换：

- **v-enter**：定义进入过渡的开始状态。在元素被插入之前生效，在元素被插入之后的下一帧移除。
- **v-enter-active**：定义进入过渡生效时的状态。在整个进入过渡的阶段中应用，在元素被插入之前生效，在过渡/动画完成之后移除。这个类可以被用来定义进入过渡的过程时间，延迟和曲线函数。
- **v-enter-to**: 2.1.8版及以上定义进入过渡的结束状态。在元素被插入之后下一帧生效（与此同时 v-enter 被移除），在过渡/动画完成之后移除。
- **v-leave**: 定义离开过渡的开始状态。在离开过渡被触发时立刻生效，下一帧被移除。
- **v-leave-active**：定义离开过渡生效时的状态。在整个离开过渡的阶段中应用，在离开过渡被触发时立刻生效，在过渡/动画完成之后移除。这个类可以被用来定义离开过渡的过程时间，延迟和曲线函数。
- **v-leave-to**: 2.1.8版及以上定义离开过渡的结束状态。在离开过渡被触发之后下一帧生效（与此同时 v-leave 被删除），在过渡/动画完成之后移除。

![x](http://viyitech.cn/public/images/2.png)

对于这些在过渡中切换的类名来说，如果你使用一个没有名字的 `<transition>`，则 v- 是这些类名的默认前缀。如果你使用了 `<transition name="my-transition">`，那么 v-enter 会替换为 my-transition-enter。

`v-enter-active` 和 `v-leave-active` 可以控制进入/离开过渡的不同的缓和曲线。

**CSS 动画**

CSS 动画用法类似 CSS 过渡，但是在动画中 `v-enter` 类名在节点插入 DOM 后不会立即删除，而是在 `animationend` 事件触发时删除。

```html
<style>
.bounce-enter-active {
  animation: bounce-in .5s;
}
.bounce-leave-active {
  animation: bounce-in .5s reverse;
}
@keyframes bounce-in {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.5);
  }
  100% {
    transform: scale(1);
  }
}
</style>
<div id = "databinding">
  <button v-on:click = "show = !show">点我</button>
  <transition name="bounce">
    <p v-if="show">菜鸟教程 -- 学的不仅是技术，更是梦想！！！</p>
  </transition>
</div>
<script type = "text/javascript">
new Vue({
  el: '#databinding',
  data: {
    show: true
  }
})
</script>
```

我们可以通过以下特性来自定义过渡类名：

- enter-class
- enter-active-class
- enter-to-class (2.1.8+)
- leave-class
- leave-active-class
- leave-to-class (2.1.8+)

自定义过渡的类名优先级高于普通的类名，这样就能很好的与第三方（如：animate.css）的动画库结合使用。

```html
<script src="https://cdn.staticfile.org/vue/2.2.2/vue.min.js"></script>
<link href="https://cdn.jsdelivr.net/npm/animate.css@3.5.1" rel="stylesheet" type="text/css">
<div id = "databinding">
  <button v-on:click = "show = !show">点我</button>
  <transition
    name="custom-classes-transition"
    enter-active-class="animated tada"
    leave-active-class="animated bounceOutRight">
    <p v-if="show">菜鸟教程 -- 学的不仅是技术，更是梦想！！！</p>
  </transition>
</div>
<script type = "text/javascript">
new Vue({
  el: '#databinding',
  data: {
    show: true
  }
})
</script>
```

**同时使用过渡和动画**

Vue 为了知道过渡的完成，必须设置相应的事件监听器。它可以是 `transitionend` 或 `animationend`，这取决于给元素应用的 CSS 规则。如果你使用其中任何一种，Vue 能自动识别类型并设置监听。

但是，在一些场景中，你需要给同一个元素同时设置两种过渡动效，比如 `animation` 很快的被触发并完成了，而 `transition` 效果还没结束。在这种情况中，你就需要使用 `type` 特性并设置 `animation` 或 `transition` 来明确声明你需要 Vue 监听的类型。

在很多情况下，Vue 可以自动得出过渡效果的完成时机。默认情况下，Vue 会等待其在过渡效果的根元素的第一个 `transitionend` 或 `animationend` 事件。然而也可以不这样设定——比如，我们可以拥有一个精心编排的一系列过渡效果，其中一些嵌套的内部元素相比于过渡效果的根元素有延迟的或更长的过渡效果。

在这种情况下你可以用 `<transition>` 组件上的 `duration` 属性定制一个显性的过渡持续时间（以毫秒计）：

```html
<transition :duration="1000">...</transition>
```

你也可以定制进入和移出的持续时间：

```html
<transition :duration="{ enter: 500, leave: 800 }">...</transition>
```

**javaScript钩子**

HTML:

```html
<transition
  v-on:before-enter="beforeEnter"
  v-on:enter="enter"
  v-on:after-enter="afterEnter"
  v-on:enter-cancelled="enterCancelled"
  v-on:before-leave="beforeLeave"
  v-on:leave="leave"
  v-on:after-leave="afterLeave"
  v-on:leave-cancelled="leaveCancelled">
  <!-- ... -->
</transition>
```

javascript:

```js
// ...
methods: {
  // --------
  // 进入中
  // --------

  beforeEnter: function (el) {
    // ...
  },
  // 此回调函数是可选项的设置
  // 与 CSS 结合时使用
  enter: function (el, done) {
    // ...
    done()
  },
  afterEnter: function (el) {
    // ...
  },
  enterCancelled: function (el) {
    // ...
  },
  // --------
  // 离开时
  // --------
  beforeLeave: function (el) {
    // ...
  },
  // 此回调函数是可选项的设置
  // 与 CSS 结合时使用
  leave: function (el, done) {
    // ...
    done()
  },
  afterLeave: function (el) {
    // ...
  },
  // leaveCancelled 只用于 v-show 中
  leaveCancelled: function (el) {
    // ...
  }
}
```

这些钩子函数可以结合 CSS `transitions/animations` 使用，也可以单独使用。

当只用 javaScript 过渡的时候，在 `enter` 和 `leave` 中必须使用 `done` 进行回调。否则，它们将被同步调用，过渡会立即完成。

推荐对于仅使用 javaScript 过渡的元素添加 `v-bind:css="false"`，Vue 会跳过 CSS 的检测。这也可以避免过渡过程中 CSS 的影响。

一个使用 Velocity.js 的简单例子：

```html
<script src="https://cdn.staticfile.org/vue/2.2.2/vue.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/velocity/1.2.3/velocity.min.js"></script>
<div id = "databinding">
  <button v-on:click = "show = !show">点我</button>
  <transition
    v-on:before-enter="beforeEnter"
    v-on:enter="enter"
    v-on:leave="leave"
    v-bind:css="false">
    <p v-if="show">菜鸟教程 -- 学的不仅是技术，更是梦想！！！</p>
  </transition>
</div>
<script type = "text/javascript">
new Vue({
  el: '#databinding',
  data: {
    show: false
  },
  methods: {
    beforeEnter: function (el) {
      el.style.opacity = 0
      el.style.transformOrigin = 'left'
    },
    enter: function (el, done) {
      Velocity(el, { opacity: 1, fontSize: '1.4em' }, { duration: 300 })
      Velocity(el, { fontSize: '1em' }, { complete: done })
    },
    leave: function (el, done) {
      Velocity(el, { translateX: '15px', rotateZ: '50deg' }, { duration: 600 })
      Velocity(el, { rotateZ: '100deg' }, { loop: 2 })
      Velocity(el, {
        rotateZ: '45deg',
        translateY: '30px',
        translateX: '30px',
        opacity: 0
      }, { complete: done })
    }
  }
})
</script>
```

**初始渲染的过渡**

可以通过 appear 特性设置节点在初始渲染的过渡

```html
<transition appear>
  <!-- ... -->
</transition>
```

这里默认和进入/离开过渡一样，同样也可以自定义 CSS 类名。

```html
<transition
  appear
  appear-class="custom-appear-class"
  appear-to-class="custom-appear-to-class" (2.1.8+)
  appear-active-class="custom-appear-active-class"
>
  <!-- ... -->
</transition>
```

自定义 javaScript 钩子：

```html
<transition
  appear
  v-on:before-appear="customBeforeAppearHook"
  v-on:appear="customAppearHook"
  v-on:after-appear="customAfterAppearHook"
  v-on:appear-cancelled="customAppearCancelledHook"
>
  <!-- ... -->
</transition>
```

**多个元素的过渡**

我们可以设置多个元素的过渡。当有相同标签名的元素切换时，需要通过 `key` 特性设置唯一的值来标记以让 Vue 区分它们，否则 Vue 为了效率只会替换相同标签内部的内容。

```html
<transition>
  <table v-if="items.length > 0">
    <!-- ... -->
  </table>
  <p v-else>抱歉，没有找到您查找的内容。</p>
</transition>
```

如下实例：

```html
<transition>
  <button v-if="isEditing" key="save">Save</button>
  <button v-else key="edit">Edit</button>
</transition>
```

在一些场景中，也可以通过给同一个元素的 key 特性设置不同的状态来代替 `v-if` 和 `v-else`，上面的例子可以重写为：

```html
<transition>
  <button v-bind:key="isEditing">
    {{ isEditing ? 'Save' : 'Edit' }}
  </button>
</transition>
```

使用多个 `v-if` 的多个元素的过渡可以重写为绑定了动态属性的单个元素过渡。例如：

```html
<transition>
  <button v-if="docState === 'saved'" key="saved">Edit</button>
  <button v-if="docState === 'edited'" key="edited">Save</button>
  <button v-if="docState === 'editing'" key="editing">Cancel</button>
</transition>
```

可以重写为：

```html
<transition>
  <button v-bind:key="docState">
    {{ buttonMessage }}
  </button>
</transition>
<script>  
// ...
new Vue ({
  computed: {
    buttonMessage: function () {
      switch (this.docState) {
        case 'saved': return 'Edit'
        case 'edited': return 'Save'
        case 'editing': return 'Cancel'
      }
    }
  }
})
</script>
```

### 混入

**混入(mixins)**定义了一部分可复用的方法或者计算属性。

混入对象可以包含任意组件选项，当组件使用混入对象时，所有混入对象的选项将被混入该组件本身的选项。

来看一个简单的实例：

```html
<div id = "databinding"></div>
<script>
var vm = new Vue({
  el: '#databinding',
  data: {},
  methods : {},
});
// 定义一个混入对象
var myMixin = {
  created: function () {
    this.startmixin()
  },
  methods: {
    startmixin: function () {
      document.write("欢迎来到混入实例");
    }
  }
};
var Component = Vue.extend({
  mixins: [myMixin]
})
var component = new Component();
</script>
```

**选项合并**

当组件和混入对象含有同名选项时，这些选项将以恰当的方式混合。

比如，数据对象在内部会进行浅合并（一层属性深度），在和组件的数据发生冲突时以组件数据优先。

以下实例中，Vue 实例与混入对象包含了相同的方法。从输出结果可以看出两个选项合并了。

```js
var mixin = {
  created: function () {
    document.write('混入调用' + '<br>')
  }
}
new Vue({
  mixins: [mixin],
  created: function () {
    document.write('组件调用' + '<br>')
  }
});
```

输出结果为：

```sh
混入调用
组件调用
```

如果 methods 选项中有相同的函数名，则 Vue 实例优先级会较高。如下实例，Vue 实例与混入对象的 methods 选项都包含了相同的函数：

```html
<div id = "databinding"></div>
<script type = "text/javascript">
var mixin = {
  methods: {
    hellworld: function () {
      document.write('HelloWorld 方法' + '<br>');
    },
    samemethod: function () {
      document.write('Mixin：相同方法名' + '<br>');
    }
  }
};
var vm = new Vue({
  mixins: [mixin],
  methods: {
    start: function () {
      document.write('start 方法' + '<br>');
    },
    samemethod: function () {
      document.write('Main：相同方法名' + '<br>');
    }
  }
});
vm.hellworld();
vm.start();
vm.samemethod();
</script>
```

输出结果为：

```sh
HelloWorld 方法
start 方法
Main：相同方法名
```

从输出结果 methods 选项中如果碰到相同的函数名则 Vue 实例有更高的优先级会执行输出。

**全局混入**

也可以全局注册混入对象。注意使用！ 一旦使用全局混入对象，将会影响到所有之后创建的 Vue 实例。使用恰当时，可以为自定义对象注入处理逻辑。

```html
<script type = "text/javascript">
// 为自定义的选项 'myOption' 注入一个处理器。
Vue.mixin({
  created: function () {
    var myOption = this.$options.myOption
    if (myOption) {
      document.write(myOption)
    }
  }
})

new Vue({
  myOption: 'hello!'
})
// => "hello!"
</script>
```

谨慎使用全局混入对象，因为会影响到每个单独创建的 Vue 实例（包括第三方模板）。

### 插槽

参考：[https://blog.csdn.net/qq_38128179/article/details/85273522](https://blog.csdn.net/qq_38128179/article/details/85273522)

## 实战



### i18n

- internationalization
- 国际化
- index.html -> 中国人
- index.html -> 美国人
- vue-i18n

### wappalyzer

获取到当前网站的使用的技术：[https://wappalyzer.com/download](https://wappalyzer.com/download)

### Vuex

**Vuex 是什么？**

Vuex 是一个专为 Vue.js 应用程序开发的状态管理模式。它采用集中式存储管理应用的所有组件的状态，并以相应的规则保证状态以一种可预测的方式发生变化。

**什么是“状态管理模式”？**

让我们从一个简单的 Vue 计数应用开始：

```js
new Vue({
  // state
  data () {
    return {
      count: 0
    }
  },
  // view
  template: `
    <div>{{ count }}</div>
  `,
  // actions
  methods: {
    increment () {
      this.count++
    }
  }
})
```

这个状态自管理应用包含以下几个部分：

- state，驱动应用的数据源；
- view，以声明方式将 state 映射到视图；
- actions，响应在 view 上的用户输入导致的状态变化。

以下是一个表示“单向数据流”理念的极简示意：

![x](http://viyitech.cn/public/images/57.png)

但是，当我们的应用遇到多个组件共享状态时，单向数据流的简洁性很容易被破坏：

- 多个视图依赖于同一状态。
- 来自不同视图的行为需要变更同一状态。

对于问题一，传参的方法对于多层嵌套的组件将会非常繁琐，并且对于兄弟组件间的状态传递无能为力。对于问题二，我们经常会采用父子组件直接引用或者通过事件来变更和同步状态的多份拷贝。以上的这些模式非常脆弱，通常会导致无法维护的代码。

因此，我们为什么不把组件的共享状态抽取出来，以一个全局单例模式管理呢？在这种模式下，我们的组件树构成了一个巨大的“视图”，不管在树的哪个位置，任何组件都能获取状态或者触发行为！

另外，通过定义和隔离状态管理中的各种概念并强制遵守一定的规则，我们的代码将会变得更结构化且易维护。

这就是 Vuex 背后的基本思想，借鉴了 [Flux](https://facebook.github.io/flux/docs/overview.html)、[Redux](http://redux.js.org/) 和 [The Elm Architecture](https://guide.elm-lang.org/architecture/)。

与其他模式不同的是，Vuex 是专门为 Vue.js 设计的状态管理库，以利用 Vue.js 的细粒度数据响应机制来进行高效的状态更新。

![x](http://viyitech.cn/public/images/58.png)

**什么情况下我应该使用 Vuex？**

虽然 Vuex 可以帮助我们管理共享状态，但也附带了更多的概念和框架。这需要对短期和长期效益进行权衡。

如果您不打算开发大型单页应用，使用 Vuex 可能是繁琐冗余的。确实是如此——如果您的应用够简单，您最好不要使用 Vuex。一个简单的 [store 模式](https://cn.vuejs.org/v2/guide/state-management.html#%E7%AE%80%E5%8D%95%E7%8A%B6%E6%80%81%E7%AE%A1%E7%90%86%E8%B5%B7%E6%AD%A5%E4%BD%BF%E7%94%A8)就足够您所需了。但是，如果您需要构建一个中大型单页应用，您很可能会考虑如何更好地在组件外部管理状态，Vuex 将会成为自然而然的选择。引用 Redux 的作者 Dan Abramov 的话说就是：

>Flux 架构就像眼镜：您自会知道什么时候需要它。

**安装：**

直接下载 / CDN 引用：[https://unpkg.com/vuex](https://unpkg.com/vuex)

Unpkg.com 提供了基于 NPM 的 CDN 链接。以上的链接会一直指向 NPM 上发布的最新版本。您也可以通过 [https://unpkg.com/vuex@2.0.0](https://unpkg.com/vuex@2.0.0) 这样的方式指定特定的版本。

在 Vue 之后引入 vuex 会进行自动安装：

```html
<script src="/path/to/vue.js"></script>
<script src="/path/to/vuex.js"></script>
```

NPM：

```sh
npm install vuex --save
```

Yarn

```sh
yarn add vuex
```

在一个模块化的打包系统中，您必须显式地通过 Vue.use() 来安装 Vuex：

```js
import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)
```

Vuex 依赖 Promise。如果你支持的浏览器并没有实现 Promise (比如 IE)，那么你可以使用一个 polyfill 的库，例如 es6-promise。

你可以通过 CDN 将其引入：

```html
<script src="https://cdn.jsdelivr.net/npm/es6-promise@4/dist/es6-promise.auto.js"></script>
```

然后 window.Promise 会自动可用。

如果你喜欢使用诸如 npm 或 Yarn 等包管理器，可以按照下列方式执行安装：

```sh
npm install es6-promise --save # npm
yarn add es6-promise # Yarn
```

或者更进一步，将下列代码添加到你使用 Vuex 之前的一个地方：

```js
import 'es6-promise/auto'
```

自己构建：

如果需要使用 dev 分支下的最新版本，您可以直接从 GitHub 上克隆代码并自己构建。

```sh
git clone https://github.com/vuejs/vuex.git node_modules/vuex
cd node_modules/vuex
npm install
npm run build
```

每一个 Vuex 应用的核心就是 `store（仓库）`。"store"基本上就是一个容器，它包含着你的应用中大部分的状态 (state)。Vuex 和单纯的全局对象有以下两点不同：

1. Vuex 的状态存储是响应式的。当 Vue 组件从 store 中读取状态的时候，若 store 中的状态发生变化，那么相应的组件也会相应地得到高效更新。

2. 你不能直接改变 store 中的状态。改变 store 中的状态的唯一途径就是显式地提交 (commit) mutation。这样使得我们可以方便地跟踪每一个状态的变化，从而让我们能够实现一些工具帮助我们更好地了解我们的应用。

Vuex 使用单一状态树——是的，用一个对象就包含了全部的应用层级状态。至此它便作为一个“唯一数据源 (SSOT)”而存在。这也意味着，每个应用将仅仅包含一个 `store` 实例。单一状态树让我们能够直接地定位任一特定的状态片段，在调试的过程中也能轻易地取得整个当前应用状态的快照。

那么我们如何在 Vue 组件中展示状态呢？由于 Vuex 的状态存储是响应式的，从 `store` 实例中读取状态最简单的方法就是在计算属性中返回某个状态。

然而，这种模式导致组件依赖全局状态单例。在模块化的构建系统中，在每个需要使用 state 的组件中需要频繁地导入，并且在测试组件时需要模拟状态。

Vuex 通过 `store` 选项，提供了一种机制将状态从根组件“注入”到每一个子组件中（需调用 `Vue.use(Vuex)`）：

```js
const app = new Vue({
  el: '#app',
  // 把 store 对象提供给 "store" 选项，这可以把 store 的实例注入所有的子组件
  store,
  components: { Counter },
  template: `
    <div class="app">
      <counter></counter>
    </div>
  `
})
```

通过在根实例中注册 `store` 选项，该 `store` 实例会注入到根组件下的所有子组件中，且子组件能通过 `this.$store` 访问到。

**mapState 辅助函数：**

当一个组件需要获取多个状态时候，将这些状态都声明为计算属性会有些重复和冗余。为了解决这个问题，我们可以使用 `mapState` 辅助函数帮助我们生成计算属性，让你少按几次键：

```js
// 在单独构建的版本中辅助函数为 Vuex.mapState
import { mapState } from 'vuex'
export default {
  // ...
  computed: mapState({
    // 箭头函数可使代码更简练
    count: state => state.count,
    // 传字符串参数 'count' 等同于 `state => state.count`
    countAlias: 'count',
    // 为了能够使用 `this` 获取局部状态，必须使用常规函数
    countPlusLocalState (state) {
      return state.count + this.localCount
    }
  })
}
```

当映射的计算属性的名称与 state 的子节点名称相同时，我们也可以给 mapState 传一个字符串数组。

```js
computed: mapState([
  // 映射 this.count 为 store.state.count
  'count'
])
```

**对象展开运算符：**

mapState 函数返回的是一个对象。我们如何将它与局部计算属性混合使用呢？

通常，我们需要使用一个工具函数将多个对象合并为一个，以使我们可以将最终对象传给 computed 属性。但是自从有了对象展开运算符（现处于 ECMASCript 提案 stage-4 阶段），我们可以极大地简化写法：

```js
computed: {
  localComputed () { /* ... */ },
  // 使用对象展开运算符将此对象混入到外部对象中
  ...mapState({
    // ...
  })
}
```

使用 Vuex 并不意味着你需要将所有的状态放入 Vuex。虽然将所有的状态放到 Vuex 会使状态变化更显式和易调试，但也会使代码变得冗长和不直观。如果有些状态严格属于单个组件，最好还是作为组件的局部状态。你应该根据你的应用开发需要进行权衡和确定。

### ElementUI插件

官方网站：[https://element.eleme.cn/#/zh-CN](https://element.eleme.cn/#/zh-CN)

### mint-ui

饿了么出品，element-ui 在PC端使用，移动端版本 mint-ui：`https://mint-ui.github.io/#!/zh-cn`



### 构建

如果要发布在子网站下，需要做如下配置：

**1. build/utils.js**

```js
// ...

if (options.extract) {
  return ExtractTextPlugin.extract({
    use: loaders,
    fallback: 'vue-style-loader',
    publicPath: '../../'  // 修改此处
  })
} else {
  return ['vue-style-loader'].concat(loaders)
}

// ...
```

**2. config/index.js**

```js
// ...

build: {
  // ...
  /**
    * You can set by youself according to actual condition
    * You will need to set this if you plan to deploy your site under a sub path,
    * for example GitHub pages. If you plan to deploy your site to https://foo.github.io/bar/,
    * then assetsPublicPath should be set to "/bar/".
    * In most cases please use '/' !!!
    */
  assetsPublicPath: '/pm/'  // 部署在子网站[应用] pm 下
  // ...
}

// ...
```

**3. index.html**

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <title>电气研发项目管理系统</title>
    <base href="/pm/"> <!-- 部署在子网站[应用] pm 下 -->
    <script src="static/config.js"></script>
    <script type="text/javascript" src="https://webapi.amap.com/maps?v=1.4.15&key=39a58452fda1df6d0c4426101f6326a4"></script>
  </head>
  <body>
    <div id="app"></div>
    <!-- built files will be auto injected -->
  </body>
</html>
```



## 总结



### 常见错误

**1. [NavigationDuplicated {_name: "NavigationDuplicated", name: "NavigationDuplicated"}](https://www.cnblogs.com/dianzan/p/11399745.html)**

1）安装 3.1.1 以上的 vue-router 版本

2）通用处理：在main.js中加入

```js
import Router from 'vue-router'

const originalPush = Router.prototype.push
Router.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}
```



**2. [watch不触发、不生效](https://blog.csdn.net/weixin_40755688/article/details/86719530)**

问题：深层props过程中，props的数据传到了目标文件，但却没有触发数据更新及页面更新

解决：

```js
watch: {
  'obj.attr1.attr1_1': {
    handler (newVal) {
      if (this.uploadConfig.moreList && this.uploadConfig.moreList.length > 0) {
	      this.moreList = newVal.moreList
	    }
	  },
	  deep: true,  // 监听深层
	  immediate: true  // 立即触发
	}
}
```



vue实战路由选项卡