db.user.aggregate([
    {
      "$unwind":"$posts"
    },
    {
      "$project": {
        "username": 1,
        "object_size": { $bsonSize: "$posts" }
      }
    },
    {
      "$group": {"_id":null, "avg":{"$avg":"$object_size"}}
    }
  ])