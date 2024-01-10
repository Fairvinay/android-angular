  import { Injectable } from "@angular/core";
import { CanActivate, Router ,ActivatedRouteSnapshot, RouterStateSnapshot } from "@angular/router";
import { Observable } from "rxjs";
import { map, tap } from "rxjs/operators";
import { AuthService } from "../services/auth.service";

@Injectable({
  providedIn: "root",
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(next: ActivatedRouteSnapshot,
        state: RouterStateSnapshot): Observable<boolean> {
    console.log(" login  "+next.url[0].path)

    return this.authService.isLoggedIn$().pipe(
      tap((isLoggedIn) => {
        if (isLoggedIn) {
          this.router.navigate([this.authService.INITIAL_PATH]);
        }
      }),
      map((isLoggedIn) => !isLoggedIn)
    );
  }
}
