package scope;

public class GlobalScope extends BaseScope {

	public GlobalScope() {
		super(null);
	}

	@Override
	public String getScopeName() {
		return "global";
	}
	
	@Override
	public int getNumLocals() {
		return 0;
	}
	
	@Override
	public VariableSymbol defineVariable(String name, boolean constant, boolean assigned) {
		VariableSymbol sym = new VariableSymbol(name, numVariables, constant, assigned);
		symbols.put(name, sym);
		sym.scope = this;
		++numVariables;
		
		return sym;
	}
}
