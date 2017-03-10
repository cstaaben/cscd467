import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 6
 * Word count example using MapReduce in the school's HDFS setup
 */
public class WordCount {
	public static class WCMapper extends Mapper<Object, Text, Text, Text> {
		private Text match = new Text();
		private Text out = new Text();
		
		@Override
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer st = new StringTokenizer(value.toString());
			int lineNum = Integer.parseInt(st.nextToken());
			String curFile = ((FileSplit)context.getInputSplit()).getPath().toString();
			String pattern = context.getConfiguration().get("pattern");
			
			while(st.hasMoreTokens()) {
				if(st.nextToken().equalsIgnoreCase(pattern)) {
					match.set(pattern + " " + curFile);
					out.set(lineNum+"");
					context.write(match, out);
				}
			}
			
			/*while(st.hasMoreTokens()) {
				match.set(st.nextToken());
				context.write(match, curFile);
			}// end while*/
		}
		
	} // end WCMapper
	
	public static class WCReducer extends Reducer<Text, Text, Text, Text> {
		
		@Override
		protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			for(Text value : values) {
				context.write(key, value);
			}
		}
		
	} // end WCReducer
	
	public static void main(String[] args) throws Exception {
		Configuration c = new Configuration();
		String[] sara = new GenericOptionsParser(c, args).getRemainingArgs();
		if(sara.length < 3) {
			System.err.println("Usage: wordcount <pattern> <in> [<in>...] <out>");
			System.exit(2);
		}
		
		c.set("pattern", args[0]);
		
		Job job = new Job(c, "match count");
		job.setJarByClass(WordCount.class);
		job.setMapperClass(WCMapper.class);
		job.setCombinerClass(WCReducer.class);
		job.setReducerClass(WCReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		for(int i = 1; i < sara.length - 1; ++i) {
			FileInputFormat.addInputPath(job, new Path(sara[i]));
		}
		
		FileOutputFormat.setOutputPath(job, new Path(sara[sara.length-1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
