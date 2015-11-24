#MABED
=====

Mention-anomaly-based Event Detection and Tracking in Twitter

Author: Adrien GUILLE
Continuous Version with HTML Report : Nicolas Dugué

Details of this program are described in the following papers:

	Adrien Guille and Cécile Favre (2015) 
	Event detection, tracking, and visualization in Twitter: a mention-anomaly-based approach.
	Springer Social Network Analysis and Mining,
	vol. 5, iss. 1, art. 18, DOI: 10.1007/s13278-015-0258-0

	Adrien Guille and Cécile Favre (2014) 
	Mention-Anomaly-Based Event Detection and Tracking in Twitter.
	In Proceedings of the 2014 IEEE/ACM International Conference on
	Advances in Social Network Mining and Analysis (ASONAM 2014),
	pp. 375-382, DOI: 10.1109/ASONAM.2014.6921613

Please cite one of these papers when using the program.

###Getting the exec files
----------------------

With Maven, please run 
	mvn install
at the root of the project.
It will produce four jars.

The *getTweet* jar is used to consume the Twitter Streaming API and thus to collect tweets.

The *prepareCorpus* jar can be used to prepare a corpus which was collected in one file. It cuts files in slices corresponding to time interval. It is necessary to partition the corpus in slices to run MABED. However, *getTweet* jar automatically partitions the corpus.

The *eventDetection* jar runs the MABED method on a corpus of tweets and produces the html report to visualize the events detected.

Finally, the *orchestrator* jar can be used to handle all the previous jars efficiently by running one command. It allows to run mabed continuously while collecting tweets and to keep html results.

*ALL JARS have to be in the same directory.*

###Obtaining Twitter Tokens
----------------------

Twitter tokens are mandatory to use MABED and C-MABED. Thay allow the application to get data from Twitter.
To get usable tokens, please follow this link : [https://dev.twitter.com/oauth/overview/application-owner-access-tokens] and especially the section introduced by "At the bottom of the next page, you will see a section labeled “your access token”:".

###Running the program
----------------------

	java -jar mabed-0.1-orchestrator.jar 
	usage: Streaming API
	 -c,--consumer <arg>        Consumer key
	 -cs,--consumerkey <arg>    Secret Consumer key
	 -e,--exp <arg>             Experiment Name : ONE WORD ONLY
	 -h,--help                  print this message
	 -k,--events <arg>          Number of events to detect. Default to 20.
	 -keyword,--keyword <arg>   Keywords to use to filter the tweet stream
	 -m,--minutes <arg>         Time interval in minutes. Default : 30.
	 -ms,--minsupport <arg>     Parameter for keyword selection between 0 and
		                    1. Default to 0.01
	 -Ms,--maxsupport <arg>     Parameter for keyword selection between 0 and
		                    1. Default to 0.1
	 -nt,--thread <arg>         Number of Threads
	 -p,--keywords <arg>        Number of keywords per event. Default to 10.
	 -period,--period <arg>     How many time intervals make a period.
	 -sigma,--sigma <arg>       Parameter to control event redundancy between
		                    0 and 1. Default to 0.5
	 -t,--token <arg>           Twitter token
	 -theta,--theta <arg>       Parameter for keyword selection between 0 and
		                    1. Default to 0.7
	 -ts,--secrettoken <arg>    Secret Twitter token


###Files in the Directory
----------------------

- input/: input files that describe the corpus in which we want to detect events
- MABED.jar: Java program that does the event detection
- README.txt: this file
- parameters.txt: Java properties file in which parameters are set
- stopwords.txt: a list of common stopwords to remove when generating the vocabulary
- lib/: program dependencies
