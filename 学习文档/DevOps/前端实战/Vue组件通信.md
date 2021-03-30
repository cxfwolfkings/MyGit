# Vue组件通信

组件(Component)是 Vue.js 最强大的功能之一。组件可以扩展 HTML 元素，封装可重用的代码。组件系统让我们可以用独立可复用的小组件来构建大型应用，几乎任意类型的应用的界面都可以抽象为一个组件树：

![x](http://viyitech.cn/public/images/53.png)

**为什么使用组件？**

为了应对频繁的需求变化！

>注意：Vue组件的模板在某些情况下会受到HTML的限制，比如`<table>`内规定只允许`<tr>`、`<td>`、`<th>`等这些表格元素，所以在`<table>`内直接使用组件是无效的。这种情况下，可以使用特殊的 `is` 属性来挂载组件。

**全局组件**

```html
<div id="app">
  <runoob></runoob>
</div>
<script>
  // 注册
  Vue.component('runoob', {
    template: '<h1>自定义组件!</h1>'
  })
  // 创建根实例
  new Vue({
    el: '#app'
  })
</script>
```

**局部组件**

全局组件（任何Vue实例都可以使用），局部组件（只有在注册该组件的实例作用域下有效，使用`components`选项注册，可以嵌套）：

```html
<div id="app">
  <runoob></runoob>
</div>
<script>
  var Child = {
    template: '<h1>自定义组件!</h1>'
  }
  // 创建根实例
  new Vue({
    el: '#app',
    components: {
      // <runoob> 将只在父模板可用
      'runoob': Child
    }
  })
</script>
```

除了`template`（`template` 的 DOM 结构必须被一个元素包含，否则无法渲染）选项外，组件中还可以像 Vue 实例那样使用其他的选项，比如`data`、`computed`、`methods`等。但是在使用`data`时，和实例稍有区别，`data`必须是函数，然后将数据 return 出去。

JavaScript对象是引用关系，所以如果 return 出的对象引用了外部的一个对象，那这个对象就是共享的，任何一方修改都会同步。

**组件通信**

组件不仅仅是要把模板的内容进行复用，更重要的是组件间要进行通信。

![x](http://viyitech.cn/public/images/54.png)

***prop***

`prop` 是父组件用来传递数据的一个自定义属性。

父组件的数据需要通过 `props` 把数据传给子组件，子组件需要显式地用`props` 选项声明`prop`；`props`的值可以是两种，一种是字符串数组，一种是对象。

```html
<div id="app">
  <child message="hello!"></child>
</div>
<script>
  // 注册
  Vue.component('child', {
    // 声明 props
    props: ['message'],
    // 同样也可以在 vm 实例中像 "this.message" 这样使用
    template: '<span>{{ message }}</span>'
  })
  // 创建根实例
  new Vue({
    el: '#app'
  })
</script>
```

动态 `Prop` 类似于用 `v-bind` 绑定 HTML 特性到一个表达式，也可以用 `v-bind` 动态绑定 `props` 的值到父组件的数据中。每当父组件的数据变化时，该变化也会传导给子组件：

以下实例中将 `v-bind` 指令将 todo 传到每一个重复的组件中：

```html
<div id="app">
  <ol>
    <todo-item v-for="item in sites" v-bind:todo="item"></todo-item>
  </ol>
</div>
<script>
Vue.component('todo-item', {
  props: ['todo'],
  template: '<li>{{ todo.text }}</li>'
})
new Vue({
  el: '#app',
  data: {
    sites: [
      { text: 'Runoob' },
      { text: 'Google' },
      { text: 'Taobao' }
    ]
  }
})
</script>
```

`props` 中声明的数据与组件 `data` 函数 return 的数据主要区别就是`props` 的来自父级，而 data 中的是组件自己的数据，作用域是组件本身。

这两种数据都可以在模板 `template` 及计算属性 `computed` 和方法 `methods` 中使用。如果要传递多个数据，在 `props` 数组中添加项即可。

由于 HTML 特性不区分大小写，当使用 DOM 模板时，驼峰命名(CamelCase)的 `props` 名称要转为短横分隔命名(kebab-case)。如果你要直接传递数字、布尔值、数组、对象，而且不使用`v-bind`，传递的仅仅是字符串。

Vue2.x 通过 `props` 传递数据是单向的，也就是父组件数据变化时会传递给子组件，但是反过来不行。

业务中会经常遇到两种需要改变`prop`的情况，一种是父组件传递初始值进来，子组件将它作为初始值保存起来，在自己的作用域下可以随意使用和修改。这种情况可以在组件`data`内再声明一个数据，引用父组件的`prop`。另一种情况就是`prop`作为需要被转变的原始值传入。这种情况用计算属性就可以了。注意，在js中对象和数组是引用类型，指向同一个内存空间，所以`props`是对象和数组时，在子组件内改变是会影响父组件的。

**prop验证**：组件可以为 `props` 指定验证要求。`prop` 是一个对象而不是字符串数组时，它包含验证要求。

```js
Vue.component('my-component', {
  props: {
    // 基础的类型检查 (`null` 和 `undefined` 会通过任何类型验证)
    propA: Number,
    // 多个可能的类型
    propB: [String, Number],
    // 必填的字符串
    propC: {
      type: String,
      required: true
    },
    // 带有默认值的数字
    propD: {
      type: Number,
      default: 100
    },
    // 带有默认值的对象
    propE: {
      type: Object,
      // 对象或数组默认值必须从一个工厂函数获取
      default: function () {
        return { message: 'hello' }
      }
    },
    // 自定义验证函数
    propF: {
      validator: function (value) {
        // 这个值必须匹配下列字符串中的一个
        return ['success', 'warning', 'danger'].indexOf(value) !== -1
      }
    }
  }
})
```

当 `prop` 验证失败的时候，（开发环境构建版本的）Vue 将会产生一个控制台的警告。type 可以是下面原生构造器：

- String
- Number
- Boolean
- Array
- Object
- Date
- Function
- Symbol

type 也可以是一个自定义构造器，使用 `instanceof` 检测。

***自定义事件***

父组件是使用 `props` 传递数据给子组件，但如果子组件要把数据传递回去，就需要使用自定义事件！（观察者模式）。我们可以使用 `v-on` 绑定自定义事件，每个Vue实例都实现了事件接口(Events interface)，即：

- 父组件使用`$on(eventName)`监听事件
- 子组件使用`$emit(eventName)`触发事件

另外，父组件可以在使用子组件的地方直接用`v-on`来监听子组件触发的事件。

以下实例中子组件已经和外部完全解耦了，它所做的只是触发一个父组件关心的内部事件。

```html
<div id="app">
  <div id="counter-event-example">
    <p>{{ total }}</p>
    <button-counter v-on:increment="incrementTotal"></button-counter>
    <button-counter v-on:increment="incrementTotal"></button-counter>
  </div>
</div>
<script>
Vue.component('button-counter', {
  template: '<button v-on:click="incrementHandler">{{ counter }}</button>',
  data: function () {
    return {
      counter: 0
    }
  },
  methods: {
    incrementHandler: function () {
      this.counter += 1
      this.$emit('increment')
    }
  },
})
new Vue({
  el: '#counter-event-example',
  data: {
    total: 0
  },
  methods: {
    incrementTotal: function () {
      this.total += 1
    }
  }
})
</script>
```

`$emit()`方法的第一个参数是自定义事件的名称，后面的参数都是要传递的数据，可以不填或填写多个。

如果你想在某个组件的根元素上监听一个原生(DOM)事件。可以使用 .`native` 修饰 `v-on`。例如：

```html
<my-component v-on:click.native="doTheThing"></my-component>
```

**其它组件传值**

- 中央事件总线bus

  ```js
  // 官网写法，vue实例.$on就可以在根实例上定义全局方法
  vm.$on('test', () => {
    //...
  })
  // this.$root就是获取根实例，如果没有根实例，就表示当前实例
  // this.$root.$on 不需要 .eventHub
  // 定义了一个方法  其他组件中都可以使用
  this.$root.eventHub.$emit("test", params)
  // 如果这个方法只能在当前路由下调用，在其他路由中不能被调用，则在当前组件的钩子函数加上销毁方法，当前路由变化的时候（当前组件关闭的时候）销毁这个方法
  this.$root.eventHub.$off("test")
  // 当组件中对象的数据需要通过其他组件获取的时候，可以把对象写成方法传过去，对象值更改后，当前组件中的对象也会发生变化
  this.$root.eventHub.$emit("test", {
    type: [1,3],
    ok: item => {
      $this.form.name = item.name;
    },
    clear: () => {
      $this.form.name = "";
    }
  })
  ```

- 父子链：`$parent`, `$children`，缺点：紧耦合

- 子组件索引：`$refs.componentName`，缺点：`$refs`只在组件渲染完成后才填充，并且它是非响应式的。它仅仅作为一个直接访问子组件的应急方案，应当避免在模板或计算属性中使用`$refs`。

**组件递归**

**异步组件**

- $nextTick
- X-Templates