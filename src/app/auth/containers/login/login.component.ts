import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { Router, ActivatedRoute } from "@angular/router";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog } from "@angular/material/dialog";
import { retryWhen, tap, switchMap, filter } from "rxjs/operators";
import { Observable } from "rxjs";
//import { Buffer } from 'buffer';
import { SnackBarComponent } from "./../../../shared/components/snackbar/snackbar.component";
import { OtpComponent } from "../../components/otp-dialog/otp.component";
import { AuthService } from "../../services/auth.service";
import { OAuthService } from "./../../services/oauth.service";
import { LoginRequest } from "@models/loginRequest";
import { HttpErrorResponse } from "@angular/common/http";

import { Toast } from '@capacitor/toast';


@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./../auth.scss"],
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  githubLogin = "Login with GitHub";
  googleLogin = "Login with Google";
  glaubData ="";
  showGlaubDefaultLoginBtn =false;
  glaubUser:any = null;

  constructor(
    private authService: AuthService,
    private oauthService: OAuthService,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.loginForm = this.formBuilder.group({
      email: ["", Validators.email],
      password: [""],
      glaubData:[""]   
    });

    const msg = this.route.snapshot.queryParams.msg;
    if (msg) {
      this.snackBar.openFromComponent(SnackBarComponent, {
        duration: 3000,
        data: msg,
      });
    }
  }

  get f() {
    return this.loginForm.controls;
  }

  googleLogger(event: any, extUrl:string){

    //https://localhost:8080/api/auth/external/google/login
    /* const loginRequest: LoginRequest = {
      email: 'fairvinay@gmail.com',
      password: 'Forit@123_Bench_123',
    };*/
    const loginRequest: LoginRequest = {
      email: this.f.email.value,
      password: this.f.password.value,
    };
    //if( this.f.isStateValid) { 

    console.log(event);
     console.log(extUrl);

    this.authService
      .loginExt(loginRequest,extUrl)
      .pipe(retryWhen(this.invalidOtp(loginRequest)))
      .subscribe((user) =>
        this.router.navigate([
          this.authService.getInitialPathForRole(user.role),
        ])
      );
    /*}
    else {
      this.catchLoginError("Invalid Details")
      event.preventDefault();
    }*/

  }

  login() {
    const loginRequest: LoginRequest = {
      email: this.f.email.value,
      password: this.f.password.value,
    };
     console.log("inside login componeit you clocked login button");
    this.authService
      .login(loginRequest)
      .pipe(retryWhen(this.invalidOtp(loginRequest)))
      .subscribe((user) =>
	
        this.router.navigate([
          this.authService.getInitialPathForRole(user.role),
        ]),
        (err) => this.catchLoginError(err)
         
      );//.catch((err: HttpErrorResponse) => {});
  }
   
   catchLoginError(err){
       console.log("errorr login ")
       this.loginForm.controls.email.setErrors({ invalid: err })
      
   }

   getGlaub() {
	 const loginRequest: LoginRequest = {
      email: this.f.email.value,
      password: this.f.password.value,
    };
     console.log("inside glaub ");
	
     this.authService
      .glaub(loginRequest)
      
      .subscribe((data) => {
		this.glaubData =JSON.stringify(data);
		 console.log(" glaub user " + JSON.stringify(data));
		 this.showGlaubDefaultLoginBtn = true;  
		 this.glaubUser= data;
		 this.f.glaubData.setValue(this.glaubData);
		this.loginForm.patchValue({glaubData: this.glaubData});
		/*this.loginForm.patchValue({textglb: data});
		/this.f.glaubData.setValue(data);*/	
		if(this.showGlaubDefaultLoginBtn && data?.day !=undefined ){
                  let th = data?.day
		  let tiIdx = th.lastIndexOf("\n");
  		   let tim = th.substring(tiIdx);
		   console.log("tim  "+tim);
			const showHelloToast = async () => {
 			 await Toast.show({
   			 text: 'Server loged: '+tim,
 				 });
			};
		   showHelloToast();
		 }
	
		
		}
             ,
        (err) => this.catchLoginError(err)
         
      );
  }
  defaultLogin() {
      if(this.glaubUser !=undefined){ 	
        this.router.navigate([
          this.authService.getInitialPathForRole(this.glaubUser.role)
        ]);
      }
  }

  getIdToken() {
    this.oauthService.requestIdToken();
  }

  private invalidOtp(loginRequest: LoginRequest) {
    console.log("invalid Otp ")
    return (errors: Observable<HttpErrorResponse>) =>
      errors.pipe(
        filter((err) => err.error.msg === "OTP_REQUIRED"),
        switchMap(() => this.requestOtp()),
        tap((otp) => (loginRequest.otp = otp))
      );
  }
    private encode(logReq: LoginRequest) {
       let enLogReq = Object.assign({}, logReq);
	     
      let buff = btoa(enLogReq.password)  // Buffer.from( enLogReq.password, 'base64').toString();
        enLogReq.password= (buff.split("").reverse().join(""));
       return enLogReq;
   }
  private requestOtp() {
    const config = {
      width: "400px",
      disableClose: true,
    };
    console.log("dialog opening .....")
    return this.dialog.open(OtpComponent, config).afterClosed();
  }
}
