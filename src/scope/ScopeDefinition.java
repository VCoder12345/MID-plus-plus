package scope;

import ast.AST;
import ast.Assignment;
import ast.BinOp;
import ast.Block;
import ast.Expr;
import ast.ForStmt;
import ast.FunCall;
import ast.FunNode;
import ast.IDNode;
import ast.Parameter;
import ast.ReturnStmt;
import ast.SelfAssign;
import ast.VarDecl;
import compiler.CompilerError;
import utils.Utils;
import vm.ValueType;

import static parsing.TokenType.*;

public class ScopeDefinition {
	public Scope currentScope;
	public int currentLine; //the line of the current ast node (the line of the token of the ast)
	public String currentFile;
	
	public void build(AST node) {
		//gets the line of the token of the ast node
		currentLine = node.getLine();
		currentFile = node.getToken().file;
		switch(node.getType()) {
		case VAR:
			build((VarDecl) node);
			break;
		case CONST:
			build((VarDecl) node);
			break;
		case ADD_SELF, SUB_SELF, MUL_SELF, DIV_SELF:
		case ASSIGN:
			build((Assignment) node);
			break;
		case PARAM:
			build((Parameter) node);
			break;
		case ID:
			buildID((IDNode) node);
			break;
		case FUN:
			build((FunNode) node);
			break;
		case CALL:
			build((FunCall) node);
			break;
		case STATEMENTS:
			build((Block) node);
			break;
		case RETURN:
			build((ReturnStmt) node);
			break;
		case FOR:
			build((ForStmt) node);
			break;
		default:
			for(AST child : node.getChilds()) {
				build(child);
			}
		}
	}
	
	public void initGlobalScope() {
		currentScope = new GlobalScope();
		
		NativeFunctions natFuncs = new NativeFunctions();
		
		currentScope.define(new NativeFunSymbol("time", 0, currentScope, natFuncs::time));
		currentScope.define(new NativeFunSymbol("sqrt", 1, currentScope, natFuncs::sqrt, ValueType.NUMBER));
		currentScope.define(new NativeFunSymbol("randInt", 1, currentScope, natFuncs::randInt, ValueType.NUMBER));
		currentScope.define(new NativeFunSymbol("len", 1, currentScope, natFuncs::len, ValueType.ARRAY));
		currentScope.define(new NativeFunSymbol("floor", 1, currentScope, natFuncs::floor, ValueType.NUMBER));
	}
	
	public void build(ForStmt node) {
		beginLocalScope();
		
		for(AST child : node.getChilds()) {
			if(child == null) continue;
			build(child);
		}
		
		endLocalScope();
	}
	
	
	public void build(ReturnStmt node) {
		
		node.scope = currentScope;

		if(node.childCount() > 0) {
			build(node.getChild(0));
		}
	}
	
	public void build(Parameter node) {
		String varName = node.getValue();
		
		VariableSymbol sym = currentScope.defineVariable(varName, false, true);
		node.symbol = sym;
		
	}
	
	public void build(Block node) {
		if(currentScope == null) {
			initGlobalScope();
		}else {
			beginLocalScope();
		}
		
		for(AST child : node.getChilds()) {
			build(child);
		}
		
		node.scope = currentScope;
		
		if(currentScope.getEnclosingScope() != null) {
			endLocalScope();
		}
	}
	
	public void beginLocalScope() {
		currentScope = new LocalScope(currentScope);
	}
	
	public void endLocalScope() {
		currentScope = currentScope.getEnclosingScope();
	}
	

	
	
	public void build(VarDecl node) {
		boolean constant = node.getType() == CONST;
		boolean assigned = false;
		
		if(node.childCount() > 1) {
			build(node.getChild(1));
			assigned = true;
		}
		
		IDNode varNode = (IDNode) node.getChild(0);
		String varName = varNode.getValue();
		
		if(currentScope.contains(varName)) {
			throw error("variable " + varName + " is already defined");
		}
		
		
		VariableSymbol sym = currentScope.defineVariable(varName, constant, assigned);
		
		varNode.symbol = sym;
		varNode.scope = currentScope;
		

	}
	
	public void build(FunNode node) {
		String funName = node.getValue();
		
		if(currentScope.contains(funName)) {
			if(currentScope.resolve(funName) instanceof NativeFunSymbol) {
				throw error("the function " + funName + " is already defined natively");
			}else {
				throw error("function " + funName + " is already defined");
			}
		}
		AST params = node.getChild(0);
		FunSymbol sym = new FunSymbol(funName, params.childCount(), currentScope);
		currentScope.define(sym);
		
		node.symbol = sym;
		
		currentScope = sym;
		
		
		build(params);
		build(node.getChild(1));
		
		currentScope = sym.getEnclosingScope();
	}
	
	public void build(Assignment node) {
		IDNode varNode = (IDNode) node.getChild(0);
		String varName = varNode.getValue();
		
		build(varNode);
		
		if(varNode.symbol.constant) {
			if(varNode.symbol.assigned) {
				throw error("the variable " + varName + " is a constant and cannot be re-assigned");
			}else {
				varNode.symbol.assigned = true;
			}
		}
		
		
		
		build(node.getChild(1));
		if(node.childCount() > 2) {
			build(node.getChild(2));
		}
	}
	

	public void build(FunCall node) {
		node.scope = currentScope;
		build(node.getChild(0));
	}

	
	public void buildID(IDNode node) {
		String varName = node.getValue();
		Symbol sym = resolve(varName, true);
		if(sym instanceof FunSymbol) {
			throw error("cannot use the function " + varName + " as a variable");
		}
		node.symbol = (VariableSymbol) sym;
		node.scope = node.symbol.scope;
	}
	
	public Symbol resolve(String name, boolean variable) {
		Symbol sym = currentScope.resolve(name);
		String thing;
		if(variable) {
			thing = "variable";
		}else {
			thing = "function";
		}
		if(sym == null) {
			throw error("the " + thing + " " + name + " is not defined");
		}
		
		return sym;
	}
	
	//prints out error-messages for the compiler
	private CompilerError error(String msg) {
		Utils.printError(msg, currentLine, currentFile);
		return new CompilerError();
	}
}
