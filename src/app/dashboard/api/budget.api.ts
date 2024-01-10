import { Injectable } from "@angular/core";
import { HttpClient , HttpHeaders ,HttpErrorResponse } from "@angular/common/http";
import { Observable , of , from} from "rxjs";
import { map } from "rxjs/operators";

import { CapacitorHttp , HttpResponse  } from '@capacitor/core';

import { BudgetSummary } from "@models/budgetSummary";
import { Budget } from "@models/budget";
import { Period } from "@models/period";
import { ConfigProvider } from "../../core/config.provider";
import { environment } from "../../../environments/environment";

@Injectable()
export class BudgetApi {
  readonly apiUrl: string;
readonly envBackendUrl: string;

  constructor(private http: HttpClient, configProvider: ConfigProvider) {
    this.envBackendUrl = "";  // environment.backend?.baseURL != undefined ? environment.backend?.baseURL : '';
    
    this.apiUrl = this.envBackendUrl + configProvider.getConfig().apiUrl;
  }

  getBudgets(period: Period): Observable<Budget[]> {
    var getData:any = null;
	const options = {
  			  url: `${this.apiUrl}/budgets/`,
  			  headers:{ 
				'Content-Type':'application/json'
				},
   			  params: { 'month':`${period.month}`, 'year':`${period.year}`
			  }
 		 };	

     const response = from( async function () { 
						const r =  await CapacitorHttp.get(options).then(res => {
								 
							    if( res['status'] ==200) {
							    var data = res['data'] ;	
							    console.log('capacitor get budget data '+data);
          						     var output=''; 
 								getData = data;
            							return getData;
	 						    }
							    else {	
								console.log('capacitor budget get return failed ');							
								 throw new HttpErrorResponse({error: 'Invalid User Role or Access ', status:403})
 								}
							   });
						return r; 
			 	 }());

          if( response!=undefined ) {
		console.log('Capacitor response  observable ready ');
		return response.pipe(
      			  map((budgets: any[]) =>
        		  budgets.map((budget) => Budget.buildFromJson(budget))
       			 )
     		 );	
	  }	
	return;
    /*return this.http
      .get(`${this.apiUrl}/budgets/?month=${period.month}&year=${period.year}`)
      .pipe(
        map((budgets: any[]) =>
          budgets.map((budget) => Budget.buildFromJson(budget))
        )
      );*/
  }

  getBudgetSummary(period: Period): Observable<BudgetSummary> {
     
    
    var getData: any=null;
	const options = {
  			  url: `${this.apiUrl}/budget-summary/`,
  			  headers:{ 
				'Content-Type':'application/json'
				},
   			  params: { 'month':`${period.month}`, 'year':`${period.year}`
			  }
 		 };	

     const response = from( async function () { 
						const r =  await CapacitorHttp.get(options).then(res => {
								 
							    if(res['status']  !== undefined && res['accountId'] !== undefined ) {
							    var data = res ;	
							    console.log('capacitor get budget sum data '+JSON.stringify(data));
          						     var output=''; 
 								getData = data;
            							return getData;
	 						    }
							    else {	
								console.log('capacitor budget sum get return failed ');							
								 throw new HttpErrorResponse({error: 'Invalid User Role or Access ', status:403})
 								}
							 });
						return r; 
			 	 }());
    	 	 	
      if( response!=undefined ) {
		console.log('Capacitor response observable ready ');
		return response.pipe(map((summary) => BudgetSummary.buildFromJson(summary)));	

	}	
    /*return this.http
      .get<BudgetSummary>(
        `${this.apiUrl}/budget-summary/?month=${period.month}&year=${period.year}`
      )
      .pipe(map((summary) => BudgetSummary.buildFromJson(summary)));*/
  }
}
