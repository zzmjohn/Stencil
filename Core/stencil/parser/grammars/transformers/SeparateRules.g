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
 
/* Takes layer rules and separates them by target type.
 */
tree grammar SeparateRules;
options {
	tokenVocab = Stencil;
	ASTLabelType = CommonTree;	
	output = AST;
	filter = true;
}

@header {
	package stencil.parser.string;
	
	import stencil.parser.tree.*;
	import static stencil.parser.string.StencilParser.*;
}

@members {
   private StencilTree siftRules(List<Rule> rules, int type) {return siftRules(adaptor, rules, type, -1, null);}
 
   //This binding check will be a problem when animated bindings come into play
   public static StencilTree siftRules(TreeAdaptor adaptor, List<Rule> rules, int type, int binding, String label) {
      label = label != null ? label : StencilParser.tokenNames[type] ;
      StencilTree list = (StencilTree) adaptor.create(LIST, label);
      
      for(Rule r: rules) {
         if(r.getTarget().getType() == type) {
            if (binding < 0 || r.getBinding().getType() == binding) {
              adaptor.addChild(list, adaptor.dupTree(r));
            }
         }
      }
      return list;
   }

   protected StencilTree local(CommonTree source)         {return siftRules((List<Rule>) source, LOCAL);}   
   protected StencilTree canvas(CommonTree source)        {return siftRules((List<Rule>) source, CANVAS);}
   protected StencilTree view(CommonTree source)          {return siftRules((List<Rule>) source, VIEW);}
   protected StencilTree prefilter(CommonTree source)     {return siftRules((List<Rule>) source, PREFILTER);}
   protected StencilTree result(CommonTree source)        {return siftRules((List<Rule>) source, RESULT);}
}

topdown: ^(CONSUMES filters=. rules=.) 
		-> ^(CONSUMES 
              $filters 
              {prefilter(rules)} 
              {local(rules)} 
              {result(rules)} 
              {view(rules)} 
              {canvas(rules)});
	 
	 
	 
	 