package ast;

import parsing.Token;

public class SelfAssign extends Assignment {

	public SelfAssign(IDNode var, Token token, Expr expr, ArrayIndex index) {
		super(var, token, expr, index);
	}

	

}
