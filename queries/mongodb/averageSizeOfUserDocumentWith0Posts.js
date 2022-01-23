db.user.aggregate([
    {
      "$match":
      {
        "posts.0": {$exists: false}
      }
    },
    {
      "$project": {
        "username": 1,
        "object_size": { $bsonSize: "$$ROOT" }
      }
    },
    {
      "$group":
      {
        "_id":null, "average":{"$avg":"$object_size"}
      }
    }
  ])

  