import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class Snippets {
	public static void main(String[] args) {
		String name = JOptionPane.showInputDialog(null, "Please enter a bucket prefix:");
		while(bucketExists(name)) {
			name = JOptionPane.showInputDialog(null, "Bucket exists. Enter a new prefix: ");
		}
	}
	
	private static boolean bucketExists(String name) {
		AmazonS3 client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_2).build();
		
		for(Bucket b : client.listBuckets()) {
			if(b.getName().contains(name)) {
				System.out.println("exists");
				return true;
			}
		}
		
		return false;
	}
}
