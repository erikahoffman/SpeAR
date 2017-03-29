package com.rockwellcollins.spear.translate.transformations;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.SimpleAttributeResolver;

import com.rockwellcollins.spear.Constant;
import com.rockwellcollins.spear.Pattern;
import com.rockwellcollins.spear.Specification;
import com.rockwellcollins.spear.TypeDef;
import com.rockwellcollins.spear.translate.intermediate.PatternDocument;
import com.rockwellcollins.spear.translate.intermediate.SpearDocument;
import com.rockwellcollins.spear.translate.intermediate.TypeDocument;

public class CreateUserNamespace {

	public static Map<EObject,Map<String,String>> transform(SpearDocument doc) {
		Map<EObject,Map<String,String>> map = new HashMap<>();

		//process the typedefs
		Map<String,TypeDef> newTypeDefs = new HashMap<>();
		BiConsumer<String,TypeDef> tdConsumer = (s,td) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(td, transform(td));
			newTypeDefs.put(newString, td);
		};
		doc.typedefs.forEach(tdConsumer);
		doc.typedefs = newTypeDefs;

		//process the constants
		Map<String,Constant> newConstants = new HashMap<>();
		BiConsumer<String,Constant> cConsumer = (s,c) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(c, transform(c));
			newConstants.put(newString, c);
		};
		doc.constants.forEach(cConsumer);
		doc.constants = newConstants;
				
		//process the patterns
		Map<String,Pattern> newPatterns = new HashMap<>();
		BiConsumer<String,Pattern> pConsumer = (s,p) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(p, transform(p));
			newPatterns.put(newString, p);
		};
		doc.patterns.forEach(pConsumer);
		doc.patterns = newPatterns;

		//process the patterns
		Map<String,Specification> newSpecs = new HashMap<>();
		BiConsumer<String,Specification> specConsumer = (s,spec) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(spec, transform(spec));
			newSpecs.put(newString, spec);
		};
		doc.specifications.forEach(specConsumer);
		doc.specifications = newSpecs;

		doc.mainName=CreateUserNamespace.makeUsername(doc.mainName);
		return map;
	}
	
	public static Map<EObject,Map<String,String>> transform(PatternDocument doc) {
		Map<EObject,Map<String,String>> map = new HashMap<>();
		//process the typedefs
		Map<String,TypeDef> newTypeDefs = new HashMap<>();
		BiConsumer<String,TypeDef> tdConsumer = (s,td) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(td, transform(td));
			newTypeDefs.put(newString, td);
		};
		doc.typedefs.forEach(tdConsumer);
		doc.typedefs = newTypeDefs;

		//process the constants
		Map<String,Constant> newConstants = new HashMap<>();
		BiConsumer<String,Constant> cConsumer = (s,c) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(c, transform(c));
			newConstants.put(newString, c);
		};
		doc.constants.forEach(cConsumer);
		doc.constants = newConstants;
				
		//process the patterns
		Map<String,Pattern> newPatterns = new HashMap<>();
		BiConsumer<String,Pattern> pConsumer = (s,p) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(p, transform(p));
			newPatterns.put(newString, p);
		};
		doc.patterns.forEach(pConsumer);
		doc.patterns = newPatterns;
		
		doc.mainName=CreateUserNamespace.makeUsername(doc.mainName);
		return map;		
	}
	
	public static Map<EObject,Map<String,String>> transform(TypeDocument doc) {
		Map<EObject,Map<String,String>> map = new HashMap<>();

		//process the typedefs
		Map<String,TypeDef> newTypeDefs = new HashMap<>();
		BiConsumer<String,TypeDef> tdConsumer = (s,td) -> {
			String newString = CreateUserNamespace.makeUsername(s);
			map.put(td, transform(td));
			newTypeDefs.put(newString, td);
		};
		doc.typedefs.forEach(tdConsumer);
		doc.typedefs = newTypeDefs;
		
		doc.mainName=CreateUserNamespace.makeUsername(doc.mainName);
		return map;		
	}
	
	public static Map<String,String> transform(EObject o) {
		CreateUserNamespace user = new CreateUserNamespace();
		return user.processNames(o);
	}

	private static String makeUsername(String original) {
		return "USER_" + original;
	}
	
	private Map<String,String> processNames(EObject root) {
		Map<String,String> renamed = new HashMap<>();
		SimpleAttributeResolver<EObject, String> resolver = SimpleAttributeResolver.newResolver(String.class,"name");
		rename(renamed, resolver, root);
		for (EObject e : EcoreUtil2.getAllContentsOfType(root, EObject.class)) {
			rename(renamed, resolver, e);
		}
		return renamed;
	}

	private void rename(Map<String, String> renamed, SimpleAttributeResolver<EObject, String> resolver, EObject e) {
		String name = resolver.apply(e);
		if (name != null) {
			String uniqueName = makeUsername(name);
			e.eSet(resolver.getAttribute(e), uniqueName);
			renamed.put(uniqueName,name);
		}
	}
}
