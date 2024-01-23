package ast;

import java.util.ArrayList;

import parsing.Token;

public class ArrayNode extends Expr {

	public ArrayNode(Token token, ArrayList<Expr> elements) {
		super(token);
		
		this.childs.addAll(elements);
	}

}
