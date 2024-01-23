package ast;

import parsing.Token;
import scope.FunSymbol;

public class FunNode extends AST {
	public FunSymbol symbol;

	public FunNode(Token token, Params params, Block statements) {
		super(token);
		
		addChild(params);
		addChild(statements);
	}

}
