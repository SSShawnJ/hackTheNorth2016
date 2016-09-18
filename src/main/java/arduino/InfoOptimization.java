package arduino;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class InfoOptimization {

	private  Map<String,String> map=new HashMap<>();
	public InfoOptimization(){
		List<String> keys=Arrays.asList("professional","room","finger","thumb","arm","hand","screenshot","organ","driving","energy");
		List<String> values=new ArrayList<>(Arrays.asList("person looks professional","wall","person's finger",
				                                 "person's thumb","person's arm","person's hand","something","something","something","something"));
		
		for(int i=0;i<keys.size();i++){
			map.put(keys.get(i), values.get(i));
		}
		
	}
	
	public String optimize(String input){
		String output=map.get(input);
		return output==null?input:output;
	}
	
	
	
	
}
