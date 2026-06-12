folder ?= source

default: build test

build:
	javac -d out $(shell find . -name "*.java")

test:
	java -ea -cp out test.InterpreterTests

run:
	java -cp out Interpreter $(folder)

b: build
t: test
r: run
