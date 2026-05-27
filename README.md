# Scripting Language

Interpreter is written in Java. Start the Interpreter by just typing `make`. Docker deamon must be running.

## Project Structure

The Interpreter is written in Java and resides in the interpreter folder. The `Interpreter.java` is the main class. The Dockerfile declares a java image that copies the source folder and the interpreter folder and builds and executes the interpreter. The interpreter takes the folder path as argument, currently reads the files of that folder and prints them to stdout. The makefile is just used to conveniently start the docker image, just type `make` and you should see the output. 
