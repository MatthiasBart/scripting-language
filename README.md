# Scripting Language

Interpreter is written in Java. Start the Interpreter by just typing `make`. Docker deamon must be running.

## Project Structure

The Interpreter is written in Java and resides in the interpreter folder. The `Interpreter.java` is the main class. The Dockerfile declares a java image that copies the source folder and the interpreter folder and builds and executes the interpreter. The interpreter takes the folder path as argument, currently reads the files of that folder and prints them to stdout. The makefile is just used to conveniently start the docker image, just type `make` and you should see the output. 

### Interpreter Design

Here we define which parts of the system are needed and what boundaries there are. Each step could simply be a separate Java Class whose in and outputs are combined in the Interpreter main class.

#### Preprocessor

The preprocessor should combine a main file and multiple includes to one long string, with the includes on the top in the defined order and the main file on the bottom. When there are syntactic errors in the files the preprocessor does not care the only job is to combine the files into one big source. 

#### lexer 

A lexer creates a list of tokens from the source. It evaluates the characters and classifies the token eg. a = 1 + b gets [var(a), eq, int(1), add, var(b)].

#### Parser

Here syntactic analysis is carried out. The list of tokens is formed into a tree strucutre, using a data structure that is most appropriate for tree walking afterwards (execution). This is a recursive operation defining a fucntion for each token. Starting the recursive call in th main function for each statement wihtin the main block could be starting point?

#### AST

At best this is the output of the parser and no additional transformations are needed anymore.

#### Optional Semantic Analyzer

Based on the Sytax Tree, we could check if variables are declared before used...

#### TreeWalk-Evaluation

Every Node of the Tree is evaluated. A dictionary for storing variables and their values should be created for each scope?


## Dev Flow

To adapt the interpreter add/remove/edit the files in the Interpreter, then call `make b` to rebuild the image. To adapt the source that gets interpreted add/remove/edit files in the source directory and again call `make b` afterwards. To execute the interpreter call `make i`. Just copying the source and interpreter files into the directory, compiling and executing should be fast enough for development. 

## Language

Grammer:
```
<prog> ::= {<proc>} <body>.
<proc> ::= <name> '(' {<name>} '|' {<name>} ')' <body>.
<body> ::= '{' {<name>} '|' {<stmt>} '}'.
<stmt> ::= <name> '?' <body> [':' <body>]
| <name> '@' <body>
| <name> '^'
| <name> '(' {<expr>} '|' {<name>} ')'
| <name> '=' <expr>
| <name>.<intlit> '=' <expr>
| '->' <expr>
| <name> '<-'.
<expr> ::= <strlit>
| <intlit>
| <name>
| '[' <expr> '|' <expr> ']'
| '<infix>
| <name>.<intlit>.
<infix> ::= <intlit> + <intlit> 
| <intlit> - <intlit>
| <intlit> * <intlit>
| <intlit> / <intlit>
| <strlit> + <strlit>.
```

Difference from our language to the one described in the assignment
* instead of builtin procedures for arithmetics, IO and pairs handling added new language elements:
  * infix operators, +, -, *, / for integers, + for string concatenation
  * out statement, `-> <expr>`
  * in statement, `<name> <-` to read a string from stdin
  * pair accessor, `<name>.<intlit>`, 0 to access left expression, 1 to access right expression (also allowed on left hand side of assignment)
* <name> identifiers are allowed to be TODO
* <strlit> string literals TODO
* <intlit> int literals TODO
* source is a directory where as a file named "main" is loaded as main and any other file in the dir as included file