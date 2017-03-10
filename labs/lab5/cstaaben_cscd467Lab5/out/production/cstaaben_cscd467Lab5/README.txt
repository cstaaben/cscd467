Author: Corbin Staaben
CSCD 467 Lab 5
Description: Unzip the submitted cstaabenCSCD467Lab5.zip file to get the cstaaben_cscd467Lab5 folder.
    To compile: execute the following commands
        cd cstaaben_cscd467Lab5/src
        javac *java
    To run: execute the following commands
        java RandomCharacters

Answers:
2.) Each thread sleeps for a random number of milliseconds before setting a JLabel with a random character from the standard
    alphabet continually while the thread exists; it waits if the thread gets suspended by the user clicking a checkbox
    that is the same index in an array as the index of the current thread in its containing array.

3.) The thread waits when it still exists and the boolean determining whether it's suspended, which is when the checkbox
    corresponding to the current thread is checked. It wakes up when the checkbox is clicked a second time and the
    boolean which determines whether the thread is suspended.

4.) One defect is that when all threads are suspended and then a thread that is not the first thread is resumed, it does
    not resume displaying its random characters (fixed by using notifyAll() instead of notify()).