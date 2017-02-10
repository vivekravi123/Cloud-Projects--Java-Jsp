package start;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Aras {
	public static PrintWriter writer;
	public static Scanner input;
	public static int supportThreshold;
	public static int confidenceThreshold;
	public static String dataFilePath;
	public static List<String> attributeNames = new ArrayList<String>();
	public static List<String> stableAttributes = new ArrayList<String>();
	public static List<String> flexibleAttributes = new ArrayList<String>();
	public static String decisionAttribute,decisionFrom,decisionTo;
	public static ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>(); 
	static Map<String, HashSet<String>> distinctAttributeValues = new HashMap<String, HashSet<String>>();
	static Map<HashSet<String>, HashSet<String>> attributeValues = new HashMap<HashSet<String>, HashSet<String>>();
	static Map<HashSet<String>, HashSet<String>> oneItemsets = new HashMap<HashSet<String>, HashSet<String>>();
	static Map<HashSet<String>, HashSet<String>> reducedAttributeValues = new HashMap<HashSet<String>, HashSet<String>>();
	static Map<String, HashSet<String>> decisionValues = new HashMap<String, HashSet<String>>();
	static Map<ArrayList<String>, HashSet<String>> markedValues = new HashMap<ArrayList<String>, HashSet<String>>();
	public static Map<ArrayList<String>,String> certainRules = new HashMap<ArrayList<String>,String>();
	public static Map<ArrayList<String>,String> certainRulesForAras = new HashMap<ArrayList<String>,String>();
	public static Map<ArrayList<String>,HashSet<String>> possibleRules = new HashMap<ArrayList<String>,HashSet<String>>();

	public static void SetSupportThreshold(int supportThreshold) {
	    Aras.supportThreshold = supportThreshold;
	}
	
	public static void SetConfidenceThreshold(int confidenceThreshold) {
	    Aras.confidenceThreshold = confidenceThreshold;
	}

	public static void SetDataFilePath(String dataFilePath) {
	    Aras.dataFilePath = dataFilePath;
		readData();
	}

	public static void SetAttributeNames(String[] attributeNames) {
	    Aras.attributeNames = new ArrayList<String>(Arrays.asList(attributeNames));
	    flexibleAttributes = new ArrayList<String>(Arrays.asList(attributeNames));
	}
	  
	public static void SetDecisionAttribute(String decisionAttribute) {
	    Aras.decisionAttribute = decisionAttribute;
	    flexibleAttributes.remove(decisionAttribute);
		HashSet<String> listOfDecisionValues = distinctAttributeValues.get(decisionAttribute);
		removeDecisionValueFromAttributes(listOfDecisionValues);
	
	}

	public static void SetStableAttribute(int[] stableAttributesIndex) {
	    for (int attributeIndex:stableAttributesIndex) {
	        stableAttributes.add(attributeNames.get(attributeIndex));
	    }

	    for (String attribute:stableAttributes) {
	        if(flexibleAttributes.contains(attribute)) {
	            flexibleAttributes.remove(attribute);
	        }
	    }
	}

	public static void SetDecisionFromValue(String inputDecisionFrom)
	{
		inputDecisionFrom = decisionAttribute + inputDecisionFrom;
		
		if (inputDecisionFrom != null && !inputDecisionFrom.isEmpty()){
			if(decisionValues.keySet().contains(inputDecisionFrom)){
				Aras.decisionFrom = inputDecisionFrom;
			}
		}
	    else {
	        printMessage("Invalid value.");
	    }
	}

	public static void SetDecisionToValue(String inputDecisionTo){
		
		inputDecisionTo = decisionAttribute + inputDecisionTo;
		
		if (inputDecisionTo != null && !inputDecisionTo.isEmpty()){
			if(decisionValues.keySet().contains(inputDecisionTo)){
				Aras.decisionTo = inputDecisionTo;
			}
		}
	    else {
	        printMessage("Invalid value.");
	    }
	}

	//Printing String, List and Map
	public static void printMessage(String content){
		System.out.println(content);
		writer.println(content);
	}
	
	public static void printList(List<String> list){
		Iterator<String> iterate = list.iterator();
		
		while(iterate.hasNext()){
			printMessage(iterate.next().toString());
		}
	}
	
	private static void printAttributeMap(Map<HashSet<String>, HashSet<String>> values) {
		for(Map.Entry<HashSet<String>, HashSet<String>> set : values.entrySet()){
			printMessage(set.getKey().toString() + " = " + set.getValue());
		}
	}
	
	private static void printCertainRulesMap(Map<ArrayList<String>, String> value) {
		printMessage("\nCertain Rules:");
		if (value == null || value.isEmpty()){
			printMessage("None");
		}
		for(Map.Entry<ArrayList<String>,String> set : value.entrySet()){
			if (set.getValue().equals(decisionTo)){
				System.out.println("set key: "+set.getKey().toString());
				System.out.println("decision to: "+decisionTo.toString());
				certainRulesForAras.put(set.getKey(), decisionTo.toString());
				printMessage(set.getKey().toString() + " -> "+decisionTo);
			}
		}
	}
	
	private static void printPossibleRulesMap(Map<ArrayList<String>, HashSet<String>> value) {
		if(!value.isEmpty()){
			printMessage("\nPossible Rules:");
			for(Map.Entry<ArrayList<String>,HashSet<String>> set : value.entrySet()){
				for(String possibleValue:set.getValue()){
					if (possibleValue.equals(decisionTo)){
						int support = calculateSupportLERS(set.getKey(),possibleValue);
						int confidence = calculateConfidenceLERS(set.getKey(),possibleValue);
						printMessage(set.getKey().toString() + " -> " + possibleValue + " [Support: " + support + ", Confidence: " + confidence +"%]");
					}
				}
			}
		}
	}
	
	private static void printMarkedValues() {
		printMessage("\nMarked Values:");
		for(Map.Entry<ArrayList<String>, HashSet<String>> markedSet : markedValues.entrySet()){
			attributeValues.remove(new HashSet<String>(markedSet.getKey()));
		
			printMessage(markedSet.toString());
		}
	}

	private static int findLERSSupport(ArrayList<String> tempList) {
		int count = 0;
		
		for(ArrayList<String> data : data){	
			if(data.containsAll(tempList))
				count++;
		}
		
		return count;
	}
	
	private static int calculateSupportLERS(ArrayList<String> key, String value) {
		ArrayList<String> tempList = new ArrayList<String>();
		
		for(String val : key){
			tempList.add(val);
		}
		
		if(!value.isEmpty())
			tempList.add(value);
	
		return findLERSSupport(tempList);
		
	}

	private static int calculateConfidenceLERS(ArrayList<String> key,
			String value) {
		int num = calculateSupportLERS(key, value);
		int den = calculateSupportLERS(key, "");
		int confidence = 0;
		if (den != 0){
			confidence = (num * 100)/den;
		} 
		return confidence;
	}
	
	private static void readData() {
		try {
			input = new Scanner(new File(dataFilePath));
			int lineNo = 0;
			
			while(input.hasNextLine()){
				String[] lineData = input.nextLine().split(",");
				String key;
				
				lineNo++;
				ArrayList<String> tempList = new ArrayList<String>();
				HashSet<String> set;
				
				for (int i=0;i<lineData.length;i++) {
					String currentAttributeValue = lineData[i];
					String attributeName = attributeNames.get(i);
					key = attributeName + currentAttributeValue;
					
					tempList.add(key);

					HashSet<String> mapKey = new HashSet<String>();
					mapKey.add(key);
					setMap(attributeValues,lineData[i],mapKey,lineNo);
					setMap(oneItemsets,lineData[i],mapKey,lineNo);
					
					if (distinctAttributeValues.containsKey(attributeName)) {
						set = distinctAttributeValues.get(attributeName);
						set.add(key);
						
					}else{
						set = new HashSet<String>();
					}
					
					set.add(key);
					distinctAttributeValues.put(attributeName, set);
				}
		
				data.add(tempList);
			}
		} catch (FileNotFoundException e) {
			printMessage("File Not Found");
			e.printStackTrace();
		}
	}

	private static void setMap(Map<HashSet<String>, HashSet<String>> values,
			String string, HashSet<String> key, int lineNo) {
		HashSet<String> tempSet;
		
		if (values.containsKey(key)) {
			tempSet = values.get(key);						
		}else{
			tempSet = new HashSet<String>();
		}
		
		tempSet.add("x"+lineNo);
		values.put(key, tempSet);
	}

	private static void removeDecisionValueFromAttributes(HashSet<String> listOfDecisionValues) {
		for(String value : listOfDecisionValues){
			HashSet<String> newHash = new HashSet<String>();
			newHash.add(value);
			Aras.decisionValues.put(value, attributeValues.get(newHash));
			attributeValues.remove(newHash);
		}
	}

	private static void findRules() {
		int loopCount = 0;
		printMessage("\n------------------------------------");
		printMessage("\nGenerating Certain Rules using LERS");
		printMessage("\n------------------------------------");
		
		while(!attributeValues.isEmpty()){
			printMessage("\nLoop " + (++loopCount) +":");
			printMessage("--------------------------");
			//printMessage("Itemsets with "+loopCount+" elements:");
			//printAttributeMap(attributeValues);
			
			for (Map.Entry<HashSet<String>, HashSet<String>> set : attributeValues.entrySet()) {
				ArrayList<String> setKey = new ArrayList<String>();
				setKey.addAll(set.getKey());
				
				if (set.getValue().isEmpty()) {
					continue;
				}else{
					if(!includesMarked(setKey)){
						for(Map.Entry<String, HashSet<String>> decisionSet : decisionValues.entrySet()){
							if(decisionSet.getValue().containsAll(set.getValue())){
								certainRules.put(setKey, decisionSet.getKey());
								markedValues.put(setKey, set.getValue());
								break;
							}
						}
					}
				}
				
				if(!includesMarked(setKey)){
					HashSet<String> possibleRulesSet = new HashSet<String>();
					for(Map.Entry<String, HashSet<String>> decisionSet : decisionValues.entrySet()){
						
						possibleRulesSet.add(decisionSet.getKey());
					}
					possibleRules.put(setKey, possibleRulesSet);
				}
				
			}
			printMarkedValues();
			removeMarkedValues();
			
			printCertainRulesMap(certainRules);
			printPossibleRulesMap(possibleRules);
			
			combinePossibleRules();
		}
	}

	private static boolean includesMarked(ArrayList<String> setKey) {
		for(Map.Entry<ArrayList<String>, HashSet<String>> markedSet : markedValues.entrySet()){
			if(setKey.containsAll(markedSet.getKey())){
				return true;
			}
		}
		return false;
	}	
	
	private static void removeMarkedValues() {
		for(Map.Entry<ArrayList<String>, HashSet<String>> markedSet : markedValues.entrySet()){
			attributeValues.remove(new HashSet<String>(markedSet.getKey()));
		}
	}
	
	private static void combinePossibleRules() {
		Set<ArrayList<String>> keySet = possibleRules.keySet();
		ArrayList<ArrayList<String>> keyList = new ArrayList<ArrayList<String>>();
		keyList.addAll(keySet);
		HashSet<String> possibleRule;
		for(int i = 0;i<possibleRules.size();i++){
			for(int j = (i+1);j<possibleRules.size();j++){
				possibleRule = new HashSet<String>(keyList.get(j));
				Iterator<String> iter = possibleRule.iterator();
				HashSet<String> combinedKeys = null;
				while (iter.hasNext()) {
				    combinedKeys = new HashSet<String>(keyList.get(i));
				    if (combinedKeys.add((String)iter.next())){
				    	if(!checkSameGroup(combinedKeys)){
				    		combineAttributeValues(combinedKeys);
				    	}
				    }
				}
			}
		}
		
		certainRules.clear();
		possibleRules.clear();
		
		removeRedundantValues();
		clearAttributeValues();
	}

	private static boolean checkSameGroup(HashSet<String> combinedKeys) {
		List<String> list = new ArrayList<String>(combinedKeys);
		HashSet<String> pair = new HashSet<String>();
		if (combinedKeys.size()==2){
			if (isPairSameGroup(combinedKeys))
				return true;
		} else {
			for(int i = 0;i<list.size()-1;i++){
				for(int j = i+1;j<list.size();j++){
					pair.add(list.get(i));
					pair.add(list.get(j));
					if (isPairSameGroup(pair))
						return true;
					pair.clear();
				}
			}
		}
		return false;
	}

	private static boolean isPairSameGroup(HashSet<String> pair) {
		for(Map.Entry<String, HashSet<String>> singleAttribute : distinctAttributeValues.entrySet()){
			if(singleAttribute.getValue().containsAll(pair)){
				return true;
			}
		}
		return false;
	}
	
    private static HashSet<String> findSupportActionSchema(ArrayList<String> key, String value) {
        // Find support of generalized action schema
        ArrayList<String> tempList = new ArrayList<String>();
        
        for(String val : key){
            if (stableAttributes.contains(Character.toString(val.charAt(0)))) {
                tempList.add(val);
            }
        }
        
        if(!value.isEmpty())
            tempList.add(value);
        
        HashSet<String> supportActionSchema = findARASSupport(tempList);
        
        // Find support of bad rule leading wrong decision
        tempList.clear();
        
        for(String val : key){
            tempList.add(val);
        }
        
        if(!value.isEmpty())
            tempList.add(value);
        
        HashSet<String> supportBadRule = findARASSupport(tempList);
        
        // Correcting support for generalized action schema
        supportActionSchema.removeAll(supportBadRule);
        
        return supportActionSchema;
    }
	
	private static void combineAttributeValues(HashSet<String> combinedKeys) {
		HashSet<String> combinedValues = new HashSet<String>();
			
		for(Map.Entry<HashSet<String>, HashSet<String>> attributeValue : attributeValues.entrySet()){
			if(combinedKeys.containsAll(attributeValue.getKey())){
				if(combinedValues.isEmpty()){
					combinedValues.addAll(attributeValue.getValue());
				}else{
					combinedValues.retainAll(attributeValue.getValue());
				}
			}
		}

		
		if (!checkSameGroup(combinedKeys))
			reducedAttributeValues.put(combinedKeys, combinedValues);
	}

	private static void removeRedundantValues() {
		HashSet<String> mark = new HashSet<String>();
		
		for(Map.Entry<HashSet<String>, HashSet<String>> reducedAttributeValue : reducedAttributeValues.entrySet()){
			for(Map.Entry<HashSet<String>, HashSet<String>> attributeValue : attributeValues.entrySet()){
				
				if(attributeValue.getValue().containsAll(reducedAttributeValue.getValue()) || reducedAttributeValue.getValue().isEmpty()){
					mark.addAll(reducedAttributeValue.getKey());
				}
			}
		}
		
		reducedAttributeValues.remove(mark);
	}
	
    private static HashSet<String> findARASSupport(ArrayList<String> tempList) {
        HashSet<String> tempSet = new HashSet<String>();
        
        int count = 1;
        for(ArrayList<String> data : data){
            if(data.containsAll(tempList))
                tempSet.add("x"+count);
                count++;
        }
        
        return tempSet;
    }
    
    private static HashSet<String> calculateSupportARAS(ArrayList<String> key, String value) {
        ArrayList<String> tempList = new ArrayList<String>();
        
        for(String val : key){
            tempList.add(val);
        }
        
        if(!value.isEmpty())
            tempList.add(value);
        
        return findARASSupport(tempList);
    }

	
	private static void clearAttributeValues() {
		 attributeValues.clear();
		 for(Map.Entry<HashSet<String>, HashSet<String>> reducedAttributeValue : reducedAttributeValues.entrySet()){
			 if (!checkSameGroup(reducedAttributeValue.getKey()))
				 attributeValues.put(reducedAttributeValue.getKey(), reducedAttributeValue.getValue());
		 }
		 reducedAttributeValues.clear();
	}

	
	private static void generateActionRules() {
        String actionRuleSchema;
        String actionRule;
        String attributeName = "";
        String attributeValueTo = "";
        String attributeValueFrom = "";
		
		printMessage("\n------------------------------------");
		printMessage("\nGenerating Action Rules using ARAS");
		printMessage("\n------------------------------------");
		
        // Run ARAS for every Certain Rule
		for(Map.Entry<ArrayList<String>, String> certainRule : certainRulesForAras.entrySet()){
            ArrayList<String> attrInRule = new ArrayList<String>();
            for (String attr : certainRule.getKey()) {
                    attrInRule.add(Character.toString(attr.charAt(0)));
            }

            // Find header of stable attributes that are in given certain rule
            ArrayList<String> header = new ArrayList<String>();
            for (String attr : certainRule.getKey()) {
                if (stableAttributes.contains(Character.toString(attr.charAt(0)))) {
                    header.add(attr);
                }
            }

            // Find Action Rule Schema
    		printMessage(" ");
    		printMessage(" ");
			printMessage("Action Rule Schema: ");
            actionRuleSchema = "";
            for (String attr: certainRule.getKey()) {
                attributeName = Character.toString(attr.charAt(0));
                attributeValueFrom = "";
                attributeValueTo = Character.toString(attr.charAt(1));
                
                if (stableAttributes.contains(attributeName))
                	actionRuleSchema = formRule(actionRuleSchema, attributeName, "", attributeValueTo, true);
                
                if (flexibleAttributes.contains(attributeName))
                	actionRuleSchema = formRule(actionRuleSchema, attributeName, "", attributeValueTo, false);
            }
            printMessage("[" + actionRuleSchema + "] -> (" + decisionAttribute + ", " + decisionFrom + "->" + decisionTo + ")");

            // Find support for Action Rule Schema
    		printMessage(" ");
            HashSet<String> supportActionSchema = findSupportActionSchema(certainRule.getKey(), decisionFrom);
            printMessage("Support for Action Schema: "+supportActionSchema.toString());
        
            // Find list of attribute values, which are not considered in Action Rule Schema
            ArrayList<String> remainingVals = new ArrayList<String>();
            for (String sAttr : stableAttributes) {
                if (!attrInRule.contains(sAttr)) {
                    remainingVals.addAll(new ArrayList<String>(distinctAttributeValues.get(sAttr)));
                }
            }
            for (String fAttr : flexibleAttributes) {
                if (attrInRule.contains(fAttr)) {
                    for (String fAttrVal: new ArrayList<String>(distinctAttributeValues.get(fAttr))) {
                        if (!certainRule.getKey().contains(fAttrVal)) {
                            remainingVals.add(fAttrVal);
                        }
                    }
                }
            }
        
            // Find Marked Sets
    		printMessage(" ");
            printMessage("Marked Sets: ");
            ArrayList<ArrayList<String>> markedRules = new ArrayList<ArrayList<String>>();
            for (String attrVal: remainingVals) {
                ArrayList<String> tempRule = new ArrayList<String>();
                tempRule.addAll(header);
                tempRule.add(attrVal);
                
                HashSet<String> tempSupport = findARASSupport(tempRule);
                
                if (!tempSupport.isEmpty()) {
	                if (supportActionSchema.containsAll(tempSupport)) {
	                    printMessage(tempRule.toString()+": "+tempSupport.toString());
	                    markedRules.add(tempRule);
	                }
                }
            }
            if (markedRules.isEmpty()) {
            	printMessage("None");
            }
        
            // Print Action Rules
    		printMessage(" ");
            printMessage("Action Rules: ");
            for (ArrayList<String> markedRule: markedRules) {
                actionRule = "";

                for (String attr: certainRule.getKey()) {
                    attributeName = Character.toString(attr.charAt(0));
                    attributeValueFrom = "";
                    attributeValueTo = Character.toString(attr.charAt(1));
                    
                    if (stableAttributes.contains(attributeName)) {
                        markedRule.remove(attributeName+attributeValueTo);
                        actionRule = formRule(actionRule, attributeName, "", attributeValueTo, true);
                    }
                    
                    if (flexibleAttributes.contains(attributeName)) {
                    	String removeAttr = "";
                        for (String markedAttr: markedRule) {
                            if (Character.toString(markedAttr.charAt(0)).equals(attributeName)) {
                                attributeValueFrom = Character.toString(markedAttr.charAt(1));
                                removeAttr = markedAttr;
                                break;
                            }
                        }
                        
                        if (!attributeValueFrom.isEmpty()) {
                            markedRule.remove(removeAttr);
                            actionRule = formRule(actionRule, attributeName, attributeValueFrom, attributeValueTo, false);
                        }
                        else {
                            actionRule = formRule(actionRule, attributeName, "", attributeValueTo, false);
                        }
                    }
                }
                
                printMessage("[" + actionRule + "] -> (" + decisionAttribute + ", " + decisionFrom + "->" + decisionTo + ")");
            }
            if (markedRules.isEmpty()) {
            	printMessage("None");
            }
        }
	}
	
	private static String checkAttribute(String value1) {
		for(Map.Entry<String, HashSet<String>> entryValue : distinctAttributeValues.entrySet()){
			if(entryValue.getValue().contains(value1)){
				return entryValue.getKey();
			}
		}
		return null;
	}

    private static String formRule(String rule, String attributeName, String attributeValueFrom, String attributeValueTo, boolean isStable) {
        if(!rule.isEmpty())
            rule += "^";

        if (isStable) {
            rule += attributeName + attributeValueTo;
        }
        else {
            rule += "(" + attributeName + ", ";
            if (!attributeValueFrom.isEmpty())
                rule += attributeName + attributeValueFrom;
            rule += "->" + attributeName + attributeValueTo + ")";
        }

        return rule;
    }

	public static void aras() throws IOException{
		try {
			//Find Certain and Possible rules
			writer = new PrintWriter("aras_output.txt", "UTF-8");
			findRules();
			generateActionRules();
			writer.close();
			
		} catch (Throwable t){
			System.out.println("Fatal error: "+ t.toString());
			t.printStackTrace();
		}
		
		ProcessBuilder pb = new ProcessBuilder("Notepad.exe", "aras_output.txt");
		pb.start();
		
	}
}