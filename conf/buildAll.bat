cd file_gene

java -jar sanguo-0.0.1-SNAPSHOT.jar


copy bean\java_client_bean\* ..\wonzer_tools\src\main\java\bean\
copy bean\java_server_bean\* ..\wonzer_tools\src\main\java\bean\

cd ..
cd wonzer_tools

mvn clean install

