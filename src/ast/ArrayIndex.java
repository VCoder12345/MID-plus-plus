package ast;

import parsing.Token;

public class ArrayIndex extends AST {

	public ArrayIndex(Token token, Expr index) {
		super(token);
		addChild(index);
	}

}
