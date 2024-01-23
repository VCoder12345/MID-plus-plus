package vm;

import scope.FunSymbol;

public class StackFrame {
	public FunSymbol symbol;
	public int retAddress;
	public int retFp;
	
	public StackFrame(FunSymbol symbol, int retAddress, int retFp) {
		super();
		this.symbol = symbol;
		this.retAddress = retAddress;
		this.retFp = retFp;
	}
	
	
}
