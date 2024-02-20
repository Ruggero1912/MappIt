# MappIt

MappIt is an application that allows users to discover new places to visit and share their adventures and
experiences, helping creators to promote their contents on the community.


### MappIt application architecture

MappIt is an application based on the client-server paradigm. We designed and implemented in detail the server
part.

![image](https://user-images.githubusercontent.com/63967908/201122489-577e84a8-d694-49aa-aaa3-17b27ccd0c48.png)


### Cluster architecture

MappIt was deployed on a cluster of three servers, in which each of them was in charge of a different part of the service.

In particular we had:
- server A: which run the Java backend of the service and was part of the MongoDB cluster
- server B: which run the Neo4j server and was part of the MongoDB cluster
- server C: which run the data population periodic scripts and was part of the MongoDB cluster

![image](https://user-images.githubusercontent.com/63967908/201122388-68341dd4-7f19-4f34-ad58-af50800bb652.png)

### Database entities

The main entities of MappIt are:
- user
- place
- post

There are different kind of relations between these three entities, and some attributes of the entities are stored only on the document database (MongoDB) while some other information are stored only in the graph database (Neo4j).

#### Neo4j entities and relations

In the following it is reported a schema of the entities and relations declared in the graph database:

![image](https://github.com/Ruggero1912/MappIt/assets/63967908/81a224b9-1a1d-4cc0-b450-14d035d0f152)


### Data population service


### UML use case diagram
![image](https://user-images.githubusercontent.com/63967908/201122582-78d117dd-38e8-45ad-9261-50646ea84e37.png)


### Full project documentation
Have a look at the full project documentation [at this link](/documentation/Mappit-documentation.pdf)
