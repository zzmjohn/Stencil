package stencil.module.util;

import stencil.module.MetadataHoleException;
import stencil.tuple.prototype.TuplePrototype;

public final class FacetData {
	public static enum MemoryUse {FUNCTION, READER, WRITER, OPAQUE} 
	
	private final String name;
	private final MemoryUse memory;
	private final TuplePrototype prototype;
	private final String target;
	private final String counterpart;
	
	/**Copy everything but the memory usage from the passed facet data.  Use the new memory usage.**/
	public FacetData(FacetData fd, MemoryUse memory) {this(fd.name, fd.target, fd.counterpart, memory, fd.prototype);}

	public FacetData(String name, MemoryUse memory, String... fields) {this(name, name, name, memory, new TuplePrototype(fields));}
	public FacetData(String name, String counterpart, MemoryUse memory, String... fields) {this(name, name, counterpart, memory, new TuplePrototype(fields));}
	public FacetData(String name, MemoryUse memory, TuplePrototype prototype) {this(name, name, name, memory, prototype);}
	public FacetData(String name, String target, String counterpart, MemoryUse memory, TuplePrototype prototype) {
		
		//If there is no specified counterpart AND the memory use is compatible with counterpart, make it self-counterpart
		if ((counterpart == null || counterpart.trim().equals("")) 
			&& (memory.equals(MemoryUse.FUNCTION) || memory.equals(MemoryUse.READER))) {
			counterpart = name;
		}
		
		//If this a writer and there is no counterpart, don't continue...
		if (memory == MemoryUse.WRITER 
			&& (counterpart == null || counterpart.trim().equals("") || counterpart.equals(name))) {
			throw new MetadataHoleException("Must provide proper counterpart for WRITER facet: " + name);
		}
		
		this.name = name;
		this.target = target;
		this.counterpart = counterpart;
		this.memory = memory;
		this.prototype = prototype;
	}
	
	public FacetData(FacetData source) {
		this.name = source.name;
		this.target = source.target;
		this.counterpart = source.counterpart;
		this.memory = source.memory;
		this.prototype = source.prototype;
	}
	
	public String name() {return name;}
	public String target() {return target!=null?target:name;}
	public MemoryUse memUse() {return memory;}
	public TuplePrototype prototype() {return prototype;}
	public String counterpart() {return counterpart;}	
	
	public boolean mutative() {
		return memory == MemoryUse.WRITER || memory == MemoryUse.OPAQUE;
	}
}
