#!/bin/sh

PRO_DIR=`dirname $0`;
PRO_DIR=`cd $PRO_DIR/;pwd`;
TIME_SUFFIX=`date +%Y%m%d%H`;
PWD_PATH=`pwd | awk -F/ '{print $NF}'`

echo $PRO_DIR

cd $PRO_DIR/center_server/target
nohup java -jar center_server-1.0-SNAPSHOT.jar >> /dev/null 2>>error.log &

sleep 3;
cd $PRO_DIR/gate_server/target
nohup java -jar gate_server-1.0-SNAPSHOT.jar >> /dev/null 2>>error.log &

sleep 3;
cd $PRO_DIR/logic_server/target
nohup java -jar  logic_server-1.0-SNAPSHOT.jar >> /dev/null 2>>error.log &

sleep 3;
cd $PRO_DIR/login_server/target
nohup java -jar login_server-1.0-SNAPSHOT.jar >> /dev/null 2>>error.log &

sleep 3;
cd $PRO_DIR/log_server/target
nohup java -jar log_server-1.0-SNAPSHOT.jar  >> /dev/null 2>>error.log &
