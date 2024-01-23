package compiler;

import static parsing.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;

import ast.*;
import parsing.TokenType;
import scope.FunSymbol;
import scope.NativeFunSymbol;
import utils.Utils;
import vm.Code;
import vm.Op;
import vm.VMError;
import vm.Value;

//walks the AST and generates Bytecode
public class Compiler {
	public Code code = new Code(); //the compiler writes the bytecode into this code-instance
	//and it is later interpreted by the Virtual Machine
	public int currentLine; //the line of the current ast node (the line of the token of the ast)
	public String currentFile;
	//the key is the name and the value the id of the global
	private static final int MAX_JUMP_OFFSET = Short.MAX_VALUE; //the max offset that is possible 
	//to represent with two bytes
	private ArrayList<Integer> breakResolves = new ArrayList<>();
	private ArrayList<Integer> continueResolves = new ArrayList<>();
	
	public Compiler(int numGlobals) {
		this.code.numGlobals = numGlobals;
	}
	
	//looks at the type of the token of the node 
	//and calls an appropriate function or just emits the right bytes
	//most of the functions are also overloaded compile functions 
	//which take as a parameter the node cast to the specific type for the function
	public void compile(AST node) {
		//gets the line of the token of the ast node
		currentLine = node.getLine();
		currentFile = node.getToken().file;
	
		//what bytecode should be generated based on the type of the token of the ast
		switch(node.getType()) {
		case TRUE:
			emit(Op.TRUE);
			break;
		case NIL:
			emit(Op.NIL);
			break;
		case NUMBER:
			compileNumber((Expr) node);
			break;
		case STRING:
			compileString((Expr) node);
			break;
		case FALSE:
			emit(Op.FALSE);
			break;
		case ADD: case SUB: case MUL: case DIV: case POWER: case MODULO: 
		case SMALLER, SMALLER_EQUALS, BIGGER, BIGGER_EQUALS, EQUALS, BANG_EQUALS, LSQRBR:
			compile((BinOp) node);
			break;
		case NEGATE, BANG, NUMBER_TYPE, STRING_TYPE, BOOL_TYPE:
			compile((PrefixOp) node);
			break;
		case AND:
			compileAnd((BinOp) node);
			break;
		case OR:
			compileOr((BinOp) node);
			break;
		case OUT:
			compile((OutStmt) node);
			break;
		case STATEMENTS:
			compile((Block) node);
			break;
		case VAR:
			compile((VarDecl) node);
			break;
		case CONST:
			compile((VarDecl) node);
			break;
		case ASSIGN:
			compile((Assignment) node);
			break;
		case ADD_SELF, SUB_SELF, MUL_SELF, DIV_SELF:
			compile((SelfAssign) node);
			break;
		case ID:
			compileID((IDNode) node);
			break;
		case IF:
			compile((IfStmt)node);
			break;
		case WHILE:
			compile((WhileStmt)node);
			break;
		case IN:
			compile((InExpr)node);
			break;
		case FUN:
			compile((FunNode) node);
			break;
		case CALL:
			compile((FunCall) node);
			break;
		case ARGS:
			for(AST child : node.getChilds()) {
				compile(child);
			}
			break;
		case RETURN:
			compile((ReturnStmt) node);
			break;
		case FOR:
			compile((ForStmt) node);
			break;
		case CONTINUE:
			compileContinue(node);
			break;
		case BREAK:
			compileBreak(node);
			break;
		case ARRAY:
			compile((ArrayNode)node);
			break;
		case ARRAY_INDEX:
			compile(node.getChild(0));
			break;
		case IMPORT:
			for(AST child : node.getChilds()) {
				compile(child);
			}
			break;
		}
	}
	
	private void compile(ArrayNode node) {
		byte size = (byte) node.childCount();
		
		for(int i = size - 1; i >= 0; --i) {
			compile(node.getChild(i));
		}
		
		emit(Op.ARRAY_CREATE, size);
	}
	
	private void compileBreak(AST node) {
		int jmp = emitJump(Op.JUMP);
		
		breakResolves.add(jmp);
	}
	
	private void compileContinue(AST node) {
		int jmp = emitJump(Op.JUMP);
		
		continueResolves.add(jmp);
	}
	
	private void compileAnd(BinOp node) {
		compile(node.getChild(0));
		int jmp = emitJump(Op.FALSE_JUMP_PEEK);
		emit(Op.POP);
		
		compile(node.getChild(1));
		
		backpatching(jmp);
	}
	
	private void compileOr(BinOp node) {
		compile(node.getChild(0));
		int jmp = emitJump(Op.TRUE_JUMP_PEEK);
		emit(Op.POP);
		
		compile(node.getChild(1));
		
		backpatching(jmp);
	}

	private void compile(ForStmt node) {
		AST startClause = node.getChild(0);
		AST condClause = node.getChild(1);
		AST repeatClause = node.getChild(2);
		AST block = node.getChild(3);
		
		if(startClause != null) {
			compile(startClause);
		}
		
		int condPos = code.data.size(); 
		int exitJump = -1;
		if(condClause != null) {
			compile(condClause);
			exitJump = emitJump(Op.FALSE_JUMP);
		}
		
		compile(block);
		
		for(int jmp : continueResolves) {
			backpatching(jmp);
		}
		
		continueResolves.clear();
		if(repeatClause != null) {
			compile(repeatClause);
		}
		
		emitJumpBack(condPos);
		
		if(exitJump != -1) {
			backpatching(exitJump);
		}
		
		for(int jmp : breakResolves) {
			backpatching(jmp);
		}
		
		breakResolves.clear();
		
		if(startClause != null && startClause.getType() == TokenType.VAR) {
			emit(Op.POP);
		}
	}
	
	private void compile(ReturnStmt node) {
		if(node.childCount() > 0) {
			compile(node.getChild(0));
		}else {
			emit(Op.NIL);
		}
		emit(Op.RET);
		byte pops = (byte) node.scope.getNumLocals();
		
		emit(pops);
	}
	
	private void compile(FunNode node) {
		int jmp = emitJump(Op.JUMP);
		node.symbol.address = code.data.size() - 1;
		compile(node.getChild(1));
		emit(Op.NIL);
		emit(Op.RET, (byte)0);
		
		backpatching(jmp);
		
	}
	
	private void compile(FunCall node) {
		compile(node.getChild(0));
		
		Value val;
		
		if(node.symbol.isNative()) {
			val = new Value((NativeFunSymbol)node.symbol);
		}else {
			val = new Value((FunSymbol)node.symbol);
		}
		byte index = code.addConstant(val);
		
		if(node.symbol.isNative()) {
			emit(Op.CALL_NATIVE);
		}else {
			emit(Op.CALL);
		}
		
		emit(index);
		
		if(!node.fromExpression) {
			emit(Op.POP);
		}
	}
	
	private void compile(Block node) {
		for(AST child : node.getChilds()) {
			compile(child);
		}
		
		if(node.scope.getEnclosingScope() != null) {
			int locals = node.scope.getNumVariables();
			
			for(int i = 0; i < locals; ++i) {
				emit(Op.POP);
			}
		}
	}
	
	private void compile(IfStmt node) {
		//compile the conditional
		compile(node.getChild(0));
		//emit a false-jump for jumping if the conditional is false
		int thenJump = emitJump(Op.FALSE_JUMP);
		compile(node.getChild(1));
		

		//checks if there's a else-statement
		if(node.childCount() > 2) {
			//if this jump is reached, the else-statements should be skipped --> jump
			int elseJump = emitJump(Op.JUMP);
			//backpatches the thenJump, so it jumps to the else-statement
			backpatching(thenJump);
			compile(node.getChild(2));
			backpatching(elseJump);
		}else {
			//backpatches the thenJump, so it jumps to the end of the if-statement
			backpatching(thenJump);
		}

	}
	
	private void compile(WhileStmt node) {
		int whileCondPos = code.data.size();
		//compile the conditional
		compile(node.getChild(0));
		//emit a false-jump for jumping if the conditional is false
		int thenJump = emitJump(Op.FALSE_JUMP);
		compile(node.getChild(1));
		
		for(int jmp : continueResolves) {
			backpatching(jmp);
		}
		
		continueResolves.clear();
		
		emitJumpBack(whileCondPos);
		
		for(int jmp : breakResolves) {
			backpatching(jmp);
		}
		
		breakResolves.clear();

		//backpatches the thenJump, so it jumps to the end of the while-statement
		backpatching(thenJump);

	}
	
	//prints out error-messages for the compiler
	private CompilerError error(String msg) {
		Utils.printError(msg, currentLine, currentFile);
		return new CompilerError();
	}
	
	public void compileID(IDNode node) {
		//resolves the global, gets the id and emits bytecode for getting the global
		byte id = (byte) node.symbol.id;
		
		if(node.scope.getEnclosingScope() == null) {
			emit(Op.GET_GLOBAL);
		}else {
			emit(Op.GET_LOCAL);
		}
		
		emit(id);
	}
	
	public void compile(VarDecl node) {
		//creates a global, adds it to the globals dictionary and emits bytecode to define it
		IDNode idNode = (IDNode) node.getChild(0);
		byte id = (byte) idNode.symbol.id;
		
		if(node.childCount() > 1) {
			compile(node.getChild(1));
		}else {
			emit(Op.NIL);
		}
		
		
		if(idNode.scope.getEnclosingScope() == null) {
			emit(Op.SET_GLOBAL);
			
			emit(id);
		}else {
			emit(Op.DEFINE_LOCAL);
		}
		
		

	}
	
	public void setVar(IDNode node) {
		byte id = (byte) node.symbol.id;
		
		
		if(node.scope.getEnclosingScope() == null) {
			emit(Op.SET_GLOBAL);
		}else {
			emit(Op.SET_LOCAL);
		}
		
		emit(id);
	}
	
	public void compile(Assignment node) {
		//resolves the variable for this assignment and then emits bytecode for the assignment with the right id
		compile(node.getChild(1));
		
		handleVarAssign(node);
		
		
		
	}
	
	public void handleVarAssign(Assignment node) {
		if(node.childCount() > 2) {
			compile(node.getChild(2));
			IDNode idNode = (IDNode)node.getChild(0);
			compileID(idNode);
			
			emit(Op.ARRAY_SET_ELEMENT);
		}else {
			setVar((IDNode)node.getChild(0));
		}
	}
	
	public void compile(SelfAssign node) {
		IDNode idNode = (IDNode)node.getChild(0);
		compileID(idNode);
		
		if(node.childCount() > 2) {
			compile(node.getChild(2));
			emit(Op.ARRAY_ACCESS);
		}
		compile(node.getChild(1));
		
		
		
		switch(node.getType()) {
		case ADD_SELF:
			emit(Op.ADD);
			break;
		case SUB_SELF:
			emit(Op.SUB);
			break;
		case MUL_SELF:
			emit(Op.MUL);
			break;
		case DIV_SELF:
			emit(Op.DIV);
			break;
		}
		
		handleVarAssign(node);
	}
	
	
	//compiles out-statement
	public void compile(OutStmt node) {
		compile(node.getChild(0));
		
		emit(Op.OUT);
	}
	
	public void compile(InExpr node) {
		compile(node.getChild(0));
		
		emit(Op.IN);
	}
	
	//handels prefix-operators, like negation, casting, etc.
	public void compile(PrefixOp node) {
		compile(node.getChild(0));
		
		//emit bytecode for the specific prefix-operator
		switch(node.getType()) {
		case NEGATE:
			emit(Op.NEGATE);
			break;
		case BANG:
			emit(Op.NOT);
			break;
		case NUMBER_TYPE:
			emit(Op.NUM_CAST);
			break;
		case STRING_TYPE:
			emit(Op.STRING_CAST);
			break;
		case BOOL_TYPE:
			emit(Op.BOOL_CAST);
			break;
		}
		
	}
	
	//emits bytecode for binary operations like addition, subtraction, multiplication, etc.
	public void compile(BinOp node) {
		compile(node.getChild(0));
		compile(node.getChild(1));
		
		//which binary operation is it?
		switch(node.getType()) {
		case ADD:
			emit(Op.ADD);
			break;
		case SUB:
			emit(Op.SUB);
			break;
		case MUL:
			emit(Op.MUL);
			break;
		case DIV:
			emit(Op.DIV);
			break;
		case POWER:
			emit(Op.POWER);
			break;
		case MODULO:
			emit(Op.MODULO);
			break;
		case SMALLER:
			emit(Op.SMALLER);
			break;
		case BIGGER:
			emit(Op.BIGGER);
			break;
		case SMALLER_EQUALS:
			emit(Op.SMALLER_EQUALS);
			break;
		case BIGGER_EQUALS:
			emit(Op.BIGGER_EQUALS);
			break;
		case EQUALS:
			emit(Op.EQUALS);
			break;
		case BANG_EQUALS:
			emit(Op.BANG_EQUALS);
			break;
		case LSQRBR:
			emit(Op.ARRAY_ACCESS);
			break;
		}
	}
	
	//emits bytecode for a number
	public void compileNumber(Expr node) {
		double numberVal = Double.parseDouble(node.getValue());
		emitConstant(new Value(numberVal));
	}
	
	//emits bytecode for a string
	public void compileString(Expr node) {
		emitConstant(new Value(node.getValue()));
	}
	
	//stores the value in the constant-pool and emits a constant-op and the index
	private void emitConstant(Value value) {
		byte index = code.addConstant(value);
		emit(Op.CONSTANT, index);
	}
	
	//emits a jump instruction and an empty offset which is later changed to the real jump offset by backpatching
	private int emitJump(byte jumpInstr) {
		emit(jumpInstr);
		emit((byte)0xff);
		emit((byte)0xff);
		return code.data.size() - 2;
	}
	
	//emits a jump instruction back to address
	private void emitJumpBack(int address) {
		int offset = code.data.size() - address + 3;
		if(offset > MAX_JUMP_OFFSET) {
			//the offset is to big for two bytes
			throw error("Too much code to jump over");
		}
		
		
		emit(Op.JUMP_BACK);
		emit((byte)((offset >> 8) & 0xff));
		emit((byte)(offset & 0xff));
	}
	
	//uses backpatching to change the empty jump-offset to the one lading on the next instruction
	//jumpPos is the position of the jump-instruction in the code
	private void backpatching(int jumpPos) {
		int offset = code.data.size() - jumpPos - 2;
		
		if(offset > MAX_JUMP_OFFSET) {
			//the offset is to big for two bytes
			throw error("Too much code to jump over");
		}

		code.data.set(jumpPos, (byte)((offset >> 8) & 0xff));
		code.data.set(jumpPos + 1, (byte)(offset & 0xff));
	}
	
	
	//writes bytes into the code
	private void emit(byte... bytes) {
		for(byte b : bytes) {
			code.write(b, currentLine, currentFile);
		}
	}
}
