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

## Entities

The main entities of MappIt are:
- user
- place
- post
- activity

<img alt="UML entities diagram" src="https://github.com/Ruggero1912/MappIt/assets/63967908/333e6ed6-34b0-451a-927e-1308319de32c" style="width:500px;"/>


There are different kind of relations between these three entities, and some attributes of the entities are stored only on the document database (MongoDB) while some other information are stored only in the graph database (Neo4j).

### Neo4j entities and relations

In the following it is reported a schema of the entities and relations declared in the graph database:

![image](https://github.com/Ruggero1912/MappIt/assets/63967908/81a224b9-1a1d-4cc0-b450-14d035d0f152)

## Cross-database consistency management:

In this section we analyze queries that requires to be handled as they involve both the databases.

We designed data flow schemas in the cases of successfull or failed operations for each instruction, always aiming at preserving a state of consistency for the data.

In addition to perform an automatic attempt of consistency recovery, we decided to log errors into an
`errors.txt` file, allowing administrators to manually check and enforce consistency and restored the nominal state.

### Registration of a new user 

![image](https://github.com/Ruggero1912/MappIt/assets/63967908/bb64a504-304c-4a2c-972e-0ef620083bb0)

### Adding or Removing a like on post

![image](https://github.com/Ruggero1912/MappIt/assets/63967908/fdbb1b63-8454-4614-8e37-9c08ce43505d)


## Analysis queries

In the following are reported some queries we performed over the database to get interesting overviews of the data and extract information.

### Document database queries: MongoDB

#### Most popular posts of a given period

**Description:**
>*this query selects the most appreciated posts, in terms of likes received, in a period between two dates and filtering by an activity.*

**Mandatory parameters:** fromDate, toDate

**Optional parameters:** activity name and maximum number of posts to return

**Java method:** PostService.getPopularPosts

```javascript
db.post.aggregate([
  {$match:
    {activity:{$in:["activityName"]}},
  },
  {$match:
    {postDate:{$gte:"fromDate", $lt: "toDate" }}
  },
  {$sort:{likes:-1}},
  {$project:
    {
      _id:0,
      title:1,
      authorUsername:1,
      placeName:1,
      desc:1,
      postDate:1
    }
  },
  {$limit: "howManyResults"}
])
```

#### Graph queries: Neo4j

**Domain query**:

*What are the most visited places, between the ones visited by the followings of a specified user?*

**Graph-centric query:**

>*Considering U as all the User vertices that have an
incoming edge “FOLLOWS” from a specific User
vertex, select Place vertices that have an incoming edge
“VISITED” from U vertices. Then count the incoming
“VISITED” edges for each of those places.*

**Equivalent query in Cypher:**
```cypher
MATCH (u:User{username:$username})-[f:FOLLOWS]->(followings:User)-[v:VISITED]
->(p:Place)
WITH p.id AS id, p.name AS place, count(v) AS visitTimes
ORDER BY visitTimes DESC
LIMIT $howManyResults
RETURN id, place, visitTimes
```

**Domain query**:

*Makes suggestions about new posts in the same places to check, basing on users’ liked posts and ordering by number of likes*

**Graph-centric query:**

>*Considering P as the Post vertices with an incoming edge
“LIKES” from a specific User vertex, select PL as the
Place vertices that have an incoming edge
“LOCATION” from P. Then considering the Post
vertices with an outgoing edge “LOCATION” from PL,
count the incoming “LIKES” edges and sort Posts by this
value.*

**Equivalent query in Cypher:**
```cypher
MATCH (u:User{username:$username})-[:LIKES]->(p:Post)-[:LOCATION]->(pl:Place)
WITH DISTINCT pl AS places, COLLECT(p) AS likedPosts
MATCH (:User)-[l:LIKES]->(sp:Post WHERE NOT(sp IN likedPosts))-[:LOCATION]->(places)
WITH DISTINCT sp AS suggestedPosts, COUNT(l) AS likeReceived
ORDER BY likeReceived DESC
RETURN suggestedPosts.id, likeReceived, suggestedPosts.title
```


### Data population service


<!-- ### UML use case diagram
![image](https://user-images.githubusercontent.com/63967908/201122582-78d117dd-38e8-45ad-9261-50646ea84e37.png) -->


### Full project documentation

We analyzed deeper and more broadly the aforementioned aspects and even others, like:
- **databases queries analysis** by means of the *Operation Frequency Table*
- **Indexes** on certain collections and documents attributes to improve performances of certain frequent queries
- **Redundant fields** in documents to improve queries performances in terms of *executionStats*
- **Database sharding**: we proposed a database sharding based on the country code of places and users in order to grant higher service availability
- **Java application packages organization**
- **Java application databases connection handling**
- **Service endpoints**
- **Application use cases**
- **Functional requirements**
- **Non-Functional requirements**

Have a look at the full project documentation [at this link](/documentation/MappIt-documentation.pdf)
