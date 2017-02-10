package rfalgorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class RandomForest {

	public static ArrayList<String> attributeList = new ArrayList<String>();
	public static ArrayList<String> dataFileList = new ArrayList<String>();
	public static ArrayList<String> originalAttributeList = new ArrayList<String>();
	public static ArrayList<String> stableAttributeList = new ArrayList<String>();
	public static ArrayList<String> decisionAttributeList = new ArrayList<String>();
	public static ArrayList<String> decisionFromToList = new ArrayList<String>();
	public static String decisionFrom = new String();
	public static String decisionTo = new String();
	public static int support = 0;
	public static int confidence = 0;
	public static Scanner scanner = new Scanner(System.in);
	public static int index = 0;

	public static void setStableAttributes() {
		boolean flag = false;
		String[] stable = null;
		System.out.println("==========Random Forest Algorithm==========");
		System.out.println("Available Attributes: " + attributeList.toString());
		System.out.println("1. Enter the Stable Attribute(s):");
		String s = scanner.next();
		if (s.split(",").length > 1) {
			stable = s.split(",");
			for (int j = 0; j < stable.length; j++) {
				if (!(attributeList.contains(stable[j]))) {
					System.out.println("Invalid Stable attribute(s)");
					flag = true;
					break;
				}
			}
			if (flag == false) {
				stableAttributeList.addAll(Arrays.asList(stable));
				attributeList.removeAll(stableAttributeList);
			}
		} else {
			if (!(attributeList.contains(s))) {
				System.out.println("Invalid Stable attribute(s)");
			} else {
				stableAttributeList.add(s);
				attributeList.removeAll(stableAttributeList);
			}
		}
		System.out.println("Stable Attribute(s): "
				+ stableAttributeList.toString());
		System.out.println("Available Attribute(s): "
				+ attributeList.toString());
	}

	public static void setDecisionAttribute() {
		System.out.println("1. Enter the Decision Attribute:");
		String s = scanner.next();
		if (!(attributeList.contains(s))) {
			System.out.println("Invalid Decision attribute(s)");
		} else {
			decisionAttributeList.add(s);
			index = originalAttributeList.indexOf(s);
			attributeList.removeAll(decisionAttributeList);
		}
	}

	public static void setDecisionFromTo(String args) {
		HashSet<String> set = new HashSet<String>();
		File data = new File(args);
		FileReader data_reader;
		BufferedReader data_buffer;
		try {
			data_reader = new FileReader(data);
			data_buffer = new BufferedReader(data_reader);
			String d = new String();
			while ((d = data_buffer.readLine()) != null)
				set.add(d.split(",")[index]);
			data_reader.close();
			data_buffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext()) {
			decisionFromToList.add(originalAttributeList.get(index)
					+ iterator.next());
		}
		System.out.println("Available Decision Attributes: "
				+ decisionFromToList.toString());
		System.out.println("Enter the Decision From Attribute: ");
		decisionFrom = scanner.next();
		System.out.println("Enter the Decision To Attribute: ");
		decisionTo = scanner.next();
		System.out.println("Stable Attribute(s): "
				+ stableAttributeList.toString());
		System.out.println("Decision Attribute: "
				+ decisionAttributeList.toString());
		System.out.println("Decision From Attribute: " + decisionFrom);
		System.out.println("Decision To Attribute: " + decisionTo);
		System.out
				.println("Flexible Attribute(s): " + attributeList.toString());
	}

	public static void main(final String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {

		File attribute = new File(args[0]);
		FileReader attribute_reader = new FileReader(attribute);
		BufferedReader attribute_buffer = new BufferedReader(attribute_reader);
		String att = new String();
		while ((att = attribute_buffer.readLine()) != null) {
			attributeList.addAll(Arrays.asList(att.split("\\s+")));
			originalAttributeList.addAll(Arrays.asList(att.split("\\s+")));
		}
		int count = 0;
		attribute_reader.close();
		attribute_buffer.close();
		File data = new File(args[1]);
		FileReader data_reader = new FileReader(data);
		BufferedReader data_buffer = new BufferedReader(data_reader);
		String d = new String();
		while ((d = data_buffer.readLine()) != null) {
			count++;
		}
		data_reader.close();
		data_buffer.close();
		setStableAttributes();
		setDecisionAttribute();
		setDecisionFromTo(args[1]);
		System.out.println("Enter the minimum Support: ");
		support = scanner.nextInt();
		System.out.println("Enter the minimum Confidence %: ");
		confidence = scanner.nextInt();
		scanner.close();

		Configuration conf = new Configuration();

		conf.set("mapred.max.split.size", data.length() / 5 + "");
		conf.set("mapred.min.split.size", "0");

		conf.setInt("count", count);
		conf.setStrings("attributes", Arrays.copyOf(
				originalAttributeList.toArray(),
				originalAttributeList.toArray().length, String[].class));
		conf.setStrings("stable", Arrays.copyOf(stableAttributeList.toArray(),
				stableAttributeList.toArray().length, String[].class));
		conf.setStrings("decision", Arrays.copyOf(
				decisionAttributeList.toArray(),
				decisionAttributeList.toArray().length, String[].class));
		conf.setStrings("decisionFrom", decisionFrom);
		conf.setStrings("decisionTo", decisionTo);
		conf.setStrings("support", support + "");
		conf.setStrings("confidence", confidence + "");

		Job actionJob;
		actionJob = Job.getInstance(conf);

		actionJob.setJarByClass(ActionRules.class);

		actionJob.setMapperClass(ActionRules.JobMapper.class);
		actionJob.setReducerClass(ActionRules.JobReducer.class);

		actionJob.setNumReduceTasks(1);

		actionJob.setOutputKeyClass(Text.class);
		actionJob.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(actionJob, new Path(args[1]));

		FileOutputFormat.setOutputPath(actionJob, new Path(args[2]));

		actionJob.waitForCompletion(true);

		Job associationJob;
		associationJob = Job.getInstance(conf);

		associationJob.setJarByClass(AssociationActionRules.class);

		associationJob.setMapperClass(AssociationActionRules.JobMapper.class);
		associationJob.setReducerClass(AssociationActionRules.JobReducer.class);

		associationJob.setNumReduceTasks(1);

		associationJob.setOutputKeyClass(Text.class);
		associationJob.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(associationJob, new Path(args[1]));

		FileOutputFormat.setOutputPath(associationJob, new Path(args[3]));

		associationJob.waitForCompletion(true);

		/*ArrayList<String> actionRuleList = new ArrayList<String>();
		ArrayList<String> associationRuleList = new ArrayList<String>();

		File action = new File(args[2] + "/part-r-00000");
		BufferedReader br = new BufferedReader(new FileReader(action));
		String b = new String();
		while ((b = br.readLine()) != null)
			actionRuleList.add(b.split(" ==> ")[0].replaceAll("[^_A-Za-z0-9]+",
					""));
		br.close();

		File association = new File(args[3] + "/part-r-00000");
		BufferedReader br1 = new BufferedReader(new FileReader(association));
		String b1 = new String();
		while ((b1 = br1.readLine()) != null)
			associationRuleList.add(b1.split(" ==> ")[0].replaceAll(
					"[^_A-Za-z0-9]+", ""));
		br1.close();

		System.out.println("------Common rules------");
		associationRuleList.retainAll(actionRuleList);
		System.out.println(associationRuleList.toString());
*/	}
}