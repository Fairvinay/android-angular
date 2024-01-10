import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable , of , from} from "rxjs";
import { CapacitorHttp , HttpResponse  } from '@capacitor/core';

import { config } from "../../core/config";
import { ExpenseCategory } from "@models/expenseCategory";

@Injectable()
export class ExpenseCategoryApi {
  private API_URL = `${config.apiUrl}/expense-categories`;

  constructor(private http: HttpClient) {}

  getExpenseCategories(): Observable<ExpenseCategory[]> {
	const options = {
  			  url: this.API_URL,
  			  headers:{ 
				'Content-Type':'application/json'
				}
 		 };	

    var postData:any = null;
          const response = from( async function () { 
						const r = await CapacitorHttp.get(options).then( res =>  {return res['data'] !==undefined ? res['data'] : null; }
					        	);
						return r;

			  }());
	  if( response!=undefined ) {
	    	return response;
	  }
	  else {
	     return this.http.get<ExpenseCategory[]>(this.API_URL);
	   }	
   
  }

  createExpenseCategory(category: ExpenseCategory): Observable<any> {
   const options = {
  			  url: this.API_URL,
  			  headers:{ 
				'Content-Type':'application/json'
				}, 
			  data: JSON.stringify(category)
 		 };	

    var postData:any = null;
    const response = from( async function () { 
						const r = await CapacitorHttp.post(options).then( res =>  {return res['data'] !==undefined ? res['data'] : null; }
					        	);
						return r;

			  }());
	  if( response!=undefined ) {
	    	return response;
	  }
	  else {		
	       return this.http.post(this.API_URL, category);
	}
  }

  updateExpenseCategory(category: ExpenseCategory): Observable<any> {
	const options = {
  			  url: `${this.API_URL}/${category.id}`,
  			  headers:{ 
				'Content-Type':'application/json'
				}, 
			  data: JSON.stringify(category)
 		 };	

    var postData:any = null;
    const response = from( async function () { 
					const r = await CapacitorHttp.put(options).then( res =>  {return res['data'] !==undefined ? res['data'] : null; }
				       	);
						return r;

			  }());
	  if( response!=undefined ) {
	    	return response;
	  }
    else {
   	 return this.http.put(`${this.API_URL}/${category.id}`, category);
	}
  }
}
