## Mysql  基础知识

### 1. 事务

#### 1.1事务特性

先简单复习一下事务的四个基本要素：ACID



- 原子性：整个事务中的操作，要么全部完成， 要么全部不完成（全部撤销）。

  

- 一致性：事务开始之前和结束之后，数据库的完整性没有遭到破坏。

  

- 隔离性：在同一时间，只允许一个事务请求同一数据。

  

- 持久性：事务完成以后，该事务对数据库所做的操作持久化在数据库中，并不会被回滚。



#### 1.2事务隔离级别

以上都是事务中经常发生的问题，所以为了兼顾并发效率和异常控制，SQL规范定义了四个事务隔离级别：



Read uncommitted (读未提交)：如果设置了该隔离级别，则当前事务可以读取到其他事务已经修改但还没有提交的数据。这种隔离级别是最低的，会导致上面所说的脏读



Read committed (读已提交)：如果设置了该隔离级别，当前事务只可以读取到其他事务已经提交后的数据，这种隔离级别可以防止脏读，但是会导致**不可重复读和幻读**。这种隔离级别最效率较高，并且不可重复读和幻读在一般情况下是可以接受的，所以这种隔离级别最为常用。

例如：A事务隔离级别read committed，在A的一个事务中，执行两次相同的查询，在这两次查询的中间，客户端B对数据进行更改并提交事务，那么会导致客户端A的两次查询结果不一致，导致“不可重复读”的麻烦。



Repeatable read (可重复读)：如果设置了该隔离级别，可以保证当前事务中多次读取特定记录的结果相同。可以防止脏读、不可重复读，但是会导致幻读。



Serializable (串行化)：如果设置了该隔离级别，所有的事务会放在一个队列中执行，当前事务开启后，其他事务将不能执行，即同一个时间点只能有一个事务操作数据库对象。这种隔离级别对于保证数据完整性的能力是最高的，但因为同一时刻只允许一个事务操作数据库，所以大大降低了系统的并发能力。



引用一张很经典的表格来按隔离级别由弱到强来标示为：

|   事务隔离级别   | 是否存在脏读 | 是否存在不可重复读 | 是否存在幻读 |
| :--------------: | :----------: | :----------------: | :----------: |
| Read uncommitted |      √       |         √          |      √       |
|  Read committed  |      ×       |         √          |      √       |
| Repeatable read  |      ×       |         ×          |      √       |
|   Serializable   |      ×       |         ×          |      ×       |



并且隔离级别越高，并发性能越弱：



![图片](http://mmbiz.qpic.cn/mmbiz/DmibiaFiaAI4B0tPURzjJKKWXkgefeVP7W2sG8whnEW3oglUVia3A2rJl4QSULoq60pT8fphESEERAOZyDBt0PXdFA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

查看事务隔离级别：

```
select @@tx_isolation;
```



设置隔离级别：

```
select @@global.tx_isolation;	

set global transaction isolation level repeatable read;
```





#### 1.3脏读、不可重复读、幻读

- 脏读

  某个事务更新了一份数据，另一个事务在此时读取了同一份数据，由于某些原因，前一个事务进行了ROLLBACK操作，则后一个事务读取的数据就会是不正确的。

- 不可重复读

  在一个事务的两次查询之中数据不一致，一个未提交事务读取了另一个事务更新已提交的数据。

- 幻读

  在一个事务的两次查询中数据的记录数不一致，例如有一个事务查询了几行数据，而另一个事务在此时插入了几行数据并进行了提交，先前的事务在接下来的查询中就会发现有几行数据是先前查询中所没有的。



### 2. 存储过程

**场景1：**：根据用户地理位置来展示附近商家的功能，即**按照经纬度计算距离**

```mysql
DROP FUNCTION IF EXISTS func_calcDistance ;
CREATE FUNCTION func_calcDistance(  
    origLng DECIMAL(20,6), -- 目的地经度 
    origLat DECIMAL(20,6), -- 目的地纬度  
    longitude DECIMAL(20,6), -- 当前所在地点经度  
    latitude DECIMAL(20,6) -- 当前所在地点纬度  
)
RETURNS DOUBLE
BEGIN
      DECLARE result DOUBLE DEFAULT 0; 
      
      SET result = round(6378.138*2*asin(sqrt(pow(sin(
        (origLat*pi()/180-latitude*pi()/180)/2),2)+cos(origLat*pi()/180)*cos(latitude*pi()/180)*
        pow(sin( (origLng*pi()/180-longitude*pi()/180)/2),2)))*1000);
      
      RETURN result;  
     
 END ;
```



方案二：

 利用Geohash 计算两点间距离



### 3 复杂查询

统计报表：

```sql
select 
                a.product_id,
                a.product_name,
                count(a.ins_id) as ins_num,
                -- 性别
                count(a.f) as f_num,
                count(a.m) as m_num,
                -- 成员数
                count(a.p_1) as p_1_num,
                count(a.p_2) as p_1_num,
                count(a.p_3) as p_1_num,
                count(a.gt3) as gt3_num,
                -- 年龄
                count(lt25) as lt25_num,
                count(gt25lt35) as gt25lt35_num,
                count(gt35lt45) as gt25lt35_num,
                count(gt45lt55) as gt25lt35_num,
                count(gt55) as gt55_num
        from(
                select 
                        a.ins_id,
                        b.product_id,
                        b.product_name,
                        c.cust_id,
                        c.cust_name,
                        c.cust_sex,
                        c.cust_age,
                        c.family_num,
                        -- 男
                        -- 这个地方根据数据库字段的不同，处理方式也不同
                        -- 如果数据库中cust_sex的数据类型本身就是0和1，那么就不需要转换
                        -- 只列出来即可
                        (case when c.cust_sex='男' then 1 else 0 end) as f,
                        -- 女
                        (case when c.cust_sex='女' then 1 else 0 end) as as m,
                        -- 其他的依次类推
                        -- 家庭成员数
                        (case when c.family_num=1 then 1 else 0 end) as p_1,
                        (case when c.family_num=2 then 1 else 0 end) as P_2,
                        (case when c.family_num=3 then 1 else 0 end) as p_3,
                        (case when c.family_num>3 then 1 else 0 end) as gt3,
                        -- 客户年龄
                        (case when c.cust_age<=25 then 1 else 0 end) as lt25,
                        (case when c.cust_age>25 and c.cust_age<=35 then 1 else 0 end) as gt25lt35,
                        (case when c.cust_age>35 and c.cust_age<=45 then 1 else 0 end) as gt35lt45,
                        (case when c.cust_age>45 and c.cust_age<=55 then 1 else 0 end) as gt45lt55,
                        (case when c.cust_age>55 then 1 else 0 end) as gt55
                from
                        insurance a,
                        product b,
                        customer c
                where 
                        a.product_id=b.product_id
                        and a.cust_id=c.cust_id
        ) a
        group by b.product_id, b.product_name
```

### 4 explain

结果输出展示：

| 字段              | format=json时的名称 | 含义                         |
| ----------------- | ------------------- | ---------------------------- |
| id                | select_id           | 该语句的唯一标识             |
| select_type       | 无                  | 查询类型                     |
| table             | table_name          | 表名                         |
| partitions        | partitions          | 匹配的分区                   |
| **type**          | access_type         | 联接类型                     |
| **possible_keys** | possible_keys       | 可能的索引选择               |
| **key**           | key                 | 实际选择的索引               |
| **key_len**       | key_length          | 索引的长度                   |
| ref               | ref                 | 索引的哪一列被引用了         |
| **rows**          | rows                | 估计要扫描的行               |
| **filtered**      | filtered            | 表示符合查询条件的数据百分比 |
| **Extra**         | 没有                | 附加信息                     |

**字段解释：**

<img src="http://mmbiz.qpic.cn/mmbiz_png/DmibiaFiaAI4B3iar3lZHqAfAKiaD0RHhof1JS0rzZkib9hFrda9EZlCiaphTjV82uG0ic70PHMzMXTXyDzYXiaMzL0FCeQ/640?wx_fmt=png&amp;tp=webp&amp;wxfrom=5&amp;wx_lazy=1&amp;wx_co=1" alt="图片" style="zoom:150%;" />



**select_type**

表示查询的类型

![图片](http://mmbiz.qpic.cn/mmbiz_png/DmibiaFiaAI4B3iar3lZHqAfAKiaD0RHhof1JicEgPXPjuof2foL38KwN2Qhk5LFCfCva67mH1gxQFxDFEIEKqVgaO4w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)





**type**

type显示的是访问类型，是较为重要的一个指标，结果值从好到坏依次是：
**system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL** ，一般来说，得保证查询至少达到range级别，最好能达到ref。

![图片](http://mmbiz.qpic.cn/mmbiz_png/DmibiaFiaAI4B3iar3lZHqAfAKiaD0RHhof1Jx4LdKtnfmxK30DD8dk9qb5sibVBGUg0aJeLNd93e7ViaPVIl2pjRFiatQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)