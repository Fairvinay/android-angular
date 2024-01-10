package com.budget.client;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

// Regex to replace [a-zA-z]*[:][\s][']*
public class UsersList {

  static User[] USERS = {
   new User(
    "1", "admin@app.com",
    "$2y$10$k.58cTqd/rRbAOc8zc3nCupCC6QkfamoSoO2Hxq6HVs0iXe7uvS3e",   // '123'
    "ADMIN",
    true,
    "password" )
  ,
     new User(
    "2",
      "1",
    "bartosz@app.com",
       // '123'
    "$2y$10$k.58cTqd/rRbAOc8zc3nCupCC6QkfamoSoO2Hxq6HVs0iXe7uvS3e 'OWNER' ",
    true,
    "password 'FB2S2HQLIE2UIZQDGYLCMS3SNZMXQDSK'"


     ),
     new User(
    "3",
      "2",
    "john@app.com",
       // '123'
    "$2y$10$k.58cTqd/rRbAOc8zc3nCupCC6QkfamoSoO2Hxq6HVs0iXe7uvS3e 'OWNER' " ,
    true,
    "password"
     ),
     new User(
    "4",
      "2",
    "mike@app.com",
    "READER",
    true,
    "password"
     ),
     new User(
    "5",
      "1",
    "hi@bartosz.io",
    "OWNER",
    true,
    "{ '8076187'  } , 'github'  }" ) ,
  new User    (
    "6",
      "1",
    "vickyscab24@gmail.com",
    "OWNER",
    true,
    "{ '111013284878009413865'   } , 'password' }"  ) ,
  new User (     "8",
      "1",
    "fairvinay@gmail.com",
    "OWNER",
    true,
    "$2a$10$cybQQz2tlSQm5fpn8dyv8ebDxaN/PCrHwt6hIH2nbkNrr2aQuWM9q { '108114016982917227438'   }, 'password' } "
  ) ,
  // 116215353782290208356
     new User(
    "7",
      "1",
    "anvekar.v.anandi@gmail.com",
    "OWNER",
    true,
    "{  '116215353782290208356'  }, 'password' }"
     )
  };

   public static List<User>  getUserList() {
     List<User> tempList = null;

     return tempList ;
   }
   public static String  getKey () {

     return "VERY_SECRET_KEY!";
   }
  public static User  getUserList(JSONObject userFetched) throws JSONException {
    List<User> tempList = null;
    JSONObject userFetchedTemp  = userFetched;
       String accountID =  userFetchedTemp.getString("sub");
       String email = userFetchedTemp.getString("email");
       String role = "USER";
       boolean confirmed = true ;
        String comfirmationCode = String.valueOf(UUID.randomUUID());
           Object externalId = JSONObject.quote(" { 'google' : '1'  } " );
         String createdWith = "password" ;
         List<User> ul = Arrays.asList(USERS);
    Iterator lItr = ul.iterator();
      User temp = null ;
      while( lItr.hasNext()){
             String emailIn = ((User) lItr.next()).email;
             if(email.equalsIgnoreCase(emailIn)){
               temp  =  ((User) lItr.next());
             }

      }
      if ( temp == null ) {
          temp = new User("1",accountID,email,role, comfirmationCode, confirmed,externalId , createdWith );
      }

    return temp ;
  }
}
