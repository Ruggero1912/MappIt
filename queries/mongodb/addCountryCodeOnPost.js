
/* The query adds the countryCode field in Post Documents, setting it to the countryCode of the location of that post */

db.post.find({countryCode: {$exists:false}})
        .forEach( function (x){
            var postId = ObjectId(x._id.toString());

            var docCountry = db.place.find(
                {_id: ObjectId(x.place)},
                {countryCode: 1, _id: 0}
            ).toArray();
            
            if(docCountry[0] != null){
                db.post.updateOne(
                    {_id: postId},
                    {$set:{countryCode : docCountry[0].countryCode}}
                    );
                }
        });