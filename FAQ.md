1，启动时profiler未启动
排除方法：
  a.有可能是tomcat中setENV中将OPT变量覆盖掉了，此时需要将export JAVA_OPTS="-javaagent:/usr/local/tprofiler/tprofiler-2.0.0.jar -Dprofile
.properties=/usr/local/tprofiler/profile.properties -Xms512m -Xmx1024m  -XX:MaxPermSize=512M"放置到setENV.sh中
  b.每次部署前先清空~/.tprofiler/profile.properties
  c.将profiler.properties中的debugMode设置为true
  d.将profiler.properties中的开始时间设置为00:00:00,结束时间设置为23:59:59
  e.确认business server.已配置正确


windows下的路径：
-javaagent:d:/tprofiler/tprofiler-2.0.0.jar -Dprofile .properties=d:/tprofiler/profile.properties







