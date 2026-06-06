package parser;

import lexer.tokens.Token;
import lexer.tokens.TokenType;

import java.util.List;


public class Parser {

    public Parser() {

    }

    public Program parse(List<Token> tokens) {
        if(tokens.isEmpty()) return new Program();
        System.out.println("parsing");
        int position = 0;
        Token peekToken;

        Program program = new Program();

        for (Token currToken : tokens) {
            peekToken = position + 1 < tokens.size()
                    ? tokens.get(position+1)
                    : null;

            Statement stmt = parseStatement(currToken, peekToken);

            if (stmt != null) {
                program.getStatements().add(stmt);
            }

            position += 1;
        }

        return program;

    }

    private Statement parseStatement(Token currToken, Token peekToken) {
        if (currToken.type() == TokenType.IDENTIFIER && peekToken.type() == TokenType.ASSIGNMENT) {
            return parseAssignmentStatement();
        }
        return null;
    }
    private Statement parseAssignmentStatement() {
        return null;
    }
        /*

        2.4 - Parser’s first steps: parsing let statements
In Monkey, variable bindings are statements of the following form:
let x = 5;
let y = 10;
let foobar = add(5, 5);
let barfoo = 5 * 5 / 10 + 18 - add(5, 5) + multiply(124);
let anotherName = barfoo;
These statements are called “let statements” and bind a value to the given name. let x = 5;
binds the value 5 to the name x. Our job in this section is to parse let statements correctly.
For now we’re going to skip parsing the expressions that produce the value of a given variable
binding and come back to this later - as soon as we know how to parse expressions on their
own.
What does it mean to parse let statements correctly? It means that the parser produces an
AST that accurately represents the information contained in the original let statement. That
sounds reasonable, but we don’t have an AST yet, nor do we know what it should look like. So
our first task is to take a close look at Monkey source code and see how it’s structured, so that
we can define the necessary parts of an AST that’s able to accurately represent let statements.
Here is a fully valid program written in Monkey:
let x = 10;
let y = 15;
let add = fn(a, b) {
return a + b;
};
32
Programs in Monkey are a series of statements. In this example we can see three statements,
three variable bindings - let statements - of the following form:
let <identifier> = <expression>;
A let statement in Monkey consists of two changing parts: an identifier and an expression. In
the example above x, y and add are identifiers. 10, 15 and the function literal are expressions.
Before we go on, a few words about the difference between statements and expressions are
needed. Expressions produce values, statements don’t. let x = 5 doesn’t produce a value,
whereas 5 does (the value it produces is 5). A return 5; statement doesn’t produce a value,
but add(5, 5) does. This distinction - expressions produce values, statements don’t - changes
depending on who you ask, but it’s good enough for our needs.
What exactly an expression is or a statement, what produces values and what doesn’t, depends
on the programming language. In some languages function literals (e.g.: fn(x, y) { return x
+ y; }) are expressions and can be used in any place where any other expression is allowed. In
other programming languages though function literals can only be part of a function declaration
statement, in the top level of the program. Some languages also have “if expressions”, where
conditionals are expressions and produce a value. This is entirely dependent on the choices the
language designers made. As you’ll see, a lot of things in Monkey are expressions, including
function literals.
Back to our AST. Looking at the example above, we can see that it needs two different types
of nodes: expressions and statements. Take a look at the start of our AST:
// ast/ast.go
package ast
type Node interface {
TokenLiteral() string
}
type Statement interface {
Node
statementNode()
}
type Expression interface {
Node
expressionNode()
}
Here we have three interfaces called Node, Statement and Expression. Every node in our AST has
to implement the Node interface, meaning it has to provide a TokenLiteral() method that returns
the literal value of the token it’s associated with. TokenLiteral() will be used only for debugging
and testing. The AST we are going to construct consists solely of Nodes that are connected to
each other - it’s a tree after all. Some of these nodes implement the Statement and some the
Expression interface. These interfaces only contain dummy methods called statementNode and
expressionNode respectively. They are not strictly necessary but help us by guiding the Go
compiler and possibly causing it to throw errors when we use a Statement where an Expression
should’ve been used, and vice versa.
And here is our first implementation of Node:
// ast/ast.go
33
type Program struct {
Statements []Statement
}
func (p *Program) TokenLiteral() string {
if len(p.Statements) > 0 {
return p.Statements[0].TokenLiteral()
} else {
return ""
}
}
This Program node is going to be the root node of every AST our parser produces. Every
valid Monkey program is a series of statements. These statements are contained in the Program.Statements, which is just a slice of AST nodes that implement the Statement interface.
With these basic building blocks for our AST construction defined, let’s think about what a
node for a variable binding in the form of let x = 5; might look like. Which fields should it
have? Definitely one for the name of the variable. And it also needs a field that points to the
expression on the right side of the equal sign. It needs to be able to point to any expression.
It can’t just point to a literal value (the integer literal 5 in this case), since every expression
is valid after the equal sign: let x = 5 * 5 is as valid as let y = add(2, 2) * 5 / 10;. And
then the node also needs to keep track of the token the AST node is associated with, so we can
implement the TokenLiteral() method. That makes three fields: one for the identifier, one for
the expression that produces the value in the let statement and one for the token.
// ast/ast.go
import "monkey/token"
// [...]
type LetStatement struct {
Token token.Token // the token.LET token
Name *Identifier
Value Expression
}
func (ls *LetStatement) statementNode() {}
func (ls *LetStatement) TokenLiteral() string { return ls.Token.Literal }
type Identifier struct {
Token token.Token // the token.IDENT token
Value string
}
func (i *Identifier) expressionNode() {}
func (i *Identifier) TokenLiteral() string { return i.Token.Literal }
LetStatement has the fields we need: Name to hold the identifier of the binding and Value for the
expression that produces the value. The two methods statementNode and TokenLiteral satisfy
the Statement and Node interfaces respectively.
To hold the identifier of the binding, the x in let x = 5;, we have the Identifier struct type,
which implements the Expression interface. But the identifier in a let statement doesn’t produce
a value, right? So why is it an Expression? It’s to keep things simple. Identifiers in other parts
of a Monkey program do produce values, e.g.: let x = valueProducingIdentifier;. And to
34
keep the number of different node types small, we’ll use Identifier here to represent the name
in a variable binding and later reuse it, to represent an identifier as part of or as a complete
expression.
With Program, LetStatement and Identifier defined this piece of Monkey source code
let x = 5;
could be represented by an AST looking like this:
Now that we know what it’s supposed to look like, the next task is to construct such an AST.
So, without further ado here is the beginning of our parser:
// parser/parser.go
package parser
import (
"monkey/ast"
"monkey/lexer"
"monkey/token"
)
type Parser struct {
l *lexer.Lexer
curToken token.Token
peekToken token.Token
}
35
func New(l *lexer.Lexer) *Parser {
p := &Parser{l: l}
// Read two tokens, so curToken and peekToken are both set
p.nextToken()
p.nextToken()
return p
}
func (p *Parser) nextToken() {
p.curToken = p.peekToken
p.peekToken = p.l.NextToken()
}
func (p *Parser) ParseProgram() *ast.Program {
return nil
}
The Parser has three fields: l, curToken and peekToken. l is a pointer to an instance of the
lexer, on which we repeatedly call NextToken() to get the next token in the input. curToken
and peekToken act exactly like the two “pointers” our lexer has: position and peekPosition.
But instead of pointing to a character in the input, they point to the current and the next
token. Both are important: we need to look at the curToken, which is the current token under
examination, to decide what to do next, and we also need peekToken for this decision if curToken
doesn’t give us enough information. Think of a single line only containing 5;. Then curToken
is a token.INT and we need peekToken to decide whether we are at the end of the line or if we
are at just the start of an arithmetic expression.
The New function is pretty self-explanatory and the nextToken method is a small helper that
advances both curToken and peekToken. But ParseProgram is empty, for now.
Now before we start writing tests and filling out the ParseProgram method I want to show you
the basic idea and structure behind a recursive descent parser. That makes it a lot easier
to understand our own parser later on. What follows are the major parts of such a parser
in pseudocode. Read this carefully and try to understand what happens in the parseProgram
function:
function parseProgram() {
program = newProgramASTNode()
advanceTokens()
for (currentToken() != EOF_TOKEN) {
statement = null
if (currentToken() == LET_TOKEN) {
statement = parseLetStatement()
} else if (currentToken() == RETURN_TOKEN) {
statement = parseReturnStatement()
} else if (currentToken() == IF_TOKEN) {
statement = parseIfStatement()
}
if (statement != null) {
program.Statements.push(statement)
}
36
advanceTokens()
}
return program
}
function parseLetStatement() {
advanceTokens()
identifier = parseIdentifier()
advanceTokens()
if currentToken() != EQUAL_TOKEN {
parseError("no equal sign!")
return null
}
advanceTokens()
value = parseExpression()
variableStatement = newVariableStatementASTNode()
variableStatement.identifier = identifier
variableStatement.value = value
return variableStatement
}
function parseIdentifier() {
identifier = newIdentifierASTNode()
identifier.token = currentToken()
return identifier
}
function parseExpression() {
if (currentToken() == INTEGER_TOKEN) {
if (nextToken() == PLUS_TOKEN) {
return parseOperatorExpression()
} else if (nextToken() == SEMICOLON_TOKEN) {
return parseIntegerLiteral()
}
} else if (currentToken() == LEFT_PAREN) {
return parseGroupedExpression()
}
// [...]
}
function parseOperatorExpression() {
operatorExpression = newOperatorExpression()
operatorExpression.left = parseIntegerLiteral()
operatorExpression.operator = currentToken()
operatorExpression.right = parseExpression()
return operatorExpression()
}
// [...]
37
Since this is pseudocode there are a lot of omissions, of course. But the basic idea behind
recursive-descent parsing is there. The entry point is parseProgram and it constructs the root
node of the AST (newProgramASTNode()). It then builds the child nodes, the statements, by
calling other functions that know which AST node to construct based on the current token.
These other functions call each other again, recursively.
The most recursive part of this is in parseExpression and is only hinted at. But we can already
see that in order to parse an expression like 5 + 5, we need to first parse 5 + and then call
parseExpression() again to parse the rest, since after the + might be another operator expression,
like this: 5 + 5 * 10. We will get to this later and look at expression parsing in detail, since it’s
probably the most complicated but also the most beautiful part of the parser, making heavy
use of “Pratt parsing”.
But for now, we can already see what the parser has to do. It repeatedly advances the tokens
and checks the current token to decide what to do next: either call another parsing function or
throw an error. Each function then does its job and possibly constructs an AST node so that
the “main loop” in parseProgram() can advance the tokens and decide what to do again.
If you looked at that pseudocode and thought “Well, that’s actually pretty easy to understand”
I have great news for you: our ParseProgram method and the parser will look pretty similar!
Let’s get to work!
Again, we’re starting with a test before we flesh out ParseProgram. Here is a test case to make
sure that the parsing of let statements works:
// parser/parser_test.go
package parser
import (
"testing"
"monkey/ast"
"monkey/lexer"
)
func TestLetStatements(t *testing.T) {
input := `
let x = 5;
let y = 10;
let foobar = 838383;
`
l := lexer.New(input)
p := New(l)
program := p.ParseProgram()
if program == nil {
t.Fatalf("ParseProgram() returned nil")
}
if len(program.Statements) != 3 {
t.Fatalf("program.Statements does not contain 3 statements. got=%d",
len(program.Statements))
}
tests := []struct {
expectedIdentifier string
}{
{"x"},
{"y"},
38
{"foobar"},
}
for i, tt := range tests {
stmt := program.Statements[i]
if !testLetStatement(t, stmt, tt.expectedIdentifier) {
return
}
}
}
func testLetStatement(t *testing.T, s ast.Statement, name string) bool {
if s.TokenLiteral() != "let" {
t.Errorf("s.TokenLiteral not 'let'. got=%q", s.TokenLiteral())
return false
}
letStmt, ok := s.(*ast.LetStatement)
if !ok {
t.Errorf("s not *ast.LetStatement. got=%T", s)
return false
}
if letStmt.Name.Value != name {
t.Errorf("letStmt.Name.Value not '%s'. got=%s", name, letStmt.Name.Value)
return false
}
if letStmt.Name.TokenLiteral() != name {
t.Errorf("s.Name not '%s'. got=%s", name, letStmt.Name)
return false
}
return true
}
The test case follows the same principle as the test for our lexer and pretty much every other unit
test we’re going to write: we provide Monkey source code as input and then set expectations on
what we want the AST - that’s produced by the parser - to look like. We do this by checking
as many fields of the AST nodes as possible to make sure that nothing is missing. I found that
a parser is a breeding ground for off-by-one bugs and the more tests and assertions it has the
better.
I choose not to mock or stub out the lexer and provide source code as input instead of tokens,
since that makes the tests much more readable and understandable. Of course there’s the
problem of bugs in the lexer blowing up tests for the parser and generating unneeded noise, but
I deem the risk too minimal, especially judged against the advantages of using readable source
code as input.
There are two noteworthy things about this test case. The first one is that we ignore the Value
field of the *ast.LetStatement. Why don’t we check if the integer literals (5, 10, …) are parsed
correctly? Answer: we’re going to! But first we need to make sure that the parsing of let
statements works and ignore the Value.
The second one is the helper function testLetStatement. It might seem like over-engineering
to use a separate function, but we’re going to need this function soon enough. And then it’s
going to make our test cases a lot more readable than lines and lines of type conversions strewn
39
about.
As an aside: we won’t look at all of the parser tests in this chapter, since they are just too long.
But the code provided with the book contains all of them.
That being said, the tests fail as expected:
$ go test ./parser
--- FAIL: TestLetStatements (0.00s)
parser_test.go:20: ParseProgram() returned nil
FAIL
FAIL monkey/parser 0.007s
It’s time to flesh out the ParseProgram() method of the Parser.
// parser/parser.go
func (p *Parser) ParseProgram() *ast.Program {
program := &ast.Program{}
program.Statements = []ast.Statement{}
for p.curToken.Type != token.EOF {
stmt := p.parseStatement()
if stmt != nil {
program.Statements = append(program.Statements, stmt)
}
p.nextToken()
}
return program
}
Doesn’t this look really similar to the parseProgram() pseudocode function we saw earlier? See!
I told you! And what it does is the same too.
The first thing ParseProgram does is construct the root node of the AST, an *ast.Program. It
then iterates over every token in the input until it encounters an token.EOF token. It does
this by repeatedly calling nextToken, which advances both p.curToken and p.peekToken. In
every iteration it calls parseStatement, whose job it is to parse a statement. If parseStatement
returned something other than nil, a ast.Statement, its return value is added to Statements slice
of the AST root node. When nothing is left to parse the *ast.Program root node is returned.
The parseStatement method looks like this:
// parser/parser.go
func (p *Parser) parseStatement() ast.Statement {
switch p.curToken.Type {
case token.LET:
return p.parseLetStatement()
default:
return nil
}
}
Don’t worry, the switch statement will get more branches. But for now, it only calls parseLetStatement when it encounters a token.LET token. And parseLetStatement is the method where
we turn our tests from red to green:
// parser/parser.go
40
func (p *Parser) parseLetStatement() *ast.LetStatement {
stmt := &ast.LetStatement{Token: p.curToken}
if !p.expectPeek(token.IDENT) {
return nil
}
stmt.Name = &ast.Identifier{Token: p.curToken, Value: p.curToken.Literal}
if !p.expectPeek(token.ASSIGN) {
return nil
}
// TODO: We're skipping the expressions until we
// encounter a semicolon
for !p.curTokenIs(token.SEMICOLON) {
p.nextToken()
}
return stmt
}
func (p *Parser) curTokenIs(t token.TokenType) bool {
return p.curToken.Type == t
}
func (p *Parser) peekTokenIs(t token.TokenType) bool {
return p.peekToken.Type == t
}
func (p *Parser) expectPeek(t token.TokenType) bool {
if p.peekTokenIs(t) {
p.nextToken()
return true
} else {
return false
}
}
It works! The tests are green:
$ go test ./parser
ok monkey/parser 0.007s
We can parse let statements! That’s amazing! But, wait, how?
Let’s start with parseLetStatement. It constructs an *ast.LetStatement node with the token it’s
currently sitting on (a token.LET token) and then advances the tokens while making assertions
about the next token with calls to expectPeek. First it expects a token.IDENT token, which it
then uses to construct an *ast.Identifier node. Then it expects an equal sign and finally it
jumps over the expression following the equal sign until it encounters a semicolon. The skipping
of expressions will be replaced, of course, as soon as we know how to parse them.
The curTokenIs and peekTokenIs methods do not need much of an explanation. They are
useful methods that we will see again and again when fleshing out the parser. Already, we
can replace the p.curToken.Type != token.EOF condition of the for-loop in ParseProgram with
!p.curTokenIs(token.EOF).
Instead of dissecting these tiny methods, let’s talk about expectPeek. The expectPeek method
41
is one of the “assertion functions” nearly all parsers share. Their primary purpose is to enforce
the correctness of the order of tokens by checking the type of the next token. Our expectPeek
here checks the type of the peekToken and only if the type is correct does it advance the tokens
by calling nextToken. As you’ll see, this is something a parser does a lot.
But what happens if we encounter a token in expectPeek that’s not of the expected type? At
the moment, we just return nil, which gets ignored in ParseProgram, which results in entire
statements being ignored because of an error in the input. Silently. You can probably imagine
that this makes debugging really tough. And since nobody likes tough debugging we need to
add error handling to our parser.
Thankfully, the changes we need to make are minimal:
// parser/parser.go
type Parser struct {
// [...]
errors []string
// [...]
}
func New(l *lexer.Lexer) *Parser {
p := &Parser{
l: l,
errors: []string{},
}
// [...]
}
func (p *Parser) Errors() []string {
return p.errors
}
func (p *Parser) peekError(t token.TokenType) {
msg := fmt.Sprintf("expected next token to be %s, got %s instead",
t, p.peekToken.Type)
p.errors = append(p.errors, msg)
}
The Parser now has an errors field, which is just a slice of strings. This field gets initialized
in New and the helper function peekError can now be used to add an error to errors when the
type of peekToken doesn’t match the expectation. With the Errors method we can check if the
parser encountered any errors.
Extending the test suite to make use of this is as easy as you’d expect:
// parser/parser_test.go
func TestLetStatements(t *testing.T) {
// [...]
program := p.ParseProgram()
checkParserErrors(t, p)
// [...]
}
func checkParserErrors(t *testing.T, p *Parser) {
errors := p.Errors()
42
if len(errors) == 0 {
return
}
t.Errorf("parser has %d errors", len(errors))
for _, msg := range errors {
t.Errorf("parser error: %q", msg)
}
t.FailNow()
}
The new checkParserErrors helper function does nothing more than check the parser for errors
and if it has any it prints them as test errors and stops the execution of the current test. Pretty
straightforward.
But nothing in our parser creates errors yet. By changing expectPeek we can automatically add
an error every time one of our expectations about the next token was wrong:
// parser/parser.go
func (p *Parser) expectPeek(t token.TokenType) bool {
if p.peekTokenIs(t) {
p.nextToken()
return true
} else {
p.peekError(t)
return false
}
}
If we now change our test case input from this
input := `
let x = 5;
let y = 10;
let foobar = 838383;
`
to this invalid input where tokens are missing
input := `
let x 5;
let = 10;
let 838383;
`
we can run our tests to see our new parser errors:
$ go test ./parser
--- FAIL: TestLetStatements (0.00s)
parser_test.go:20: parser has 3 errors
parser_test.go:22: parser error: "expected next token to be =,\
got INT instead"
parser_test.go:22: parser error: "expected next token to be IDENT,\
got = instead"
parser_test.go:22: parser error: "expected next token to be IDENT,\
got INT instead"
FAIL
FAIL monkey/parser 0.007s
As you can see, our parser showcases a neat little feature here: it gives us errors for each
43
erroneous statement it encounters. It doesn’t exit on the first one, potentially saving us the
grunt work of rerunning the parsing process again and again to catch all of the syntax errors.
That’s pretty helpful - even with line and column numbers missing.
2.5 - Parsing Return Statements
I said earlier that we’re going to flesh out our sparse looking ParseProgram method. Now’s the
time. We’re going to parse return statements. And the first step, as with let statements before
them, is to define the necessary structures in the ast package with which we can represent
return statements in our AST.
Here is what return statements look like in Monkey:
return 5;
return 10;
return add(15);
Experienced with let statements, we can easily spot the structure behind these statements:
return <expression>;
Return statements consist solely of the keyword return and an expression. That makes the
definition of ast.ReturnStatement really simple:
// ast/ast.go
type ReturnStatement struct {
Token token.Token // the 'return' token
ReturnValue Expression
}
func (rs *ReturnStatement) statementNode() {}
func (rs *ReturnStatement) TokenLiteral() string { return rs.Token.Literal }
There is nothing about this node that you haven’t seen before: it has a field for the initial token
and a ReturnValue field that will contain the expression that’s to be returned. We will again
skip the parsing of the expressions and the semicolon handling for now, but will come back
to this later. The statementNode and TokenLiteral methods are there to fulfill the Node and
Statement interfaces and look identical to the methods defined on *ast.LetStatement.
The test we write next also looks pretty similar to the one for let statements:
// parser/parser_test.go
func TestReturnStatements(t *testing.T) {
input := `
return 5;
return 10;
return 993322;
`
l := lexer.New(input)
p := New(l)
program := p.ParseProgram()
checkParserErrors(t, p)
if len(program.Statements) != 3 {
t.Fatalf("program.Statements does not contain 3 statements. got=%d",
44
len(program.Statements))
}
for _, stmt := range program.Statements {
returnStmt, ok := stmt.(*ast.ReturnStatement)
if !ok {
t.Errorf("stmt not *ast.returnStatement. got=%T", stmt)
continue
}
if returnStmt.TokenLiteral() != "return" {
t.Errorf("returnStmt.TokenLiteral not 'return', got %q",
returnStmt.TokenLiteral())
}
}
}
Of course these test cases will also have to be extended as soon as expression parsing is in place.
But that’s okay, tests are not immutable. But they are, in fact, failing:
$ go test ./parser
--- FAIL: TestReturnStatements (0.00s)
parser_test.go:77: program.Statements does not contain 3 statements. got=0
FAIL
FAIL monkey/parser 0.007s
So let’s make them pass by changing our ParseProgram method to also take token.RETURN tokens
into account:
// parser/parser.go
func (p *Parser) parseStatement() ast.Statement {
switch p.curToken.Type {
case token.LET:
return p.parseLetStatement()
case token.RETURN:
return p.parseReturnStatement()
default:
return nil
}
}
I could make a lot of fuzz about the parseReturnStatement method before showing it to you,
but, well, I won’t. Because it’s tiny. There is nothing to fuzz about.
// parser/parser.go
func (p *Parser) parseReturnStatement() *ast.ReturnStatement {
stmt := &ast.ReturnStatement{Token: p.curToken}
p.nextToken()
// TODO: We're skipping the expressions until we
// encounter a semicolon
for !p.curTokenIs(token.SEMICOLON) {
p.nextToken()
}
return stmt
}
45
I told you: it’s tiny. The only thing it does is construct a ast.ReturnStatement, with the current
token it’s sitting on as Token. It then brings the parser in place for the expression that comes
next by calling nextToken() and finally, there’s the cop-out. It skips over every expression until
it encounters a semicolon. That’s it. Our tests pass:
$ go test ./parser
ok monkey/parser 0.009s
It’s time to celebrate again! We can now parse all of the statements in the Monkey programming
language! That’s right: there are only two of them. Let statements and return statements. The
rest of the language consists solely of expressions. And that’s what we’re going to parse next.
2.6 - Parsing Expressions
Personally, I think that parsing expressions is the most interesting part of writing a parser. As
we just saw, parsing statements is relatively straightforward. We process tokens from “left to
right”, expect or reject the next tokens and if everything fits we return an AST node.
Parsing expressions, on the other hand, contains a few more challenges. Operator precedence
is probably the first one that comes to mind and is best illustrated with an example. Let’s say
we want to parse the following arithmetic expression:
5 * 5 + 10
What we want here is an AST that represents the expression like this:
((5 * 5) + 10)
That is to say, 5 * 5 needs to be “deeper” in the AST and evaluated earlier than the addition. In
order to produce an AST that looks like this, the parser has to know about operator precedences
where the precedence of * is higher than +. That’s the most common example for operator
precedence, but there are a lot more cases where it’s important. Consider this expression:
5 * (5 + 10)
Here the parenthesis group together the 5 + 10 expression and give them a “precedence bump”:
the addition now has to be evaluated before the multiplication. That’s because parentheses
have a higher precedence than the * operator. As we will soon see, there are a few more cases
where precedence is playing a crucial role.
The other big challenge is that in expressions tokens of the same type can appear in multiple
positions. In contrast to this, the let token can only appear
         */


    }
        /*
2.4 - Parser’s first steps: parsing let statements
In Monkey, variable bindings are statements of the following form:
let x = 5;
let y = 10;
let foobar = add(5, 5);
let barfoo = 5 * 5 / 10 + 18 - add(5, 5) + multiply(124);
let anotherName = barfoo;
These statements are called “let statements” and bind a value to the given name. let x = 5;
binds the value 5 to the name x. Our job in this section is to parse let statements correctly.
For now we’re going to skip parsing the expressions that produce the value of a given variable
binding and come back to this later - as soon as we know how to parse expressions on their
own.
What does it mean to parse let statements correctly? It means that the parser produces an
AST that accurately represents the information contained in the original let statement. That
sounds reasonable, but we don’t have an AST yet, nor do we know what it should look like. So
our first task is to take a close look at Monkey source code and see how it’s structured, so that
we can define the necessary parts of an AST that’s able to accurately represent let statements.
Here is a fully valid program written in Monkey:
    }
}

*/