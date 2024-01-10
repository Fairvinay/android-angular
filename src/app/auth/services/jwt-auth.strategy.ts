import { Observable, of } from "rxjs";
import { AuthStrategy } from "./auth.strategy";
import { Token } from "@models/token";
import { User } from "@models/user";

export class JwtAuthStrategy implements AuthStrategy<Token> {
  private readonly JWT_TOKEN = "JWT_TOKEN";

  doLoginUser(token: Token): void {
    console.log(" doLogin user "+token)
    localStorage.setItem(this.JWT_TOKEN, token.jwt);
  }

  doLogoutUser(): void {
    localStorage.removeItem(this.JWT_TOKEN);
  }

  getCurrentUser(): Observable<User> {
    const token = this.getToken();
    if (token) {
      const encodedPayload = token.split(".")[1];
      let payJSON :User = null;
      let payload ="";
      try { 

        payload = window.atob(encodedPayload);
      payJSON = JSON.parse(payload)
      } catch(error){
        console.log("Initial empy load issue | improper logout ")
        return of(undefined);
      }
      return of(payJSON);
      
    } else {
      console.log("Initial empy load issue | improper logout ")
      return of(undefined);
    }
  }

  getToken() {
    return localStorage.getItem(this.JWT_TOKEN);
  }
   stringify(obj: JwtAuthStrategy ) { 
     let cache =[];
        let str = JSON.stringify(obj, function(key,value) { 
		  if(typeof value==='object' && value !==null) { 
			if(cache.indexOf(value) !== -1) {
                           // Circular reference found , discard key 
			  return ;
                         }
                        // Store value in out collection
                       cache.push(value);
                   }
                   return value;
                  }); 
       cache = null; 
       return str; 
  }   
  

}
