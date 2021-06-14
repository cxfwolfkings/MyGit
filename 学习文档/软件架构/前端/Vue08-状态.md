

commit: 同步操作

```js
// 存储
this.$store.commit('changeValue',name)
// 取值
this.$store.state.changeValue
```

dispatch: 异步操作

```js
// 存储
this.$store.dispatch('getlists',name)
// 取值
this.$store.getters.getlists
```

总的来说，它们只是存取方式的不同，两个方法都是传值给 vuex 的 mutation 改变 state。

