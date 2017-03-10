import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class Snippets {
	public static void main(String[] args) {
		AmazonS3 client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
		ArrayList<Bucket> buckets = new ArrayList<>(client.listBuckets());
		
		Random r = new Random();
		for(int i = 0; i < 7; i++) {
			File f = new File("C:\\Users\\cstaa\\Desktop\\hw7Tester" + (i+1) + ".txt");
			int index = r.nextInt(buckets.size());
			
			client.putObject(buckets.get(index).getName(), f.getName(), f);
		}
		
		for(Bucket b : client.listBuckets()) {
        	System.out.println("Bucket: " + b.getName());
        	
        	ObjectListing list = client.listObjects(b.getName());
        	do {
        		for(S3ObjectSummary summary : list.getObjectSummaries()) {
        			System.out.println("\t\t-" + summary.getKey());
        		}
        		
        		ObjectListing newList = client.listNextBatchOfObjects(list);
        		list = newList;
        	} while(list.isTruncated() && list != null);
        }
	}
}
