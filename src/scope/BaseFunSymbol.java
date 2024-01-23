package scope;

import java.util.HashMap;

public abstract class BaseFunSymbol extends Symbol implements Scope {
	

	private Scope enclosingScope;
	public HashMap<String, Symbol> argSymbols = new HashMap<>();
	public int numArgs; 
	public int numSymbols = 0;
	//public int 

	public BaseFunSymbol(String name, int numArgs, Scope enclosingScope) {
		super(name);
		this.enclosingScope = enclosingScope;
		this.numArgs = numArgs;
	}
	
	public abstract boolean isNative();
	@Override
	public String getScopeName() {
		return name;
	}

	@Override
	public Scope getEnclosingScope() {
		return enclosingScope;
	}

	@Override
	public VariableSymbol defineVariable(String name, boolean constant, boolean assigned) {
		VariableSymbol sym = new VariableSymbol(name, numSymbols - numArgs, constant, assigned);
		argSymbols.put(name, sym);
		sym.scope = this;
		
		++numSymbols;
		return sym;
	}

	@Override
	public Symbol resolve(String name) {
		Symbol sym = argSymbols.get(name);
		
		if(sym != null) return sym;
		
		return enclosingScope.resolve(name);
	}

	@Override
	public boolean contains(String name) {
		return argSymbols.containsKey(name);
	}

	@Override
	public int getNumLocals() {
		return 0;
	}

	@Override
	public int getNumVariables() {
		return 0;
	}

	@Override
	public void define(Symbol sym) {
	}

}
