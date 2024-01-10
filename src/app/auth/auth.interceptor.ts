import { Injectable, Inject } from "@angular/core";
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";

import { config } from "../core/config";
import { AuthService } from "./services/auth.service";
import { JwtAuthStrategy } from "./services/jwt-auth.strategy";
import { AUTH_STRATEGY } from "./services/auth.strategy";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    @Inject(AUTH_STRATEGY) private jwt: JwtAuthStrategy
  ) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    console.log("Auth Interceptor ")
    // check of Authorization token present 
    let reqHeaders = request.headers;
    if(reqHeaders.get("jwt_token") !=undefined){
      console.log("jwt present ")
        request.params.set("jwt_token",reqHeaders.get("jwt_token"));
    }
    if (config.auth === "token" && this.jwt && this.jwt.getToken()) 
    {
      request = this.addToken(request, this.jwt.getToken());
    }

    return next.handle(request).pipe(
      catchError((error) => {
        if (error.status === 401) {
          this.authService.doLogoutAndRedirectToLogin();
        }
        return throwError(error);
      })
    );
  }

  private addToken(request: HttpRequest<any>, token: string) {
    return request.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
  }
}
