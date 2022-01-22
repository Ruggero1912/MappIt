db.place.aggregate(
    {$unwind: "$posts"},
    {$project : {_id:0, "posts._id":1, "posts.title":1, "posts.desc":1}}
).forEach(function(x) {
    var newDescription;
    var description = x.posts.desc;
    if(description.length>75){
        newDescription = description.slice(0,75).concat("...");
    } else{
        newDescription = description;
    }
    
    db.place.updateOne(
        {"posts._id": ObjectId(x.posts._id.toString())},
        {$set:{"posts.$.desc" : newDescription}}
    );
});