package ast;

import parsing.Token;
import scope.Scope;
import scope.VariableSymbol;

//a node for the declaration of a new variable
public class VarDecl extends AST {
	
	//var '=' expr
	public VarDecl(IDNode var, Token token) {
		super(token);
		
		addChild(var);
	}

}
