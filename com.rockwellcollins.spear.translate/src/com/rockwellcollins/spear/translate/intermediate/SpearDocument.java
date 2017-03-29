package com.rockwellcollins.spear.translate.intermediate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import com.rockwellcollins.spear.Constant;
import com.rockwellcollins.spear.File;
import com.rockwellcollins.spear.Pattern;
import com.rockwellcollins.spear.Specification;
import com.rockwellcollins.spear.TypeDef;
import com.rockwellcollins.spear.translate.transformations.PerformTransforms;

public class SpearDocument extends Document {

	public Map<String,Pattern> patterns = new HashMap<>();

	public Specification getMain() {
		return (Specification) files.get(this.mainName);
	}
	
	public SpearDocument(Specification main) {
		this.mainName = main.getName();
		List<EObject> objects = FindSpecificationDependencies.getDependencies(main);
		for(EObject o : objects) {
			if (o instanceof TypeDef) {
				TypeDef typedef = (TypeDef) o;
				typedefs.put(typedef.getName(),typedef);
			}
			
			if (o instanceof Constant) {
				Constant constant = (Constant) o;
				constants.put(constant.getName(),constant);
			}
			
			if (o instanceof Pattern) {
				Pattern pattern = (Pattern) o;
				patterns.put(pattern.getName(),pattern);
			}
			
			if (o instanceof File) {
				File f = (File) o;
				files.put(f.getName(),f);
			}
		}
	}
	
	public void transform() {
		//transform the document
		try {
			PerformTransforms.apply(this);
		} catch (Exception e) {
			System.err.println("Error performing transformations.");
			e.printStackTrace();
			System.exit(-1);
		}		
	}

	public Map<String, String> mergeMaps() {
		Map<String,String> merged = new HashMap<>();
		merged.putAll(renamed.get(this.getMain()));
		
		typedefs.forEach((s,td) -> merged.putAll(renamed.get(td)));
		constants.forEach((s,c) -> merged.putAll(renamed.get(c)));
		
//		for(String name : typedefs.keySet()) {
//			TypeDef td = typedefs.get(name);
//			Map<String,String> tdMap = renamed.get(td);	
//			merged.putAll(tdMap);
//		}
//		
//		for(String name : constants.keySet()) {
//			Constant c = constants.get(name);
//			Map<String,String> cMap = renamed.get(c);
//			merged.putAll(cMap);
//		}
		return merged;
	}
}
