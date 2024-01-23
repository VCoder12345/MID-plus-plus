package ast;

import parsing.Token;
import scope.Scope;

public class Block extends AST {
	public Scope scope;

	public Block(Token token) {
		super(token);
	}

}
