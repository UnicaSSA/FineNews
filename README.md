# FineNews

### Fine-Grained Semantic Sentiment Analysis on Financial Microblogs and News

FineNews allows to perform fine-grained semantic sentiment analysis on financial microblogs and news.
The system detects cashtags mentioned in a message and predicts for each stock symbol a real-valued sentiment score in the range between -1 and 1.
The sentiment score reflects the point of view of an investor. Messages containing information that reveals a positive trend for a company or stock are annotated with positive values, while messages implying a negative trend are annotated with negative sentiment scores. Neutral messages get labeled with a score equal to 0.

### Dataset

The system has been tested on data from [SemEval 2017 Task 5](http://alt.qcri.org/semeval2017/task5/).

The run the system, you can download the following datasets:
* [Financial Microblogs](https://bitbucket.org/ssix-project/semeval-2017-task-5-subtask-1/)
* [Financial News Headlines](https://bitbucket.org/ssix-project/semeval-2017-task-5-subtask-2/)

Once downloaded, the datasets have to be placed into the ```data``` directory.
