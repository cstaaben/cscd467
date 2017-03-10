import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

/**
 * @author Corbin Staaben
 * CSCD 467 HW 6
 * Word count example using MapReduce in the school's HDFS setup
 */
public class WordCount {
	public static class WCMapper extends Mapper<Object, Text, Text, IntWritable> {
		
	}
	
	public static class WCReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		
	}
}
