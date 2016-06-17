There are 6 projects in this repository.

The sample implementation of the API is in psd2api and psd2datamodel project. Remaining are supporting projects. All of the projects are Spring boot projects. i.e. you dont need a web-server or an application server to run these apps. However, you would need a Mongo DB for running these projects. 

There is a postman json file in psd2api project. Import it in Postman to test the api.

PSD2 API is secured with OAUTH 2.0. You would need to have an OAUTH server and would have to link it with the psd2api server. In case you dont have it. Use the oauth2server project to start your own oauth 2 server. 

PSD2DemoApp contains API for a sample Third Party App which uses PSD2API. This app is under development currently. 

To run these projects.

1. Import these projects into Eclipse (these have been developed in Mars) as existing maven project
2. run maven goals (first on the psd2datamodel project and then on any other project). goals are clean and install
3. Configure the projects to run in eclipse. For all projects u need to set MONGO_SVC_URL environment variable. for instance the value could be: mongodb://localhost:27017
4. For the psd2api project you will need to set following additional environment variables:
  a. OAUTH_CHECKTOKEN_URL = http://<hostname:ip of oauth server>/oauth2server/oauth/check_token
  b. OAUTH_CLIENT_ID = psd2api (note: this is the client that needs to be created on oauth2 server)
  c. OAUTH_CLIENT_SECRET = password01 (note: client secret of the psd2api user on the oauth2 server)
  d. USE_KAFKA = (true) if you want to publish the transaction request message on Kafka (so that it can be picked up by the integration service/payment processor for further processing
5. If you set USE_KAFKA to true, you will need to create a mongodb collection (kafkaproperties) in the db psd2api. The structure of this collection is as follows: 
    {"uuid" : "kafka-properties", "bootstrapServers" : "172.17.0.2:9092", "acks" : "0", "retries" : "0", "batchSize" : "16384", "lingerMs" : "1", "bufferMemory" : "33554432", "keySerializer" : "org.apache.kafka.common.serialization.StringSerializer", "valueSerializer" : "org.apache.kafka.common.serialization.StringSerializer" }
6. run the servers. the psd2api server's default port is set to 8082. Please look at src/main/resources/application.properties to configure the server
7. Take the collection "PSD2API_Test.postman_collection.json" and import it in postman. 
8. in the colleciton there is "setup" folder. in that there are api's you can call to setup users and clients for the demo. 

