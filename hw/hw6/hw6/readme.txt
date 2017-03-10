Author: Corbin Staaben
Description: First, copy the WordCount.java, file1, file2 files to the Hadoop VM. Then execute the following commands:
	hadoop com.sun.tools.javac.Main WordCount.java
	jar cf wc.jar WordCount*.java
	hadoop jar wc.jar WordCount <pattern> <input> [<input>...] <output>
Replace anything in brackets with your desired input. To view output, run the following command:
	hadoop fs -cat <output>/part-r-00000
where <output> is the same path you specified previously.