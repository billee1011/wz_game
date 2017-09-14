bin\protoc -I=./ --csharp_out=./ ./login.proto
bin\protoc -I=./ --java_out=./ ./login.proto


bin\protoc -I=./ --csharp_out=./ ./common.proto
bin\protoc -I=./ --java_out=./ ./common.proto

bin\protoc -I=./ --csharp_out=./ ./hero.proto
bin\protoc -I=./ --java_out=./ ./hero.proto

bin\protoc -I=./ --csharp_out=./ ./equip.proto
bin\protoc -I=./ --java_out=./ ./equip.proto

pause