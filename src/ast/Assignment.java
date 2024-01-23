package ast;

import parsing.Token;
import scope.Scope;
import scope.VariableSymbol;

//a node for a assignment
public class Assignment extends AST {
	
	//var '=' expr
	public Assignment(IDNode var, Token token, Expr expr, ArrayIndex index) {
		super(token);
		
		addChild(var);
		addChild(expr);
		if(index != null)
			addChild(index);
	}
}
