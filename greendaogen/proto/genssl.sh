cmd="java -Dfile.encoding=UTF-8 -jar ../libs/wire-compiler-2.2.0-jar-with-dependencies.jar"
#pars="--proto_path=./ --java_out=../gen ofo.proto"

pars="--proto_path=./ --java_out=../../app/src/main/java"
$cmd $pars