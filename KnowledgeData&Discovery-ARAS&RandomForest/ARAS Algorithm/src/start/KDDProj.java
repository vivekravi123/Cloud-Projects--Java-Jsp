package start;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.*;
public class KDDProj {



  public static void main(String[] args) throws FileNotFoundException, IOException {

	  
	  
      
	  KDDUI myFrame=new KDDUI();
      
      JFrame f = new JFrame();
      f.setSize(1000, 700);
      f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      f.setVisible(true);         
      f.setTitle("Action Rules Extraction");
      f.getContentPane().add(myFrame);
      

  }
  
}
