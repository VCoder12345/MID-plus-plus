package ast;

import parsing.Token;
import scope.VariableSymbol;

public class Parameter extends AST {
	public VariableSymbol symbol;

	public Parameter(Token token) {
		super(token);
	}

}
