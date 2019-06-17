import {RouterModule} from '@angular/router';
import {ToastyModule} from 'ngx-toasty';
import {TranslateModule, TranslateLoader} from '@ngx-translate/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BrowserModule} from '@angular/platform-browser';
import {createTranslateLoader} from './common/translation';
import {ROUTES} from './router/router.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RestLocatorService} from './common/rest/services/rest-locator.service';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {
    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatNativeDateModule,
    MatOptionModule,
    MatRadioModule,
    MatSelectModule,
    MatSidenavModule,
    MatSlideToggleModule,
    MatTabsModule,
    MatToolbarModule
} from '@angular/material';
import { LazyLoadImageModule } from 'ng-lazyload-image';


export const IMPORTS=[
  BrowserModule,
  BrowserAnimationsModule,
  FormsModule,
  ReactiveFormsModule,
  HttpClientModule,
  LazyLoadImageModule,
  MatButtonModule,
  MatTabsModule,
  MatSidenavModule,
  MatCheckboxModule,
  MatSlideToggleModule,
  MatMenuModule,
  MatOptionModule,
  MatSelectModule,
  MatAutocompleteModule,
  MatToolbarModule,
  MatRadioModule,
  MatCardModule,
  MatDatepickerModule,
  MatNativeDateModule,
  MatAutocompleteModule,
  MatOptionModule,
  MatFormFieldModule,
  ReactiveFormsModule,
  MatInputModule,
    TranslateModule.forRoot({
    loader:{
      provide: TranslateLoader,
      useFactory: createTranslateLoader,
      deps: [HttpClient,RestLocatorService]
    }
  }),
  ToastyModule.forRoot(),
  RouterModule.forRoot(ROUTES),
];
