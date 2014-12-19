MABED
=====

Mention-anomaly-based Event Detection and Tracking in Twitter

Author: Adrien GUILLE

Details of this program are described in the following paper:

	Adrien Guille and Cécile Favre (2014) 
	Mention-Anomaly-Based Event Detection and Tracking in Twitter.
	In Proceedings of the 2014 IEEE/ACM International Conference on
	Advances in Social Network Mining and Analysis (ASONAM 2014),
	pp. 375-382, DOI: 10.1109/ASONAM.2014.6921613

Please cite this paper when using the program.

----------------------
Files in the Directory
----------------------

- input/: input files that describe the corpus in which we want to detect events
- MABED.jar: Java program that does the event detection
- README.txt: this file
- parameters.txt: Java properties file in which parameters are set
- stopwords.txt: a list of common stopwords to remove when generating the vocabulary
- lib/: program dependencies

------------
Input Format
------------

The program expects two sets of files in the "input/" directory:

1. <time_slice>.text: content of the messages, one line per message;
2. <time_slice>.time: timestamp of the messages, each line maps to the message that has the same line number in <time_slice>.text. Timestamps should be formatted according to this format: YYYY-MM-DD HH:mm:ss.S (e.g. 2009-11-01 00:01:24.0)

Time-slices are expected to be numbered starting from 0 and files are expected to be named with 8 digits (e.g. 00000000.text, 00000000.time, 00000001.text, 00000001.time) 

-----------------
Parameter Setting
-----------------

All the parameters are set in the parameters.txt file:

1. prepareCorpus (boolean): if you are running MABED for the first time, or if the content of the input directory has been modified, this parameter should be set to 'true', otherwise 'false'.
2. timeSliceLength (int): length of each time-slice, expressed in minutes (e.g. 30);
3. numberOfThreads (int): the number of threads used by MABED (if > 1, then the parallelized implementation of MABED is executed)
4. k (int): desired number of events (e.g. 40);
5. p (int): maximum number of related words describing each event (e.g. 10);
6. theta (double): minimum weight of each related word (e.g. 0.7);
7. sigma (double): merging threshold (e.g. 0.5);
8. stopwords (String): name of the file that lists the stopwords, one word per line (e.g. stopwords.txt);
9. minSupport (double): minimum support of words in the vocabulary (e.g. 0)
10. maxSupport (double): maximum support of words in the vocabulary (e.g. 1)

-------------------
Running the program
-------------------

- Requirements: JAVA (7+)

- Execute the program MABED.jar with the following command: "java -jar MABED.jar -run". It should process the input and save the output in the "ouput/" directory.
