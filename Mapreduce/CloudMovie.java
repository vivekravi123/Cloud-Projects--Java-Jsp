import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;


public class CloudMovie extends Configured implements Tool {

	
   public static void main( String[] args) throws  Exception {
      int res  = ToolRunner .run( new CloudMovie(), args);
      System .exit(res);
   }

   public int run( String[] args) throws  Exception {
	  String temp_out = args[1]+"/temp/";
	  
	  Path outputTempDir = new Path(temp_out);
      Job jobmov  = Job .getInstance(getConf(), "MovieData ");
      Job jobmov2 =null;
      jobmov.setJarByClass( this .getClass()); 
      
      FileInputFormat.addInputPaths(jobmov,  args[0]);
      FileOutputFormat.setOutputPath(jobmov,  outputTempDir);
      jobmov.setMapperClass( Map .class);
      jobmov.setReducerClass( Reduce .class);
      jobmov.setMapOutputKeyClass(Text.class);
      jobmov.setMapOutputValueClass(Text.class);
      jobmov.setOutputKeyClass( Text .class);
      jobmov.setOutputValueClass( Text .class);
      if(jobmov.waitForCompletion(true)){
    	 jobmov2 = Job .getInstance(getConf(), " MovieData ");
    	 jobmov2.setJarByClass(this .getClass());
    	
    	 jobmov2.setMapperClass( Map2 .class);
    	 jobmov2.setReducerClass( Reduce2 .class);
    	 jobmov2.setOutputKeyClass( Text .class);
    	 jobmov2.setOutputValueClass( Text .class);
    	 FileInputFormat.addInputPaths(jobmov2,  temp_out);
          FileOutputFormat.setOutputPath(jobmov2,  new Path(args[1]+"/out/"));
      }
      return jobmov2.waitForCompletion( true)  ? 0 : 1;
   }
   
   public static class Map extends Mapper<LongWritable ,  Text ,  Text ,  Text > {
	  
	  public void map( LongWritable offset,  Text lineText,  Context context)
        throws  IOException,  InterruptedException {
         String wordline  = lineText.toString();
         Text currentWord  = new Text();
         String[] inputSequence = wordline.split("::");
            currentWord  = new Text(inputSequence[1]+"#####"+inputSequence[2]);
            context.write(new Text(inputSequence[0]),currentWord);
      }
   }

   public static class Reduce extends Reducer<Text ,  Text ,  Text ,  Text > {
      @Override 
      public void reduce( Text word,  Iterable<Text > counts,  Context context)
         throws IOException,  InterruptedException {
    	 
    	 int countval=0;
    	 int sumval=0;
    	 String[] str =null;
         StringBuffer sb  = new StringBuffer();
         for ( Text rating  : counts) {
        	 str= rating.toString().split("#####");
        	countval=countval+1;
        	sumval=sumval+ Integer.parseInt(str[1]);
             sb.append(str[0].toString().trim()).append(",").append(str[1].toString().trim()).append(",");
         }
   	 System.out.println("Inside Reduce1 ---->>>"+sb.toString());
         
         context.write(word,  new Text(sb.deleteCharAt(sb.toString().length()-1).toString().trim()));
         
      }
   }
   
   
   public static class Map2 extends Mapper<LongWritable ,  Text ,  Text ,  Text > {
	     

	     public void map( LongWritable offset,  Text lineText,  Context context)
	       throws  IOException,  InterruptedException {
	      	 
	   	 System.out.println(lineText);
	   	 HashMap<String,String> movieMap = new  HashMap<String,String>();
	   	 String[] lineFull = lineText.toString().split("\\s+");
	   	 String[] line  = lineFull[1].toString().split(",");
	   	 int len_arr = 0;
		   	 for(int i=0;i<line.length;i=i+2){
		   		 movieMap.put(line[i], line[i+1]);
		   		 
		   	 }
		         ArrayList<String> array_movie = new ArrayList<String>( movieMap.keySet()); 
		         len_arr = array_movie.size();
		   	 for(int j=0;j<len_arr;j++){
		   		 for(int i=j+1;i<len_arr;i++){
		   			 
		   			 context.write(new Text(array_movie.get(j)+","+array_movie.get(i)),
		   					 new Text(movieMap.get(array_movie.get(j))+","+movieMap.get(array_movie.get(i))));
		   			 
		   		 }
		   	 }
	   	 
	   	 
	             
	     //}
	     }
   }

	  public static class Reduce2 extends Reducer<Text ,  Text ,  Text ,  Text> {
		  @Override 
	     public void reduce( Text word,  Iterable<Text > counts,  Context context)
	        throws IOException,  InterruptedException {
		       // Configuration conf = context.getConfiguration();
			  try{
				  	 
	   	 DecimalFormat deciFormat = new DecimalFormat("#.0");
	   	 int xy=0;
	   	 int corr_xy=0;
	   	 int x2= 0;
	   	 int y2= 0;
	   	 int x2_corr= 0;
	   	 int y2_corr= 0;
	   	 int mean_x = 0;
	   	 int mean_y=0;
	   	 double corr = 0;
	   	 double cosine = 0;
	   	 double jaccard_coeff = 0;
	   	 int num_jac = 0;
	   	 int deno_jac =0;
	   	 String[] movieId = word.toString().split(",");
	   	 String[] ratings=null;
	   	 ArrayList<Integer> al = new ArrayList<Integer>();
	   	 for ( Text count  : counts) {
	   		 ratings= count.toString().split(",");
	   		 al.add(Integer.parseInt(ratings[0]));
	   		 al.add(Integer.parseInt(ratings[1]));
	   		 
	          }
	   	 int x =0;
	   	 int y =0;
	   	 int mean_count = al.size()/2;
	   	 for (int i=0;i<al.size();i=i+2) {
	   		 x=al.get(i);
	   		 y=al.get(i+1);
	   		 if(x>y){
	   			 num_jac = num_jac+y;
	   			 deno_jac = deno_jac+x;
	   		 }
	   		 else{
	   			 num_jac = num_jac+x;
	   			 deno_jac = deno_jac+y;
	   			 
	   		 }
	   		 mean_x = mean_x+x;
	   		 mean_y = mean_y+y;
	   		 
	   		 xy=xy+(x*y);
	   		 x2=x2+(x* x);
	   		 y2=y2+(y* y);
	   		  
	   	 }
	   	
	   	 mean_x = mean_x/mean_count;
	   	 mean_y = mean_y/mean_count;
	   	 	 cosine=(xy)/(Math.sqrt(x2) * Math.sqrt(y2));
	   	 for (int i=0;i<al.size();i=i+2) {
	   		 corr_xy=corr_xy+((al.get(i)-mean_x)*(al.get(i+1)-mean_y));
	   		 x2_corr=x2_corr+((al.get(i)-mean_x)* (al.get(i)-mean_x));
	   		 y2_corr=y2_corr+((al.get(i+1)-mean_y)* (al.get(i+1)-mean_y));
	   		  
	   	 }
	   	 System.out.println("corr_xy--> "+ corr_xy);
	   	 System.out.println("Math.sqrt((x2_corr*y2_corr))--> "+ Math.sqrt((x2_corr*y2_corr)));
	   	  corr = corr_xy/(Math.sqrt((x2_corr*y2_corr)));
	   	  if(deno_jac!=0){
	   		  jaccard_coeff = (num_jac/deno_jac) ;
	   	  }
	   		  
			  	 System.out.println("Inside Reduce2 ---->>>jaccard_coeff, corr, cos"+jaccard_coeff+" "+corr+" "+cosine);
	   		 context.write(new Text(movieId[0]+","+movieId[1]), new Text(deciFormat.format(corr)+","+deciFormat.format(cosine)+","+deciFormat.format(jaccard_coeff)));
	   		 context.write(new Text(movieId[1]+","+movieId[0]), new Text(deciFormat.format(corr)+","+deciFormat.format(cosine)+","+deciFormat.format(jaccard_coeff)));
			  }
			  catch(InterruptedException Ie){
				  Ie.printStackTrace();
			  }
			  
	        
	     }
	  }
}