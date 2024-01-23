package parsing;

import static parsing.TokenType.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import ast.AST;
import ast.Args;
import ast.ArrayIndex;
import ast.ArrayNode;
import ast.Assignment;
import ast.BinOp;
import ast.Block;
import ast.Expr;
import ast.ForStmt;
import ast.FunCall;
import ast.FunNode;
import ast.IDNode;
import ast.IfStmt;
import ast.InExpr;
import ast.OutStmt;
import ast.Parameter;
import ast.Params;
import ast.PrefixOp;
import ast.ReturnStmt;
import ast.SelfAssign;
import ast.VarDecl;
import ast.WhileStmt;
import utils.Utils;

//uses a lexer to generate tokens on the fly and parses them to generate an AST
//uses recursive-descent 
//the functions of the parser are mostly derived of the grammar of MID
public class Parser {
	private static final int MAX_LOOKAHEAD = 2; //how big should the lookahead-buffer be?
	private Lexer lexer; //the lexer which generates the tokens
	private Token[] lookaheadBuffer = new Token[MAX_LOOKAHEAD]; //a buffer of the next MAX_LOOKAHEAD tokens
	private Token lastToken; //the token before the current one
	private int bufferPos = 0; //the position of the current token in the buffer
	private String sourceDir; //the directory of the parsed sourcecode
	
	public Parser(Lexer lexer, String sourceDir) {
		super();
		this.lexer = lexer;
		this.sourceDir = sourceDir;
		//initializes the lookahead buffer
		fillLookaheadBuffer();
	}
	
	public Parser(Lexer lexer) {
		this(lexer, "");
	}
	
	//fills the empty lookahead buffer with tokens until it's full
	private void fillLookaheadBuffer() {
		for(int i = 0; i < MAX_LOOKAHEAD; ++i) {
			lookaheadBuffer[i] = lexer.nextToken();
		}
	}

	//la=look ahead -> looks x tokens ahead (x < MAX_LOOKAHEAD)
	private Token la(int x) {
		return lookaheadBuffer[(bufferPos + x) % MAX_LOOKAHEAD];
	}
	
	//lt = looks x tokens ahead (x < MAX_LOOKAHEAD) and returns it's type
	private TokenType lt(int x) {
		return la(x).type;
	}

	//prints out error-messages
	private ParsingError error(String msg) {
		Token ct = la(0);
		Utils.printError(msg, ct.line, ct.file);
		return new ParsingError();
	}
	
	//if the type of the current token equals "type", the parser gets the next token otherwise it produces an error
	private void consume(TokenType type) {
		if(lt(0) == type) {
			advance();
		}else {
			throw error("expected a token of type " + type + " but found " + la(0));
		}
	}
	
	//generates the next token with the lexer and saves it in the lookahead buffer
	private void advance() {
		lastToken = la(0);
		
		lookaheadBuffer[bufferPos] = lexer.nextToken();
		bufferPos = (bufferPos + 1) % MAX_LOOKAHEAD;
	}
	
	//if the type of the current token equals any of the types of "types", the parser advances and returns true; otherwise the parser just returns false
	private boolean match(TokenType...types) {
		if(check(types)) {
			advance();
			return true;
		}
		
		return false;
	}
	
	//checks if the current-token-type equals any of the types of "types"
	private boolean check(TokenType...types) {
		return check(0, types);
	}
	
	//checks if the token x tokens ahead equals any of the types of "types"
	private boolean check(int x, TokenType...types) {
		for(TokenType type : types) {
			if(lt(x) == type) {
				return true;
			}
		}
		
		return false;
	}
	
	//the production "primary" of the grammar
	private Expr primary() {
		if(match(LBRACK)) {
			Expr node = expr();
			consume(RBRACK);
			
			return node;
		}else {
			return type();
		}
	}
	
	//the production "type" of the grammar
	private Expr type() {
		Token typeToken = la(0);
		Expr node;
		if(match(ID)) {
			if(check(LBRACK)) {
				node = funCall(true);
			}else {
				node = new IDNode(typeToken);
			}
		}else if(match(LSQRBR)) {
			node = array();
		}else {
			//is it not any of the possible types? --> error
			if(!match(NUMBER) 
					&& !match(STRING) 
					&& !match(TRUE) 
					&& !match(FALSE)
					&& !match(NIL)) {
				throw error("expected number, string, boolean, nil or variable but found " + la(0));
			}
			
			node = new Expr(typeToken);
		}
		
		if(match(LSQRBR)) {
			Token acToken = lastToken;
			var indexNode = expr();
			consume(RSQRBR);
			
			node = new BinOp(node, acToken, indexNode);
		}
		
		return node;
	}
	
	private AST importStmt() throws IOException {
		Token token = lastToken;

		Token strToken = la(0);
		consume(STRING);
		
		String importCode = Files.readString(Path.of(sourceDir + strToken.value));
		
		Lexer impLexer = new Lexer(importCode, strToken.value);
		Parser impParser = new Parser(impLexer, sourceDir);
		AST importTree = impParser.parse();
		
		return new AST(token, importTree.getChilds());
	}
	
	private ArrayNode array() {
		ArrayList<Expr> elements = new ArrayList<>();
		if(!check(RSQRBR)) {
			elements.add(expr());
			while(match(COMMA)) {
				elements.add(expr());
			}
		}

		consume(RSQRBR);
		
		return new ArrayNode(new Token(ARRAY, lastToken.line, lastToken.file), elements);
	}
	
	//the production "unary" of the grammar
	private Expr unary() {
		if(match(BANG)) {
			Token bangToken = lastToken;
			Expr result = primary();
			result = new PrefixOp(bangToken, result);
			
			return result;
		}else if(check(LBRACK) && check(1, NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)){
			consume(LBRACK);
			Token castTypeToken = la(0);
			typeName();
			consume(RBRACK);
			
			return new PrefixOp(castTypeToken, primary());
		}else{
			//checks how many negate-operators are in front of the value --> only if the number of negate-operators is uneven, a negate-token is needed
			int negateOccurences = 0;
			while(check(ADD, SUB)) {
				if(match(SUB)) {
					++negateOccurences;
				}else {
					consume(ADD);
				}
			}
			
			Expr result = primary();
			if(negateOccurences % 2 != 0) {
				result = new PrefixOp(new Token(NEGATE, result.getLine(), result.getToken().file), result);
			}
			
			return result;
		}
		
	}
	
	//the production "type-name" of the grammar
	private void typeName() {
		if(!match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
			throw error("expected a number-, string- or bool-type, but found " + la(0));
		}
	}
	
	private SelfAssign selfAssign(IDNode idNode, ArrayIndex index) {
		Token token = la(0);
		if(!match(ADD_SELF, SUB_SELF, MUL_SELF, DIV_SELF)) {
			throw error("expected +=, -=, *= or /=, but found " + token);
		}
		
		Expr val = expr();
		
		return new SelfAssign(idNode, token, val, index);
	}
	
	//the production "power" of the grammar
	private Expr power() {
		Expr result = unary();
		while(match(POWER)) {
			result = new BinOp(result, lastToken,  unary());
		}
		
		return result;
	}
	
	//the production "factor" of the grammar
	private Expr factor() {
		Expr result = power();
		while(match(MUL, DIV, MODULO)) {
			result = new BinOp(result, lastToken, power());
		}
		
		return result;
	}
	
	//the production "term" of the grammar
	private Expr term() {
		Expr result = factor();
		while(match(ADD, SUB)) {
			result = new BinOp(result, lastToken, factor());
		}
		
		return result;
	}
	
	//the production "comparison" of the grammar
	private Expr comparison() {
		Expr result = term();
		while(match(SMALLER, SMALLER_EQUALS, BIGGER, BIGGER_EQUALS)) {
			result = new BinOp(result, lastToken, term());
		}
		
		return result;
	}
	
	//the production "equality" of the grammar
	private Expr equality() {
		Expr result = comparison();
		while(match(EQUALS, BANG_EQUALS)) {
			result = new BinOp(result, lastToken, comparison());
		}
		
		return result;
	}
	
	//the production "expr" of the grammar
	private Expr expr()  {
		return inExpr();
	}
	
	private Expr or() {
		Expr result = and();
		while(match(OR)) {
			result = new BinOp(result, lastToken, and());
		}
		
		return result;
	}
	
	private IDNode target() {
		Token varToken = lastToken;
		

		return new IDNode(varToken);
	}
	
	private Expr and() {
		Expr result = equality();
		while(match(AND)) {
			result = new BinOp(result, lastToken, equality());
		}
		
		return result;
	}
	
	//the production "out-statement" of the grammar
	private OutStmt outStatement() {
		Token token = lastToken;
		Expr exprVal = expr();
		
		return new OutStmt(token, exprVal);
	}
	
	//the production "assignment" of the grammar
	private Assignment assignment() {
		IDNode varNode = target();
		
		ArrayIndex arIndex = null;
		if(match(LSQRBR)) {
			var index = expr();
			consume(RSQRBR);
			
			arIndex = new ArrayIndex(new Token(ARRAY_INDEX, lastToken.line, lastToken.file), index);
		}
		
		
		if(match(ASSIGN)) {
			Token token = lastToken;
			Expr exprNode = expr();
			
			return new Assignment(varNode, token, exprNode, arIndex);
		}else {
			return selfAssign(varNode, arIndex);
		}
		
		
	}
	
	private FunCall funCall(boolean fromExpression) {
		Token token = lastToken;
		
		Args arguments = args();
		
		return new FunCall(new Token(CALL, token.value, token.line, token.file), arguments, fromExpression);
	}
	
	private Args args() {
		consume(LBRACK);
		
		Args args = new Args(new Token(TokenType.ARGS, lastToken.line, lastToken.file));
		if(check(exprTypes())) {
			args.addChild(expr());
			while(match(COMMA)) {
				args.addChild(expr());
			}
		}
		
		consume(RBRACK);
		
		return args;
	}
	
	private TokenType[] exprTypes() {
		return new TokenType[] {
				NUMBER, STRING, TRUE, FALSE, NIL, ID, LBRACK, LSQRBR
		};
	}
	
	//the production "var-decl" of the grammar
	private VarDecl varDecl() {
		Token token = lastToken;
		consume(ID);
		Token varToken = lastToken;
		VarDecl node = new VarDecl(new IDNode(varToken), token);
		
		if(match(ASSIGN)) {
			Expr exprNode = expr();
			node.addChild(exprNode);
		}
		
		return node;
	}
	
	private ForStmt forStatement() {
		Token token = lastToken;
		
		AST startClause = null;
		if(match(VAR)) {
			startClause = varDecl();
		}else if(match(ID)) {
			startClause = assignment();
		}
		
		consume(COMMA);
		
		Expr condClause = null;
		if(check(exprTypes())) {
			condClause = expr();
		}
		
		consume(COMMA);
		
		Assignment repeatClause = null;
		
		if(match(ID)) {
			repeatClause = assignment();
		}
		
		Block block = statements();
		
		consume(END);
		
		return new ForStmt(token, startClause, condClause, repeatClause, block);
	}
	
	//the production "statement" of the grammar
	private AST statement() {
		AST node = null;
		if(match(OUT)) {
			node = outStatement();
		}else if(match(VAR, CONST)) {
			node = varDecl();
		}else if(match(ID)) {
			if(check(LBRACK)) {
				node = funCall(false);
			}else {
				node = assignment();
			}
			
		}else if(match(IF)) {
			node = ifStatement();
		}else if(match(WHILE)) {
			node = whileStatement();
		}else if(match(RETURN)) {
			node = returnStatement();
		}else if(match(FOR)) {
			return forStatement();
		}else if(match(CONTINUE, BREAK)) {
			return new AST(lastToken);
		}
		
		//at the end of a statement there can either be a NEWLINE or the EOF Token
		if(!match(NEWLINE, EOF)) {
			throw error("expected newline at the end of a statement, but found " + la(0));
		}
		
		return node;
	}
	
	//the production "in-expr" of the grammar
	private Expr inExpr() {
		if(match(IN)) {
			return new InExpr(lastToken, or());
		}else {
			return or();
		}
	}
	
	//the production "function" of the grammar
	private FunNode function() {
		Token idToken = la(0);
		consume(ID);
		
		Params parameters = params();
		
		Block block = statements();
		
		consume(END);
		
		return new FunNode(new Token(FUN, idToken.value, idToken.line, idToken.file), parameters, block);
	}
	
	private Params params() {
		consume(LBRACK);
		
		Params parameters = new Params(new Token(TokenType.PARAMS, lastToken.line, lastToken.file));
		if(match(ID)) {
			parameters.addChild(new Parameter(new Token(TokenType.PARAM, lastToken.value, lastToken.line, lastToken.file)));
			while(match(COMMA)) {
				consume(ID);
				parameters.addChild(new Parameter(new Token(TokenType.PARAM, lastToken.value, lastToken.line, lastToken.file)));
			}
		}
		
		consume(RBRACK);
		
		return parameters;
	}
	
	private ReturnStmt returnStatement() {
		Token token = lastToken;
		Expr retExpr = null;
		if(check(NUMBER, STRING, TRUE, FALSE, NIL, ID, LBRACK)) {
			retExpr = expr();
		}
		
		return new ReturnStmt(token, retExpr);
	}
	
	//the production "while-statement" of the grammar
	private WhileStmt whileStatement() {
		Token token = lastToken;
		Expr conditional = expr();
		AST stmts = statements();
		
		consume(END);
		
		return new WhileStmt(token, conditional, stmts);
	}
	
	//the production "if-statement" of the grammar
	private IfStmt ifStatement() {
		Token token = lastToken;
		Expr conditional = expr();
		AST stmts = statements();
		
		AST elseStmts = null;
		if(match(ELSE)) {
			elseStmts = statements();
		}
		consume(END);
		
		return new IfStmt(token, conditional, stmts, elseStmts);
	}
	
	//the production "statements" of the grammar
	private Block statements() {
		Block result = new Block(new Token(STATEMENTS, 0, la(0).file));
		while(check(OUT, VAR, CONST, IF, WHILE, ID, RETURN, FOR, NEWLINE, CONTINUE, BREAK)) {
			AST node = statement();
			//if the ast-node is important, add it to the ast
			if(node != null) { 
				result.addChild(node);
			}
		}
		
		return result;
	}
	
	private AST mainBlock() {
		Block result = new Block(new Token(STATEMENTS, 0, la(0).file));
		while(check(OUT, VAR, CONST, IF, WHILE, ID, FUN, RETURN, FOR, NEWLINE, IMPORT)) {
			AST node = null;
			if(match(FUN)) {
				node = function();
			}else if(match(IMPORT)) {
				try {
					node = importStmt();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.err.println("Could not import file");
				}
			}else {
				node = statement();
			}
			//if the ast-node is important, add it to the ast
			if(node != null) { 
				result.addChild(node);
			}
		}
		
		return result;
	}
	
	//the main function of the parser:
	//parses the tokens generated by the lexer and returns an AST (abstract syntax tree)
	public AST parse() {
		return mainBlock();
	}
}
