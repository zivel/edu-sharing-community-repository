// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export interface Env {
    EDU_SHARING_API_URL?: string;
}

export interface ExtendedWindow extends Window {
    __env?: Env;
}

declare var window: ExtendedWindow;

export const environment = {
    production: false,
    eduSharingApiUrl: window.__env?.EDU_SHARING_API_URL ?? 'http://localhost:4200/edu-sharing/rest',
    // eduSharingApiUrl: 'http://localhost:4200/edu-sharing/rest',
    // eduSharingApiUrl: 'http://repository.127.0.0.1.nip.io:8100/edu-sharing/rest',
};

window.__env = {
    EDU_SHARING_API_URL: environment.eduSharingApiUrl,
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.