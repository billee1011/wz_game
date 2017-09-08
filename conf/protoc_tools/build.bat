bin\protoc -I=./ --csharp_out=./ ./login.proto
bin\protoc -I=./ --java_out=./ ./login.proto


bin\protoc -I=./ --csharp_out=./ ./common.proto
bin\protoc -I=./ --java_out=./ ./common.proto

pause