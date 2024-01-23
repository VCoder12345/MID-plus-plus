package scope;

import java.util.Random;

import vm.Value;

public class NativeFunctions {
	public Value time(NativeArgs args) {
		return new Value(System.currentTimeMillis());
	}
	
	public Value sqrt(NativeArgs args) {
		Value val = args.values[0];
		
		return new Value(Math.sqrt(val.asNumber()));
	}
	
	public Value randInt(NativeArgs args) {
		Value val = args.values[0];
		
		return new Value(new Random().nextInt((int)val.asNumber()));
	}
	
	public Value len(NativeArgs args) {
		Value val = args.values[0];
		
		return new Value(val.asArray().size());
	}
	
	public Value floor(NativeArgs args) {
		Value val = args.values[0];
		
		return new Value(Math.floor(val.asNumber()));
	}
}
