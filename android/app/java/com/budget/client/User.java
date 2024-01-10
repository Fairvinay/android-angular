package com.budget.client;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

  String id =  "8";
  String accountId= "1";
  String email= "fairvinay@gmail.com";
  String role= "OWNER";
  String password = "$2a$10$cybQQz2tlSQm5fpn8dyv8ebDxaN/PCrHwt6hIH2nbkNrr2aQuWM9q";// 12345
  boolean confirmed=  true;
  Object externalId=  "{ google: '108114016982917227438'  }" ;
  String  createdWith = "password" ;

  public User(String number, String mail, String s, String admin, boolean b, String password) {


  }

  public User(String id, String accountId, String email, String role, String password, boolean confirmed, Object externalId, String createdWith) {
    this.id = id;
    this.accountId = accountId;
    this.email = email;
    this.role = role;
    this.password = password;
    this.confirmed = confirmed;
    this.externalId = externalId;
    this.createdWith = createdWith;
  }

  @Override
  public String toString() {
    return "User{" +
      "id='" + id + '\'' +
      ", accountId='" + accountId + '\'' +
      ", email='" + email + '\'' +
      ", role='" + role + '\'' +
      ", password='" + password + '\'' +
      ", confirmed=" + confirmed +
      ", externalId=" + externalId +
      ", createdWith='" + createdWith + '\'' +
      '}';
  }



  public String toJSONString() throws JSONException {
    JSONObject jo = new JSONObject();
    jo.put("id", id);
    jo.put("accountId", accountId);
    jo.put("email", email);
    jo.put("role", role);

    jo.put("password", password);
    jo.put("confirmed", confirmed);
    jo.put("externalId", externalId);
    jo.put("createdWith", createdWith);
    return jo.toString();
    /*return "User{" +
      "id='" + id + '\'' +
      ", accountId='" + accountId + '\'' +
      ", email='" + email + '\'' +
      ", role='" + role + '\'' +
      ", password='" + password + '\'' +
      ", confirmed=" + confirmed +
      ", externalId=" + externalId +
      ", createdWith='" + createdWith + '\'' +
      '}';

     */
  }

  public String getEmail() { return  email;
  }

  public void setEmail(String email) { this.email= email;
  }
}
