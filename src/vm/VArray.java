package vm;

import java.util.ArrayList;

public class VArray {
	public Value[] elements;

	
	public VArray(Value[] elements) {
		this.elements = elements;
	}
	
	public Value get(int i) {
		return elements[i];
	}
	

	
	public VArray add(Value v) {
		Value[] combined = new Value[elements.length + 1];
		
		for(int i = 0; i < elements.length; ++i) {
			combined[i] = elements[i];
		}
		
		combined[elements.length] = v;
		
		return new VArray(combined);
	}
	
	public int size() {
		return elements.length;
	}
	
	public VArray concat(VArray ar) {
		Value[] combined = new Value[elements.length + ar.elements.length];
		
		for(int i = 0; i < elements.length; ++i) {
			combined[i] = elements[i];
		}
		
		for(int i = 0; i < ar.elements.length; ++i) {
			combined[elements.length + i] = ar.elements[i];
		}
		
		return new VArray(combined);
	}
	
	
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("[");
		for(int i = 0; i < elements.length; ++i) {
			if(i > 0) {
				buf.append(", ");
			}
			
			buf.append(elements[i].data);
		}
		
		buf.append("]");
		
		return buf.toString();
	}
}
