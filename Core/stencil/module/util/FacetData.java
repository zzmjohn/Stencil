package stencil.module.util;

import java.util.Arrays;

import stencil.tuple.prototype.SimplePrototype;
import stencil.tuple.prototype.TuplePrototype;

public final class FacetData {
	public static enum MemoryUse {FUNCTION, READER, WRITER, OPAQUE} 
	
	private String name;
	private MemoryUse memory;
	private TuplePrototype prototype;
	private String target;
	private String roles;
	
	public FacetData(String name, MemoryUse memory, String... fields) {this(name, memory, new SimplePrototype(Arrays.asList(fields)));}
	public FacetData(String name, MemoryUse memory, TuplePrototype prototype) {this(name, name, memory, prototype);}
	public FacetData(String name, String target, MemoryUse memory, TuplePrototype prototype) {
		this.name = name;
		this.target = target;
		this.memory = memory;
		this.prototype = prototype;
	}
	
	public FacetData(FacetData source) {
		this.name = source.name;
		this.memory = source.memory;
		this.prototype = source.prototype;
	}
	
	public void setName(String name) {this.name = name;}
	public String getName() {return name;}
	
	public String getTarget() {return target!=null?target:name;}
	
	public String getRoles() {return roles;}
	public void setRoles(String roles) {this.roles = roles;}
	public boolean hasRole(String role) {return roles.indexOf(role) >=0;}
	
	public void setMemory(String memUse) {memory = MemoryUse.valueOf(memUse);}
	public MemoryUse getMemUse() {return memory;}
	public void setMemUse(MemoryUse memUse) {memory = memUse;}
	
	public void setPrototype(TuplePrototype prototype) {this.prototype = prototype;}
	public TuplePrototype getPrototype() {return prototype;}
	
	public boolean mutative() {
		return memory == MemoryUse.WRITER && memory == MemoryUse.OPAQUE;
	}
}
