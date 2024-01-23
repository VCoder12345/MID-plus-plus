package ast;

import parsing.Token;

public class ForStmt extends AST {

	public ForStmt(Token token, AST startClause, Expr condClause, Assignment repeatClause, Block block) {
		super(token);
		
		addChild(startClause);
		addChild(condClause);
		addChild(repeatClause);
		addChild(block);
	}

}
