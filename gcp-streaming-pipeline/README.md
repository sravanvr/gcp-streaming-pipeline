# Uber to BigTable Pipeline

Dataflow streaming pipeline for reading Json based Uber messages from a PubSub topic, convert the Json Uber messages to Mutations and then write to BigTable.

# Use below PipelineOptions to run this pipeline.

--runner=DataflowRunner\
--project=gcp-streaming-pipeline\
--inputTopic=projects/gcp-streaming-pipeline/topics/gcp-streaming-pipeline-topic\
--inputSubscription=projects/gcp-streaming-pipeline/subscriptions/as1\
--bigtableInstanceId=gcp-streaming-pipeline\
--accountTableId=gcp-streaming-pipeline-accounts\
--lookupTableId=gcp-streaming-pipeline-lookup\
--jobName=Uber-Accounts-DataFlow\
--chiefUrl=http://localhost/uber-chef \
--httpConnectionTimeout=20000\
--httpMaxConnections=5\
--region=us-west2\
--numWorkers=3\
--defaultWorkerLogLevel=DEBUG\


  
# gcp-streaming-pipeline
