package ast;

import parsing.Token;
import scope.Scope;

public class ReturnStmt extends AST {
	public Scope scope;

	public ReturnStmt(Token token, Expr expr) {
		super(token);
		
		if(expr != null) {
			addChild(expr);
		}
	}

}
