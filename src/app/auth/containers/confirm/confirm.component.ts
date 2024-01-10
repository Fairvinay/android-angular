import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { AuthService } from "../../services/auth.service";

@Component({
  selector: "confirm",
  templateUrl: "./confirm.component.html",
  styleUrls: ["./confirm.component.scss"],
})
export class ConfirmComponent implements OnInit {
  isConfirmed = false;
  token:string;
   private readonly JWT_TOKEN = "JWT_TOKEN";
  constructor(
    private activeRoute: ActivatedRoute,
    private authService: AuthService
  ) {

  

  }

  ngOnInit(): void {
    let email = this.activeRoute.snapshot.queryParams.email;
    let code = this.activeRoute.snapshot.queryParams.code;

     this.token = this.activeRoute.snapshot.paramMap.get('jwt_token');
     console.log(" Confirm Construct typeof this.token "+(typeof this.token != undefined || this.token != null) )
    if (this.token != undefined || this.token != null ) {
        // set the jwt_token
          localStorage.setItem(this.JWT_TOKEN, this.token);
    }
      email = localStorage.getItem("email");
      code=      localStorage.getItem("code");
    if (code != undefined || code != null ) {
          localStorage.setItem(this.JWT_TOKEN, code);
    }

    if (email && code) {
      this.authService
        .confirm(email, code)
        .subscribe(() => (this.isConfirmed = true));
    }
  }
}
