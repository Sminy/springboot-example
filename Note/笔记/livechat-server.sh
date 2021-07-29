#! /bin/bash
docker pull nexus.xhd.cn:8082/livechat/livechat-server:latest

docker stop livechat-server

docker run -d -p  9924:9924 -p 6601:6601 -p 5556:5556 --name livechat-server --rm -e "JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Shanghai -Dspring.profiles.active=test -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5556" -e "LIVECHAT_MYSQL_URL=jdbc:mysql://192.168.10.160:3306/livechat_test?useUnicode=true&characterEncoding=utf8" nexus.xhd.cn:8082/livechat/livechat-server
