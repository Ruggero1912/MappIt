
/* The query adds the countryCode field in Place Collection, setting it to the countryCode of one of its post */

db.user.aggregate(
    {$match:
        {countryCode: {$exists:false}}
    },
    {
        $addFields:{"userId":{$toString:"$_id"}}
    },  
    {$lookup:
        {
          from:"post",
          localField:"userId",
          foreignField:"author",
          as:"posts"
        }    
    },
    {$unwind: "$posts"},
    {$project: {_id:0, "posts.countryCode":1, _id:1}
    },
    {$group:
        {
            _id: "$_id",
            country: { $first : "$posts.countryCode" }
        }
    }).forEach( function (x){
        db.user.updateOne(
            {_id: ObjectId(x._id.toString())},
            {$set:{countryCode : x.country}}
            );
    });