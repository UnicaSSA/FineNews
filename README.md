# FineNews

FineNews allows to perform fine-grained semantic sentiment analysis on financial microblogs and news.
The system detects cashtags mentioned in a message and predicts for each stock symbol a real-valued sentiment score in the range between -1 and 1.
The sentiment score reflects the point of view of an investor. Messages containing information that reveals a positive trend for a company or stock are annotated with positive values, while messages implying a negative trend are annotated with negative sentiment scores. Neutral messages are classified with a score equal to 0.
