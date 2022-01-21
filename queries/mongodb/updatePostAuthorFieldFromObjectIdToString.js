
/* The query updates the author field from ObjectId to String, inside post collection */

db.post.find({author:{ $type: "objectId"}, title:"Esplorazione La Rocca Della Verruca"}).forEach( function (x) {
    var authorString = x.author.toString();
    db.post.updateOne(
        {_id: ObjectId(x._id.toString())},
        {$set:{author : authorString}}
        );
});