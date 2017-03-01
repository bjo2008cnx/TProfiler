##tprofiler源代码分析

tprofiler是taobao开源的也是国内目前为止唯一一款Profiler工具，主要用于java应用的性能分析，不仅能够抓取Java方法的调用时间，还能抓取mysql的执行时间，
是一款不错的开源性能分析工具。以下对tprofiler源码中的各包进行分析，帮助大家对tprofiler源码有个初步的认识。
###1. 最外层
* Main  TProfiler入口,定义了premain方法，使用instrument的agent类必需的方法
* Profiler：用于收集运行时应用数据，主要收集方法开始执行时的时间，方法结束时的的时间，用于方法耗时统计，这里有个硬编码，只统计耗时超过10ms的方法
* Manager：管理类，内部启动四个线程，分别用于：将分析结果写入log文件，Socket开关，时间控制和调用栈取样

###2. analysis: 离线分析profiler的结果

主要的类ProfilerLogAnalysis,该方面的分析以main()方式启动，适用方式如下：
```
ProfilerLogAnalysis <tprofiler.log path> <tmethod.log path> <topmethod.log path> <topobject.log path>
```

该类分析了以下文件：(以下文件名在profile.properties定义，完整路径为${user.home}/logs/)
tprofiler.log: 性能分析数据，由DataDumpThread输出
tmethod.log: 方法相关数据，由TimeControlThread或InnerSocketThread线程调用MethodCache.flushMethodData()输出
topmethod.log: top方法相关数据，由ProfilerLogAnalysis输出
topobjects.log: top对象相关数据，由ProfilerLogAnalysis输出
mysqlProfiler.log: 由DataDumpThread输出

###3. instrument包：修改字节码
这个是字节码注入的比较核心的包，也是唯一稍微难度的地方

* ProfClassAdapter : asm类适配器，对类进行字节码注入
* ProfMethodAdapter: asm方法适配器，对方法进行字节码注入
* ProfTransformer：用于注入Profiler的字节码，可注入字节码的类分为两类，普通类和Mysql相关类

###4. config包：配置
* ProfFilter: 包过滤器,过滤注入或者不注入的Package
* ConfigureProperties：用于加载配置文件的properties类
* ProfConfig:用于读取与保存profile.properties文件

###5. thread
* DataDumpThread ：将性能分析数据写到log线程
* InnerSocketThread ：控制开关的线程，用于配合TProfileClient
* SamplerThread ：调用栈采样线程
* TimeControlThread ：开始时间结束时间控制线程

###6. client包：客户端
* TProfileClient：TProfiler客户端,用来远程打开 关闭及查看状态


##代码量统计
|路径| 总行数 | 空行 | 注释行 | 代码行 |
| -- | ------ |----- | -----  |--------|
|\TProfiler\src\main\java|4247|470|1420|2357|


