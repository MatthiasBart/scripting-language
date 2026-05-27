from amazoncorretto:8u492
workdir app

copy interpreter . 
run javac Interpreter.java 

copy source ./source

cmd java Interpreter source

