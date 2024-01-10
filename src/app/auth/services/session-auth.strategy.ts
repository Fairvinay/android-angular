import { HttpClient } from "@angular/common/http";
import { Observable, of } from "rxjs";
import { tap } from "rxjs/operators";
import { config } from "../../core/config";
import { AuthStrategy } from "./auth.strategy";
import { User } from "@models/user";

export class SessionAuthStrategy implements AuthStrategy<User> {
  private loggedUser: User;

  constructor(private http: HttpClient) {}

  doLoginUser(user: User): void {
    this.loggedUser = user;
  }

  doLogoutUser(): void {
    this.loggedUser = undefined;
  }

  getCurrentUser(): Observable<User> {
    if (this.loggedUser) {
      return of(this.loggedUser);
    } else {
      return this.http
        .get<User>(`${config.authUrl}/user`)
        .pipe(tap((user) => (this.loggedUser = user)),tap((user) => console.log ("SAuthStra "+JSON.stringify(user))) );
    }
  }
  stringify(obj: SessionAuthStrategy ) { 
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
