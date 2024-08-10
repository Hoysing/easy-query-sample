## 简介
这是一个Easy Query的使用用例，请先执行`resources\sql\mysql.sql`来导入数据，
在`com.easy.query.sample.base.Config`中修改数据库配置。
请不要一次执行全部方法，也不要随便执行有关插入和更新的测试方法，它们不具备幂等性，
如果测试出错，请重新导入数据。

具体使用说明请参考 [Easy Query官方文档](`http://www.easy-query.com/`)。

## 对象模式
在对象模式下，为什么实体类`User`需要实现`ProxyEntityAvailable<User, UserProxy>`
因为EasyEntityQuery需要根据`User`类型推断出`UserProxy`类型，`UserProxy`类型是APT生成的，它有`name()`方法，如下：
```java
        EasyEntityQuery easyEntityQuery = new DefaultEasyEntityQuery(easyQueryClient);
        easyEntityQuery.queryable(User.class).where(e -> e.name().like("张")).toList();
```
可以不实现`ProxyEntityAvailable<User, UserProxy>`吗？
不可以，因为`EasyEntityQuery`的`queryable`方法如下：
```java
<TProxy extends ProxyEntity<TProxy, T>, T extends ProxyEntityAvailable<T,TProxy>> EntityQueryable<TProxy, T> queryable(Class<T> entityClass);
```
它声明了`T extends ProxyEntityAvailable<T,TProxy>`，也就是说`User`必须实现`ProxyEntityAvailable<User, UserProxy>`
为什么`EasyEntityQuery`的`queryable`方法如此设计呢？它可以根据`User`推断出`UserProxy`
```java
        EasyEntityQuery easyEntityQuery = new DefaultEasyEntityQuery(null);
        EntityQueryable<UserProxy, User> queryable = easyEntityQuery.queryable(User.class);
```
可否不实现`ProxyEntityAvailable<User, UserProxy>`，直接推断出`UserProxy`呢？
效果如下：
```java
EasyQuery easyQuery = new EasyQuery();
Queryable<ExtUserProxy, User> queryable1 = easyQuery.queryable(User.class);
```

设计如下：
```java
//此处ExtProxy类似ProxyEntity
public class ExtProxy<T> {
}

public class Queryable<P extends ExtProxy<T>, T> {
    public void where(SQLExpression1<P> whereExpression) {
    }
}

public class EasyQuery {
    <P extends ExtProxy<T>,T> Queryable<P, T> queryable(Class<T> entityClass) {
        return null;
    }
}

//此处ExtProxy类似AbstractProxyEntity，其实最终AbstractProxyEntity还是实现ProxyEntity
public class ExtUserProxy extends ExtProxy<User> {
    public ExtUserProxy() {
    }

    public int id() {
        return 0;
    }
}
```
结果如下：
```java
EasyQuery easyQuery = new EasyQuery();
Queryable<ExtProxy<User>, User> queryable1 = easyQuery.queryable(User.class);
```
我们发现T最多就只能推断T，如果User不去实现UserProxy的接口，根本无法根据User推断出UserProxy，
再回到设计，如下：
```java
<TProxy extends ProxyEntity<TProxy, T>, T extends ProxyEntityAvailable<T,TProxy>> EntityQueryable<TProxy, T> queryable(Class<T> entityClass);
```
首先传入的`Class<T>`推断出`T`，比如`User.class`->`User`,由`T`得知`ProxyEntityAvailable<T,TProxy>`，即比如`User`->`ProxyEntityAvailable<User,UserProxy>`
所以`T extends ProxyEntityAvailable<T,TProxy>`，而`TProxy`必须是类型`UserProxy`这样的，所以`<TProxy extends ProxyEntity<TProxy, T>`

## API模式选择
在query方法中，是根据类型查对象
   在对象模式下，入参是Class<T>类型，T实现接口关联TProxy,可推断出TProxy类型
   在代理模式下，入参是实体类T的增强类型，即TProxy类型，可推断出TProxy类型
但是在update方法中，是修改对象
   在对象模式下，入参不是Class<T>类型，是T类型，因为修改的是对象,但可以调getClass获取到其Class<T>类型，再推断出TProxy类型
   在代理模式下，入参虽然是T类型，但不可以像对象模式那样推断出TProxy类型，因为T没有实现接口关联TProxy



