package ast;

import parsing.Token;
import scope.Scope;
import scope.VariableSymbol;

public class IDNode extends Expr {
	public VariableSymbol symbol = null;
	public Scope scope;

	public IDNode(Token token) {
		super(token);
		// TODO Auto-generated constructor stub
	}

}
