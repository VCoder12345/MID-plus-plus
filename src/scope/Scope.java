package scope;

public interface Scope {
	public String getScopeName();
	public Scope getEnclosingScope();
	public VariableSymbol defineVariable(String name, boolean constant, boolean assigned);
	public void define(Symbol sym);
	public Symbol resolve(String name);
	public boolean contains(String name);
	public int getNumLocals();
	public int getNumVariables();
}
