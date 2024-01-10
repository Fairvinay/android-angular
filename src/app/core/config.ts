
import { environment } from "../../environments/environment";

interface Config {
  [key: string]: string;
  auth: "session" | "token";
}

// Session auth needs to use the same origin anyway
export const config: Config = {
  apiUrl: environment.backend?.baseURL != undefined ? environment.backend?.baseURL + "/api": '' + "/api",  //http://localhost:8080/
  adminUrl: environment.backend?.baseURL != undefined ? environment.backend?.baseURL + "/api/admin": '' + "/api/admin",  //http://localhost:8080/
  authUrl: environment.backend?.baseURL != undefined ? environment.backend?.baseURL + "/api/auth" : '' + "/api/auth",
  authExtUrl: environment.backend?.baseURL != undefined ? environment.backend?.baseURL +  "/api/auth/" : '' +  "/api/auth/",
  auth: "token" ,  // "session"  this has issue's reading from Inmemory respoto
  glaubUrl: environment.backend?.glaubURL != undefined ? environment.backend?.glaubURL +"/api/auth/" : "https://reach.glaubhanta.site" +"/api/auth/"
};

export const auth0 = {
 // url: "https://dev-5qi53ez9.eu.auth0.com/v2",
 // clientId: "3GGnK7fa8QXii04i1EBsmVKNvgChLvr4",
 // returnUrl: "http://localhost:8080",

  url: "https://dev-64006009.okta.com/oauth2/default",
  clientId: "0oa7qulqh5mEM9Dae5d7",
  returnUrl: "http://localhost:8080",
};
