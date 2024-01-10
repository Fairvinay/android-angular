import {
  Component,
  ViewChildren,
  QueryList,
  OnDestroy,
  AfterViewInit,
  ChangeDetectorRef,
} from "@angular/core";
import {
  Router,
  NavigationEnd,
  NavigationStart,
  NavigationError,
  NavigationCancel,
  ActivationStart,
} from "@angular/router";
import { Location } from "@angular/common";
import { Subscription, Observable } from "rxjs";
import { MatIconRegistry } from "@angular/material/icon";
import { DomSanitizer } from "@angular/platform-browser";
import { MenuItemDirective } from "./../menu-item.directive";
import { AuthService } from "../../auth/services/auth.service";

@Component({
  selector: "layout",
  templateUrl: "./layout.component.html",
  styleUrls: ["./layout.component.scss"],
})
export class LayoutComponent implements AfterViewInit, OnDestroy {
  @ViewChildren(MenuItemDirective)
  private buttons: QueryList<MenuItemDirective>;
  private routerSub: Subscription;
  public loadingRoute = false;
  public userEmail$: Observable<string>;
  public selected: string;

  constructor(
    private cdr: ChangeDetectorRef,
    private location: Location,
    private authService: AuthService,
    private router: Router,
    iconRegistry: MatIconRegistry,
    sanitizer: DomSanitizer
  ) {
    iconRegistry.addSvgIcon(
      "home",
      sanitizer.bypassSecurityTrustResourceUrl("assets/home.svg")
    );
    iconRegistry.addSvgIcon(
      "list",
      sanitizer.bypassSecurityTrustResourceUrl("assets/list.svg")
    );
    iconRegistry.addSvgIcon(
      "settings",
      sanitizer.bypassSecurityTrustResourceUrl("assets/settings.svg")
    );
    iconRegistry.addSvgIcon(
      "edit",
      sanitizer.bypassSecurityTrustResourceUrl("assets/edit.svg")
    );
    iconRegistry.addSvgIcon(
      "remove",
      sanitizer.bypassSecurityTrustResourceUrl("assets/remove.svg")
    );

    this.routerSub = router.events.subscribe((e) => {
      if (e instanceof NavigationStart || e instanceof ActivationStart) {
        this.loadingRoute = true;
      } else if (
        e instanceof NavigationEnd ||
        e instanceof NavigationError ||
        e instanceof NavigationCancel
      ) {
        this.loadingRoute = false;

        this.selectCurrentRoute();
      }
    });
    this.userEmail$ = this.authService.getUserEmail$();
  }

  private selectCurrentRoute() {
    this.select(this.location.path().split("/")[2]);
  }

  private select(name: string) {
    if (this.buttons) {
      this.buttons.forEach(
        (button) => (button.isSelected = button.name === name)
      );
    }
    this.selected = name;
  }

  logout() {
    console.log("layout logout " );
    this.deleteAllCookies();
    this.authService.logout().subscribe(() => {
      this.router.navigate([ this.authService.LOGOUT_PATH 
]);  //this.authService.LOGIN_PATH
    });
  }
  deleteAllCookies() {
      var cookies = document.cookie.split(";");
  
      for (var i = 0; i < cookies.length; i++) {
          var cookie = cookies[i];
          var eqPos = cookie.indexOf("=");
          var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
          document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
      }
  }
  ngAfterViewInit() {
    this.selectCurrentRoute();
    this.cdr.detectChanges();
  }

  ngOnDestroy() {
    this.routerSub.unsubscribe();
  }
}
