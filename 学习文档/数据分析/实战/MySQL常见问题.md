# 常见问题

#### 1. This function has none of DETERMINISTIC, NOSQL, ...

```sql
set global log_bin_trust_function_creators = TRUE;
```

这是我们开启了bin-log, 我们就必须指定我们的函数是否是：

1. DETERMINISTIC 不确定的
2. NO SQL 没有SQl语句，当然也不会修改数据
3. READS SQL DATA 只是读取数据，当然也不会修改数据
4. MODIFIES SQL DATA 要修改数据
5. CONTAINS SQL 包含了SQL语句

其中在 function 里面，只有 DETERMINISTIC, NO SQL 和 READS SQL DATA 被支持。如果我们开启了 bin-log, 我们就必须为我们的 function 指定一个参数。



#### 2. Illegal mix of collations (utf8_unicode_ci,IMPLICIT) and ...

```sql
CONVERT('xxx' USING utf8) COLLATE utf8_unicode_ci
```

存储过程中给字符串变量设置了超出长度的值，也有可能报此异常



#### 3. 非空字段插入空值

问题：Incorrect integer value: '' for column 'id' at row 1

解决：my.ini中查找sql-mode，默认为

sql-mode="STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"

删除`STRICT_TRANS_TABLES`, `NO_AUTO_CREATE_USER`

重启mysql后即可 



#### 4. MySQL Connector/NET Exception: Reading from the stream has failed

参考：https://blog.csdn.net/fancyf/article/details/78295964

连接字符串：**SslMode=None**



#### 5. mysql 卡死 大部分线程长时间处于sending data的状态

sending data状态表示两种情况，一种是mysql已经查询了数据，正在发给客户端；另一种情况是，mysql已经知道某些数据需要去什么地方读取，正在从数据文件中读取。

临时表、存储库引擎、缓存池设置的容量是否足够？

默认情况下，临时表空间文件是自动扩展的，在正常关闭或初始化中止时，将删除临时表空间，并在每次启动服务器时重新创建。

对临时表空间的大小进行限制，允许自动增长，但最大容量有上限：

```ini
[mysqld]
innodb_temp_data_file_path=ibtmp1:12M:autoextend:max:500M
```

设置了上限的大小，当数据文件达到最大大小时，查询将失败，并显示一条错误消息，表明表已满，查询不能往下执行，避免 ibtmp1 过大。

8.0 的临时表空间分为会话临时表空间和全局临时表空间，会话临时表空间存储用户创建的临时表和当 InnoDB 配置为磁盘内部临时表的存储引擎时由优化器创建的内部临时表，当会话断开连接时，其临时表空间将被截断并释放回池中；也就是说，在 8.0 中有一个专门的会话临时表空间，当会话被杀掉后，可以回收磁盘空间；而原来的 ibtmp1 是现在的全局临时表空间，存放的是对用户创建的临时表进行更改的回滚段，在 5.7 中 ibtmp1 存放的是用户创建的临时表和磁盘内部临时表；
也就是在 8.0 和 5.7 中 ibtmp1 的用途发生了变化，5.7 版本临时表的数据存放在 ibtmp1 中，在 8.0 版本中临时表的数据存放在会话临时表空间，如果临时表发生更改，更改的 undo 数据存放在 ibtmp1 中；

总结：在 mysql5.7 时，杀掉会话，临时表会释放，但是仅仅是在 ibtmp 文件里标记一下，空间是不会释放回操作系统的。如果要释放空间，需要重启数据库；在 mysql8.0 中可以通过杀掉会话来释放临时表空间。



## 死锁

死锁是指两个或两个以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象，可以认为如果一个资源被锁定，它总会在以后某个时间被释放。而死锁发生在当多个进程访问同一数据库时，其中每个进程拥有的锁都是其他进程所需的，由此造成每个进程都无法继续下去。

InnoDB的并发写操作会触发死锁，InnoDB也提供了死锁检测机制，可以通过设置innodb_deadlock_detect参数打开或关闭死锁检测：

```sql
-- 打开死锁检测，数据库发生死锁时自动回滚（默认选项）
innodb_deadlock_detect = on
-- 关闭死锁检测，发生死锁的时候，用锁超时来处理，
-- 通过设置锁超时参数innodb_lock_wait_timeout可以在超时发生时回滚被阻塞的事务
innodb_deadlock_detect = off
-- 保存全部死锁日志（该选项默认关闭，打开时死锁日志存放到error_log配置的文件里面）
set global innodb_print_all_deadlocks = on
```

还可以通过设置InnDB Monitors来进一步观察锁冲突详细信息。设置InnoDB Monitors方法：

```sql
create database test;
use test
create table innodb_monitor(a INT) engine=innodb;
create table innodb_tablespace_monitor(a INT) engine=innodb;
create table innodb_lock_monitor(a INT) engine=innodb;
create table innodb_table_monitor(a INT) engine=innodb;
```

常用SQL语句：

```sql
-- 查看死锁
show engine innodb status

-- 查询是否锁表
show OPEN TABLES where In_use > 0;

-- 数据库版本查询
select version();

-- 引擎查询
show create table {tableName};
SHOW TABLE STATUS FROM dbName WHERE name = 'tbName'

-- 事务隔离级别查询方法
select @@tx_isolation;

-- 事务隔离级别设置方法（只对当前Session生效）：
set session transaction isolation level read committed;
/**
 * 注意：
 *   1. 如果数据库是分库的，以上SQL语句需要在单库上执行，不能在逻辑库执行。
 *   2. 全局生效，需要修改my.ini
 */

-- 方法1：利用 metadata_locks 视图
-- 此方法仅适用于 MySQL 5.7 以上版本，该版本 performance_schema 新增了 metadata_locks，
-- 如果上bai锁前启用了元数据锁的探针（默认是未启用的），可以比较容易的定位全局锁会话。
-- 1：查看当前的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_TRX;
-- 2：查看当前锁定的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCKS;
-- 3：查看当前等锁的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCK_WAITS;

-- 方法2：利用 events_statements_history 视图
-- 此方法适用于 MySQL 5.6 以上版本，启用 performance_schema.eventsstatements_history（5.6 默认未启用，5.7 默认启用），
-- 该表会 SQL 历史记录执行，如果请求太多，会自动清理早期的信息，有可能将上锁会话的信息清理掉。

-- 方法3：利用 gdb 工具
-- 如果上述两种都用不了或者没来得及启用，可以尝试第三种方法。
-- 利用 gdb 找到所有线程信息，查看每个线程中持有全局锁对象，输出对应的会话 ID。
-- 也可以使用 gdb 交互模式，但 attach mysql 进程后 mysql 会完全 hang 住，读请求也会受到影响，不建议使用交互模式。

-- 方法4：show processlist
-- 如果备份程序使用的特定用户执行备份，如果是 root 用户备份，那 time 值越大的是持锁会话的概率越大，
-- 如果业务也用 root 访问，重点是 state 和 info 为空的，这里有个小技巧可以快速筛选，筛选后尝试 kill 对应 ID，
-- 再观察是否还有 wait global read lock 状态的会话。

-- 方法5：重启！
```

**解决思路：**

1. 使用临时表保存全部待操作记录（增删改）
2. 给业务表加表级锁
3. 将临时表数据同步到业务表
4. 释放表级锁

**问题：**业务表在大量并发操作下，会发生什么？



## 总结

1. 在1个SQL语句中临时表只能查询一次！连接断开后，自动删除
2. 存储过程（函数）的迁移不要使用 Navicat，会引起 **编码** 异常！！！用自己的脚本创建。