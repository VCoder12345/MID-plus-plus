package ast;

import parsing.Token;
import scope.BaseFunSymbol;
import scope.FunSymbol;
import scope.Scope;

public class FunCall extends Expr {
	public BaseFunSymbol symbol;
	public Scope scope;
	public boolean fromExpression;
	
	public FunCall(Token token, Args arguments, boolean fromExpression) {
		super(token);
		this.fromExpression = fromExpression;
		
		addChild(arguments);
	}

}
