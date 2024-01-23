package ast;

//prints the ast by treewalking it
public class ASTPrinter {
	public void print(AST node) {
		switch(node.getType()) {
		case OUT:
			print((OutStmt)node);
			break;
		case MUL: case DIV: case ADD: case SUB: case POWER:
			print((BinOp)node);
			break;
		case NEGATE:
			print((PrefixOp) node);
			break;
		case NUMBER:
			printValue((Expr) node);
			break;
		case STATEMENTS:
			for(AST child : node.getChilds()) {
				print(child);
				System.out.println();
			}
			break;
		}
	}
	
	public void print(OutStmt node) {
		System.out.print("out ");
		print(node.getChild(0));
	}

	
	public void printValue(Expr node) {
		System.out.print(node.getValue());
	}
	
	public void print(PrefixOp node) {
		System.out.print("(");
		System.out.print("-");
		print(node.getChild(0));
		System.out.print(")");
	}
	
	public void print(BinOp node) {
		System.out.print("(");
		System.out.print(node.getValue() + " ");
		print(node.getChild(0));
		System.out.print(" ");
		print(node.getChild(1));
		System.out.print(")");
	}
}
