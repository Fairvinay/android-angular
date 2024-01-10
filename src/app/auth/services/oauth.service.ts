import { Injectable } from "@angular/core";
import { googleClientId } from "./../../core/secret";
//import { OAuth2Client } from 'google-auth-library';

@Injectable({
  providedIn: "root",
})
export class OAuthService {
  private readonly STATE = "id_state";
  private readonly NONCE = "id_nonce";
  readonly STATE_LENGTH = 32;
  readonly NONCE_LENGTH = 16;
  private GOOGLE_CLIENT_ID = googleClientId ;

  readonly authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth";
  readonly accessTokenUrl = "https://oauth2.googleapis.com/token";
  readonly userInfoUrl = "https://openidconnect.googleapis.com/v1/userinfo";
  readonly redirectUri = "https://reach.glaubhanta.site/api/auth/external/google/callback";//"http://reach.glaubhanta.site/oauth";
  readonly responseType = "id_token";  // code also possible
  readonly scope = "openid email profile";

  requestIdToken() {
    const state = this.generateRandomString(this.STATE_LENGTH);
    const nonce = this.generateRandomString(this.NONCE_LENGTH);

    const url =
      `${this.authorizeUrl}` +
      `?client_id=${googleClientId}` +
      `&redirect_uri=${this.redirectUri}` +
      `&response_type=${this.responseType}` +
      `&scope=${this.scope}` +
      `&state=${state}` +
      `&nonce=${nonce}`;

    this.setCurrentState(state);
    this.setNonce(nonce);
    window.location.href = url;
  }

 async  decodeIdToken(token: string , usebase64: string) {
    if (token) {

	
      
      
		
		
      const encodedPayload = token.split(".")[1];
      const payload = window.atob(encodedPayload);
    if(payload) {	
      return JSON.parse(payload);
    } 
    else{ 
	//const client =   // new OAuth2Client(this.GOOGLE_CLIENT_ID)
	const userTicket = window.atob(usebase64); 
	
			/*  await client.verifyIdToken({
   				idToken:  token ,
   			  audience: this.GOOGLE_CLIENT_ID
                         }).then((response) => {
				
				if (response.getPayload() && response.getPayload()?.email_verified) {
    				  const email = response.getPayload()?.email
     				   const name = response.getPayload()?.name
				 return JSON.stringify ({
          				  id: '43',
                                         accountId: "1",
           				 name: name,
           				 email: email,
            				 role: 'user',
            				 token : token
          				 })

				}
			        else {
				return JSON.stringify ({ });

                                }
			
			    	
			  })*/
				
       return JSON.parse(userTicket)



	}
    }
  }

  isStateValid(state: string) {
    return (
      !!state &&
      state.length === this.STATE_LENGTH &&
      state === this.getCurrentState()
    );
  }

  isNonceValid(user: any) {
    const nonce = user?.nonce;
      
    if(user?.email.indexOf("gmail.com")){
    	 
      return true;
     } 
    else {
    return (
      !!nonce && nonce.length === this.NONCE_LENGTH && nonce === this.getNonce()
    );
    } 
  }

  private setCurrentState(state: string) {
    localStorage.setItem(this.STATE, state);
  }

  private getCurrentState() {
    return localStorage.getItem(this.STATE);
  }

  private setNonce(nonce: string) {
    localStorage.setItem(this.NONCE, nonce);
  }

  private getNonce() {
    return localStorage.getItem(this.NONCE);
  }

  private generateRandomString(length: number) {
    const chars =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    let state = "";
    for (let i = 0; i < length; i++) {
      state += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return state;
  }
}
