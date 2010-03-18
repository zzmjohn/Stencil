/* Copyright (c) 2006-2008 Indiana University Research and Technology Corporation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * - Neither the Indiana University nor the names of its contributors may be used
 *  to endorse or promote products derived from this software without specific
 *  prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
/**Operators over a properly formed-AST and ensures that
 * all guides are up-to date.
 */ 
tree grammar NeedsGuides;
options {
	tokenVocab = Stencil;
	ASTLabelType = CommonTree;	
	filter = true;
}

@header{
	package stencil.interpreter;
	
	import stencil.parser.tree.*;
	import stencil.tuple.NumericSingleton;	
}

@members {
	private static final Object[] EMPTY_ARGS = new Object[0];
	private static final Map<Object, Integer> stateIDs = new HashMap(); //TODO: Would this be any faster as an array? (The length and offsets can be known at Stencil compile time.)
	private boolean needsGuide;
	 	
	public boolean check(Program program) {
		needsGuide = false;
		downup(program);
		return needsGuide;
	}
	
	public boolean needsGuide(Tree t) {
    AstInvokeable i = (AstInvokeable) t;
    int nowID = ((NumericSingleton) i.getInvokeable().invoke(EMPTY_ARGS)).intValue(); //TODO: Look at not returning a tuple from StateID
         
    if (!stateIDs.containsKey(i)) {
       stateIDs.put(i, nowID+1); //Make it different...
    }
    int cachedID = stateIDs.get(i);
    stateIDs.put(i, nowID);
    return (cachedID != nowID);
	}
}

topdown: ^(GUIDE_QUERY target*);
target
	: i=AST_INVOKEABLE 
	  {needsGuide =  needsGuide || needsGuide(i);};
