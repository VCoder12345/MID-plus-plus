package scope;

public class VariableSymbol extends Symbol {
	public int id;
	public boolean constant;
	public boolean assigned;
	
	public VariableSymbol(String name, int id, boolean constant, boolean assigned) {
		super(name);
		this.id = id;
		this.constant = constant;
		this.assigned = assigned;
	}

}
