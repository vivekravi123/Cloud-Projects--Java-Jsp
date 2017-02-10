package start;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class Input {
  private int support;
  private int confidence;
  private String dataFilePath;
  private String decisionAttribute;
  private String decisionFrom;
  private String decisionTo;
  private List<String> attributeNames = new ArrayList<String>();
  private List<String> stableAttributes = new ArrayList<String>();
  private List<String> flexibleAttributes = new ArrayList<String>();
  private ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

  public void SetDataFilePath(String dataFilePath) {
      this.dataFilePath = dataFilePath;
      ReadData();
  }

  public void ReadData() {
      String[] row;
//      System.out.println(attributeNames.size());
      for (String attribute:attributeNames) {
          data.add(new ArrayList<String>());
      }
      
      try {
          Scanner input = new Scanner(new File(dataFilePath));
          
          while (input.hasNextLine()) {
              row = input.nextLine().split(",");
//              System.out.println(row.length);
 //             System.out.println(row[6]);
              for (int i=0; i<row.length; i++) {
   //               System.out.println(row[i]);
            	  data.get(i).add(row[i].trim());

              }
          }
      } catch (FileNotFoundException e) {
          printMessage("File Not Found");
          e.printStackTrace();
      }
  }

  public void SetSupport(int support) {
      this.support = support;
  }

  public void SetConfidence(int confidence) {
      this.confidence = confidence;
  }

  public void SetAttributeNames(String[] attributeNames) {
      this.attributeNames = new ArrayList<String>(Arrays.asList(attributeNames));
      this.flexibleAttributes = new ArrayList<String>(Arrays.asList(attributeNames));
  }
  
  public void SetDecisionAttribute(String decisionAttribute) {
      this.decisionAttribute = decisionAttribute;
      this.flexibleAttributes.remove(decisionAttribute);
  }

  public void SetStableAttribute(int[] stableAttributesIndex) {
      for (int attributeIndex:stableAttributesIndex) {
          this.stableAttributes.add(this.attributeNames.get(attributeIndex));
      }

      for (String attribute:this.stableAttributes) {
          if(this.flexibleAttributes.contains(attribute)) {
              this.flexibleAttributes.remove(attribute);
          }
      }
  }

	public void SetDecisionFromValue(String decisionFrom)
  {
      int decisionAttributeIndex = attributeNames.indexOf(decisionAttribute);
      
      //System.out.println(decisionAttributeIndex);
      //System.out.println(decisionAttribute);
      if (data.get(decisionAttributeIndex).contains(decisionFrom)) {
          this.decisionFrom = decisionFrom;
      }
		else {
			printMessage("Invalid value.");
		}
	}

  public void SetDecisionToValue(String decisionTo)
  {
      int decisionAttributeIndex = attributeNames.indexOf(decisionAttribute);
              
      if (data.get(decisionAttributeIndex).contains(decisionTo)) {
          this.decisionTo = decisionTo;
      }
      else {
          printMessage("Invalid value.");
      }
  }

  public ArrayList<ArrayList<String>> GetData() { return data; }
  public int GetSupport() { return support; }
  public int GetConfidence() { return confidence; }
  public List<String> GetAttributeNames() { return attributeNames; }
  public String GetDecisionAttribute() { return decisionAttribute; }
  public List<String> GetStableAttributes() { return stableAttributes; }
  public List<String> GetFlexibleAttributes() { return flexibleAttributes; }
  public String GetDecisionFromValue() { return decisionFrom; }
  public String GetDecisionToValue() { return decisionTo; }

  public void printMessage(String content) {
      System.out.println(content);
  }
             
  public void printList(List<String> list) {
      Iterator iterate = list.iterator();
              
      while(iterate.hasNext()) {
          printMessage(iterate.next().toString());
      }
  }
}

/*
public class Test {
public static void main(String[] args) {
  Input inp = new Input();
  
  inp.SetAttributeNames(new String[] {"A", "B", "C", "D"});
  inp.SetDataFilePath("/My Computer/Academic/MS/Spring 2016/KDD/Assignments/LERS/data.txt");
  inp.SetSupport(3);
  inp.SetConfidence(75);
  inp.SetDecisionAttribute("D");
  inp.SetDecisionFromValue("2");
  inp.SetDecisionToValue("1");
  inp.SetStableAttribute(new int[] {1});

  System.out.println("Decision Attribute: ");
  System.out.println(inp.GetDecisionAttribute());
  
  System.out.println("Stable Attributes: ");
  for (String attribute:inp.GetStableAttributes()) {
      System.out.println(attribute);
  }

  System.out.println("Flexible Attributes: ");
  for (String attribute:inp.GetFlexibleAttributes()) {
      System.out.println(attribute);
  }

  System.out.println("Data: ");
  for (ArrayList<String> column:inp.GetData()) {
      for (String rowValue:column) {
          System.out.print(rowValue);
      }
      System.out.println("");
  }
}
}*/