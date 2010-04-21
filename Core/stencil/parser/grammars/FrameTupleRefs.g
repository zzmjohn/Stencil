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
tree grammar FrameTupleRefs;
options {
  tokenVocab = Stencil;
  ASTLabelType = CommonTree;  
  output = AST;
  filter = true;
}

@header{
  /** Makes all tuple refs qualified by their frame offsets.
   *  References in the current frame are offset by [0], others
   *  must be reference back to an earlier frame.
   **/
   
  package stencil.parser.string;
  
  import stencil.operator.module.*;
  import static stencil.parser.string.EnvironmentProxy.initialEnv;
  import static stencil.parser.string.EnvironmentProxy.extend;
}

@members {
  private ModuleCache modules;
  
  public FrameTupleRefs(TreeNodeStream input, ModuleCache modules) {
    super(input, new RecognizerSharedState());
    assert modules != null : "ModuleCache must not be null.";
    this.modules = modules;
  }
}

topdown
  : ^(p=PREDICATE value[initialEnv($p, modules)] op=. value[initialEnv($p, modules)])
  | ^(c=CALL_CHAIN callTarget[initialEnv($c, modules)]);
	catch [EnvironmentProxy.FrameException fe] {
	 if (c != null) {throw new RuntimeException("Error framing: " + c.toStringTree(), fe);}
	 else if (p != null) {throw new RuntimeException("Error framing: " + p.toStringTree(), fe);}
	 else {throw new Error("Error in framing: No root.");}
  }
	
callTarget[EnvironmentProxy env] 
  : ^(f=FUNCTION . . ^(LIST value[env]*) y=. callTarget[extend(env, $y, $f, modules)])
  | ^(PACK value[env]*);
          
value[EnvironmentProxy env]
  options{backtrack=true;}
  : ^(TUPLE_REF n=ID v=.) 
       -> {env.isFrameRef($n.text)}? ^(TUPLE_REF $n $v)		//Already is a frame ref, no need to extend 
       -> ^(TUPLE_REF NUMBER[Integer.toString(env.frameRefFor($n.text))] ^(TUPLE_REF $n $v))
  | ^(TUPLE_REF n=ID)   -> ^(TUPLE_REF NUMBER[Integer.toString(env.frameRefFor($n.text))] ^(TUPLE_REF $n))
  | ^(TUPLE_REF NUMBER) -> ^(TUPLE_REF NUMBER[Integer.toString(env.currentIndex())] ^(TUPLE_REF NUMBER))
  | .;
