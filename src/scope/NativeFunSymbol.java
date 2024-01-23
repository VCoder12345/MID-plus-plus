package scope;

import java.util.function.Function;

import vm.Value;
import vm.ValueType;

public class NativeFunSymbol extends BaseFunSymbol {
	public Function<NativeArgs, Value> function;
	public ValueType[] argTypes;
	
	public NativeFunSymbol(String name, int numArgs, Scope enclosingScope, Function<NativeArgs, Value> function, ValueType...argTypes) {
		super(name, numArgs, enclosingScope);
		this.function = function;
		this.argTypes = argTypes;
	}

	@Override
	public boolean isNative() {
		return true;
	}

}
