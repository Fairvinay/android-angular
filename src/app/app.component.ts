import { Component, OnInit  } from "@angular/core";
import { Capacitor } from "@capacitor/core";
//import Echo from './core/echoplugin';

@Component({
  selector: "app-root",
  templateUrl: "./app.component.html",
  styleUrls: ["./app.component.css"],
})




export class AppComponent implements OnInit {
  title = "app";
  

   constructor( ) {
	
    } 
   ngOnInit() {

    if(Capacitor.getPlatform()==='andorid'){
	console.log('Android !');
	//const value  = this.getEcho();
        //console.log('response from native ', value);
    }
    else{
	console.log('Web !');
	}

   }
 /*  async getEcho() {
	
	const { value} = await Echo.echo({value : 'Budget Client'});
	return value ;
    } 
*/
   
}
