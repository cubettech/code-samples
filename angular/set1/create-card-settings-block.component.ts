import { TargetedAudienceComponent } from './../targeted-audience/targeted-audience.component';
import { Location } from '@angular/common';
import { AlertService } from './../../../_services/alert.service';
import { MapsAPILoader } from '@agm/core';
import { CardService } from './../../cards/card.service';
import { Card } from './../../../_models/card';
import { Component, OnInit, Input, ViewChild, ElementRef, NgZone, ComponentFactoryResolver, ViewContainerRef } from '@angular/core';
import * as moment from 'moment';
import { Router } from '@angular/router';
import {NgForm, FormControl, FormGroup, Validators, FormArray, FormBuilder} from '@angular/forms';

@Component({
  selector: 'create-card-settings-block',
  templateUrl: './create-card-settings-block.component.html',
  styleUrls: ['./create-card-settings-block.component.css']
})
export class CreateCardSettingsBlockComponent implements OnInit {
  @Input() audianceAgeRange : Array<string>;
  @Input() cardBudgets : Array<any>;
  @Input() cardFrequency : Array<any>;
  @Input() card: Card;
  @Input() edit: boolean = false;
  today: string ;
  loading : boolean = false;
  calculatedBudget : string = "";
  targetAudiance : string = "all";
  @Input() returnUrl : string;
  @Input() hasHistory : boolean;
  @Input() cardHistory : Array<any>;
  audianceAgeRangeValues : Array<any>;
  disableAll : boolean = false;
  targetAudienceHistory : Array<any> = [];
  tgForms = [{}];
  targetAudience: FormGroup;
  targetAudienceSelect : string = 'all';

  public userSettings1: any = {
    showSearchButton: false,
    inputPlaceholderText: 'Enter Your City',
    showCurrentLocation: false,
    showRecentSearch: false,
  };

  @Input() model: any = {};
  @ViewChild('audianceTarget', { read: ViewContainerRef })    container: ViewContainerRef; 
  constructor(
    private cardService : CardService,
    private mapsAPILoader : MapsAPILoader,
    private ngZone : NgZone,
    private alertService: AlertService,
    private router : Router,
    private location : Location,
    private _cfr: ComponentFactoryResolver,
    private _fb: FormBuilder
  ) { 
    this.model.audience_age_range="";
    this.model.frequency="lifetimebudget";
    this.model.budget="";
    this.model.start_and_end=true;
    this.model.time_type = true;
    this.model.date_from = moment().format('YYYY/MM/DD HH:mm');
    
  }

  ngOnInit() {
    this.today = moment().format('YYYY/MM/DD HH:mm');
    if(this.model.status == 'paused'){this.disableAll = true}
    if(this.edit){
      this.calculateCardBudget();
      if(this.model.audience){
        this.targetAudience = this._fb.group({
          audiences: this._fb.array([])
        });
        var that = this;
        this.model.audience.forEach(function (value) {
           that.targetAudienceHistory[value.id] = []
          if(value.audience_history.length){
            value.audience_history.forEach(function(audienceHistory){
              value[audienceHistory.field] = audienceHistory.value
              that.targetAudienceHistory[value.id].push({"field" : audienceHistory.field})
            })
          }
          that.addAudienceForEdit(value);
        });
      }else{
        this.targetAudience = this._fb.group({
          audiences: this._fb.array([])
        });
        this.addAudience();
      }
    }else{
        this.model.target = "all";
        this.targetAudience = this._fb.group({
          audiences: this._fb.array([])
        });
    }
  }

  initAudience(){
    return this._fb.group({
      ageFrom: ['',Validators.required],
      ageTo: ['',Validators.required],
      audienceAddress: ['',Validators.required],
      gender: ['',Validators.required],
      city: [''],
      region: [''],
      country:[''],
      address:[''],
      id:['']
    });
  }

  addAudience() {
      const control = <FormArray>this.targetAudience.controls['audiences'];
      const addrCtrl = this.initAudience();
      
      control.push(addrCtrl);
  }

  addAudienceForEdit(audience: any){
    const control = <FormArray>this.targetAudience.controls['audiences'];
    const addrCtrl = this.addAudienceForm(audience);
    control.push(addrCtrl);
  }

  addAudienceForm(audience:any){
    var gender = audience.gender == 1 ? 'male' : 'female';
    return this._fb.group({
      ageFrom: [audience.age_from,Validators.required],
      ageTo: [audience.age_to,Validators.required],
      audienceAddress: [audience.location,Validators.required],
      gender: [gender,Validators.required],
      city: [audience.city],
      region: [audience.region],
      country:[audience.country],
      address:[audience.location],
      id:[audience.id]
      
    });
  }

  removeAudience(i: number) {
    const control = <FormArray>this.targetAudience.controls['audiences'];
    control.removeAt(i);
  }

  updateCardSettings(settingsForm : NgForm){
    let audianceAgeRange = this.targetAudience.value;
    if(this.model.target == 'targeted'){
      if(!audianceAgeRange.audiences.length){
        return;
      }else{
        for(let audience of audianceAgeRange.audiences){
          if(!audience.address || !audience.ageFrom || !audience.ageTo || !audience.gender || !audience.audienceAddress)
          {
            return;
          }
        }
      }      
    }
    this.loading = true;
    if(!this.edit){
      this.cardService.upateSettings(this.model, audianceAgeRange.audiences, this.card.id)
      .subscribe(
        data => {
          this.loading = false;
          this.router.navigate(['/card/manage']);
          this.alertService.success("Done! You’ve successfully created "+this.card.name, true)
        },
        error => {
          this.loading = false;
          this.alertService.error(error.error.message);
        }
      );
    }else{
      if(this.model.time_type){
        this.model.date_from = this.today;
      }
      this.cardService.editUpateSettings(this.model, audianceAgeRange.audiences, this.card.id)
      .subscribe(
        data => {
          this.loading = false;
          this.location.back();
          this.alertService.success("You have successfully saved changes to "+this.card.name+". These changes will be available in the app once approved by Admin.", true)
        },
        error => {
          this.loading = false;
          this.alertService.error(error.error.message);
        }
      );
    }
  }

  dateFromSelected(event){
    let fromDate = moment(event.value).format('YYYY/MM/DD HH:mm');
    let toDate = moment(this.model.date_to).format('YYYY/MM/DD HH:mm');
    if(toDate < fromDate){
       this.model.date_to = "";
    }
    this.calculateCardBudget()
  }

  dateToSelected(event){
    this.calculateCardBudget();
  }

  campaignTypeChanged($event){
    if(!this.model.time_type){
      this.model.frequency="daily";
    }else{
      this.model.date_from = moment().format('YYYY/MM/DD HH:mm');
      this.model.date_to = "";
      this.model.frequency="lifetimebudget";
    }
    this.calculateCardBudget();
  }

  budgetChanged(){
    this.calculateCardBudget();
  }

  calculateCardBudget(){
    if(!this.model.time_type)
    {
      if(
        this.model.date_to &&
        this.model.date_from &&
        this.model.budget
      ){
        var from = moment(this.model.date_from).startOf('day');
        var to = moment(this.model.date_to).endOf('day');
        let days = to.diff(from, 'days');
        days = days + 1;
        let budgetAmount = this.model.budget;
        this.calculatedBudget = (days * budgetAmount).toString();
      }else{
        this.calculatedBudget = "";
      }
    }else
    {
      if(this.model.budget){
        let budgetAmount = this.model.budget;
        this.calculatedBudget = budgetAmount.toString();
      }else{
        this.calculatedBudget = "";
      }
    }
  }

  targetChanged(value){
    this.targetAudiance = value;
    if(!this.targetAudience.value.audiences.length){
      this.addAudience();
    }
  }

  addNewAudianceTarget(){
    this.tgForms.push({});
  }
  isReadOnly(){
    if(this.model.status == 'paused'){
      return true;
    }else{
      return false;
    }
  }

  targetAudienceHistoryFn(){

    return this.targetAudienceHistory;
  }
}
