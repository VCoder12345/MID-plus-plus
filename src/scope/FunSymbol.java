package scope;

public class FunSymbol extends BaseFunSymbol {
	public int address;
	public FunSymbol(String name, int numArgs, Scope enclosingScope) {
		super(name, numArgs, enclosingScope);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		return "function " + name + " at " + address;
	}

	@Override
	public boolean isNative() {
		// TODO Auto-generated method stub
		return false;
	}

}
