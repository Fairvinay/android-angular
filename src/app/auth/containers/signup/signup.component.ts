import { Component, OnInit } from "@angular/core";
import { FormGroup, FormBuilder, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { AuthService } from "../../services/auth.service";
import { config, auth0 } from "../../../core/config";

@Component({
  selector: "signup",
  templateUrl: "./signup.component.html",
  styleUrls: ["./../auth.scss"],
})
export class SignupComponent implements OnInit {
  signupForm: FormGroup;
  githubSignup = "Signup with GitHub";
  googleSignup = "Signup with Google";

  constructor(
    private authService: AuthService,
    private formBuilder: FormBuilder,
    private router: Router
  ) {}

  ngOnInit() {
    this.signupForm = this.formBuilder.group({
      email: ["", Validators.email],
      password: [""],
    });
  }

  get f() {
    return this.signupForm.controls;
  }

  signup() {
    this.authService
      .signup({
        email: this.f.email.value,
        password: this.f.password.value,
      })
      .subscribe((data) => { 
            let email = data.email ; let code = data.code;
           localStorage.setItem("email", email);
           localStorage.setItem("code", code);
           let url= this.authService.CONFIRM_PATH+"?email="+email+"&code="+code;
           console.log(" confirm URL path with params ....")
           console.log(` ${url} `)
          
          //let url= this.authService.CONFIRM_PATH+"?email="+email+"&code="+code;
        this.router.navigate([ this.authService.CONFIRM_PATH], {queryParams:{"email":email , "code": code}}) } );
  }
}
