package scope;

import ast.AST;
import ast.FunCall;
import compiler.CompilerError;
import utils.Utils;

public class ScopeResolution {
	public int currentLine; //the line of the current ast node (the line of the token of the ast)
	public String currentFile;
	
	public void build(AST node) {
		//gets the line of the token of the ast node
		currentLine = node.getLine();
		currentFile = node.getToken().file;
		
		switch(node.getType()) {
		case CALL:
			build((FunCall) node);
			break;
		default:
			for(AST child : node.getChilds()) {
				if(child == null) continue;
				build(child);
			}
		}
	}
	
	public void build(FunCall node) {
		String funName = node.getValue();
		
		Symbol sym = resolve(funName, node.scope);
		if(sym instanceof VariableSymbol) {
			throw error("cannot use the variable " + funName + " as a function");
		}
		
		
		node.symbol = (BaseFunSymbol) sym;
		AST args = node.getChild(0);
		int numArgs = node.symbol.numArgs;
		int passed = args.childCount();
		if(passed != numArgs) {
			throw error("the function " + funName + " needs " + numArgs + " arguments, but is called with " + passed + " arguments");
		}
		
		build(args);
	}
	
	public Symbol resolve(String name, Scope scope) {
		Symbol sym = scope.resolve(name);
		if(sym == null) {
			throw error("the function " + name + " is not defined");
		}
		
		return sym;
	}
	
	//prints out error-messages for the compiler
		private CompilerError error(String msg) {
			Utils.printError(msg, currentLine, currentFile);
			return new CompilerError();
		}
}
