
/*This query update the password field on user collection, setting it to default hashed value 
  hash("password") = "$2a$12$psGftmPI5LFtbcet5ccfm.fURsX8apkuoJrJsihCaouuT0F0OZLri"  */

db.user.updateMany(
    {},
    {$set: { password: "$2a$12$psGftmPI5LFtbcet5ccfm.fURsX8apkuoJrJsihCaouuT0F0OZLri" }}
    );