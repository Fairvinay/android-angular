import { DashboardModule } from "./dashboard/dashboard.module";
import { AppRoutingModule } from "./app-routing.module";
import { CoreModule } from "./core/core.module";
import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";
//import { BrowserXhr } from "@angular/http";
//import { CustExtBrowserXhr } from "./settings/CustExtBrowserXhr";

import { AppComponent } from "./app.component";
import { AuthModule } from "./auth/auth.module";

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    CoreModule,
    AuthModule,
    AppRoutingModule,
    DashboardModule,
  ],
  providers: [ /*{provide:BrowserXhr , useClass: CustExtBrowserXhr } */],
  bootstrap: [AppComponent],
})
export class AppModule {}
