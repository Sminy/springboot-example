## HTTP 协议

### TCP/IP 通信数据流

![image-20210722094047797](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210722094047797.png)



### TCP 生命周期

#### tcp 报文

![图片](http://mmbiz.qpic.cn/mmbiz_png/eXCSRjyNYcZ0tTCP36XkuC5IFARbyAVpQHia933vwzPJr0HtZwSjroW1v8TPtbYqYkqc8Vib2oiauO2iaFVz6Cknqg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



**上图中有几个字段需要重点介绍下：**

（1）序号：Seq序号，占32位，用来标识从TCP源端向目的端发送的字节流，发起方发送数据时对此进行标记。
（2）确认序号：Ack序号，占32位，只有ACK标志位为1时，确认序号字段才有效，Ack=Seq+1。
（3）标志位：共6个，即URG、ACK、PSH、RST、SYN、FIN等，具体含义如下：
    （A）URG：紧急指针（urgent pointer）有效。
    （B）ACK：确认序号有效。
    （C）PSH：接收方应该尽快将这个报文交给应用层。
    （D）RST：重置连接。
    （E）SYN：发起一个新连接。
    （F）FIN：释放一个连接。

**需要注意的是：**

（A）不要将确认序号Ack与标志位中的ACK搞混了。
（B）确认方Ack=发起方Req+1，两端配对。 



![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWBOnn25VTBo8Wn4l3vhO4DofGmTjM717kXiaPtNgvPErAKmREy67Eu6PtBWZpU8IgpCicpJNTTbabA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



#### **3次握手过程详解**

![图片](http://mmbiz.qpic.cn/mmbiz_png/eXCSRjyNYcZ0tTCP36XkuC5IFARbyAVphP0sA1YPPlb5QJUhW5ltEjiaZJoBuLoIa8bibjOfuGpnrnmK7OlQ46GQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**（1）第一次握手：**
Client将标志位SYN置为1，随机产生一个值seq=J，并将该数据包发送给Server，Client进入**SYN_SENT**状态，等待Server确认。

**（2）第二次握手：**
Server收到数据包后由标志位SYN=1知道Client请求建立连接，Server将标志位SYN和ACK都置为1，ack=J+1，随机产生一个值seq=K，并将该数据包发送给Client以确认连接请求，Server进入**SYN_RCVD**状态。

**（3）第三次握手：**
Client收到确认后，检查ack是否为J+1，ACK是否为1，如果正确则将标志位ACK置为1，ack=K+1，并将该数据包发送给Server，Server检查ack是否为K+1，ACK是否为1，如果正确则连接建立成功，Client和Server进入**ESTABLISHED**状态，完成三次握手，随后Client与Server之间可以开始传输数据了。



#### 4次挥手过程详解

![图片](http://mmbiz.qpic.cn/mmbiz_png/eXCSRjyNYcZ0tTCP36XkuC5IFARbyAVpzejna1ev8hkt7Y1blia3PIQvBZ7uEHpibhxFrPESlGlZHQ6kfZgKeOqQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



- 第一次挥手：
  Client发送一个FIN，用来关闭Client到Server的数据传送，Client进入**FIN_WAIT_1**状态。
- 第二次挥手：
  Server收到FIN后，发送一个ACK给Client，确认序号为收到序号+1（与SYN相同，一个FIN占用一个序号），Server进入**CLOSE_WAIT**状态。
- 第三次挥手：
  Server发送一个FIN，用来关闭Server到Client的数据传送，Server进入**LAST_ACK**状态。
- 第四次挥手：
  Client收到FIN后，Client进入TIME_WAIT状态，接着发送一个ACK给Server，确认序号为收到序号+1，Server进入**CLOSED**状态，完成四次挥手。



![image-20210603161819489](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210603161819489.png)





 **为什么建立连接是三次握手，而关闭连接却是四次挥手呢？**

这是因为服务端在LISTEN状态下，收到建立连接请求的SYN报文后，把ACK和SYN放在一个报文里发送给客户端。

而关闭连接时，当收到对方的FIN报文时，仅仅表示对方不再发送数据了但是还能接收数据，己方也未必全部数据都发送给对方了，所以己方可以立即close，也可以发送一些数据给对方后，再发送FIN报文给对方来表示同意现在关闭连接，因此，己方ACK和FIN一般都会分开发送。



**为什么客户端最后还要等待2MSL？**

MSL（Maximum Segment Lifetime），TCP允许不同的实现可以设置不同的MSL值。

第一，保证客户端发送的最后一个ACK报文能够到达服务器，因为这个ACK报文可能丢失，站在服务器的角度看来，我已经发送了FIN+ACK报文请求断开了，客户端还没有给我回应，应该是我发送的请求断开报文它没有收到，于是服务器又会重新发送一次，而客户端就能在这个2MSL时间段内收到这个重传的报文，接着给出回应报文，并且会重启2MSL计时器。



第二，防止类似与“三次握手”中提到了的“已经失效的连接请求报文段”出现在本连接中。客户端发送完最后一个确认报文后，在这个2MSL时间中，就可以使本连接持续的时间内所产生的所有报文段都从网络中消失。这样新的连接中不会出现旧连接的请求报文。



**如果已经建立了连接，但是客户端突然出现故障了怎么办？**

TCP还设有一个保活计时器，显然，客户端如果出现故障，服务器不能一直等下去，白白浪费资源。服务器每收到一次客户端的请求后都会重新复位这个计时器，时间通常是设置为2小时，若两小时还没有收到客户端的任何数据，服务器就会发送一个探测报文段，以后每隔75分钟发送一次。若一连发送10个探测报文仍然没反应，服务器就认为客户端出了故障，接着就关闭连接。



#### 抓包三次握手与四次挥手

![图片](https://mmbiz.qpic.cn/mmbiz_png/8GwA4HUQ6WhOW7LFTicdINApzXHD5KAdmf9P8Osia4KIXUg47DtuGXl62AcyNR5GHCKPmM6OARkDWicFQkddtMCEg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### HTTP 1.1

- 持久化连接：keep-alive

  ​	持久化连接的好处在于减少TCP连接的重复建立和断开所造成的额外开销，减轻了服务端的负载。

- 管线化 pipelining

  ​	持久化连接使得多数请求以管线化方式发送请求成为可能。管线化出现后，不用等待响应亦可直接发送下一个请求。

- 无状态

  ​	HTTP 是无状态协议，它不对之前发生过的请求和响应的状态进行管理。