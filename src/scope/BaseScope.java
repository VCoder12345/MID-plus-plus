package scope;

import java.util.HashMap;

public abstract class BaseScope implements Scope {
	public Scope enclosingScope;
	public HashMap<String, Symbol> symbols = new HashMap<>();
	public int numVariables = 0;

	public BaseScope(Scope enclosingScope) {
		super();
		this.enclosingScope = enclosingScope;
	}
	
	@Override
	public boolean contains(String name) {
		return symbols.containsKey(name);
	}
	@Override
	public Scope getEnclosingScope() {
		return enclosingScope;
	}
	
	@Override
	public void define(Symbol sym) {
		symbols.put(sym.name, sym);
	}

	@Override
	public VariableSymbol defineVariable(String name, boolean constant, boolean assigned) {
		VariableSymbol sym = new VariableSymbol(name, getNumLocals(), constant, assigned);
		symbols.put(name, sym);
		sym.scope = this;
		++numVariables;
		
		return sym;
	}
	
	@Override
	public int getNumVariables() {
		return numVariables;
	}
	
	@Override
	public int getNumLocals() {
		int result = getNumVariables();
		if(enclosingScope != null) {
			result += enclosingScope.getNumLocals();
		}
		
		return result;
	}

	@Override
	public Symbol resolve(String name) {
		Symbol sym = symbols.get(name);
		
		if(sym != null) return sym;
		
		if(enclosingScope != null) {
			return enclosingScope.resolve(name);
		}
		
		return null;
	}

	@Override
	public String toString() {
		return getScopeName() + ";" + symbols;
	}
	
	

}
