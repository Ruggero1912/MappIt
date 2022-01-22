
/* The query adds the countryCode field in User Collection setting it to a specified code */

db.place.find({countryCode: {$exists:false}})
        .forEach( function (x){
            var codeToInsert = "IT";
            db.place.updateOne(
                {_id: ObjectId(x._id.toString())},
                {$set:{countryCode : codeToInsert}}
                );
        });