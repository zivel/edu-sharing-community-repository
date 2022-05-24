/* tslint:disable */
/* eslint-disable */
import { Admin } from './admin';
import { AvailableMds } from './available-mds';
import { Banner } from './banner';
import { Collections } from './collections';
import { ConfigFrontpage } from './config-frontpage';
import { ConfigPrivacy } from './config-privacy';
import { ConfigPublish } from './config-publish';
import { ConfigRating } from './config-rating';
import { ConfigRemote } from './config-remote';
import { ConfigUpload } from './config-upload';
import { ConfigWorkflow } from './config-workflow';
import { ContextMenuEntry } from './context-menu-entry';
import { FontIcon } from './font-icon';
import { Guest } from './guest';
import { HelpMenuOptions } from './help-menu-options';
import { Image } from './image';
import { License } from './license';
import { LicenseAgreement } from './license-agreement';
import { LogoutInfo } from './logout-info';
import { Mainnav } from './mainnav';
import { MenuEntry } from './menu-entry';
import { Register } from './register';
import { Rendering } from './rendering';
import { Services } from './services';
import { SessionExpiredDialog } from './session-expired-dialog';
import { SimpleEdit } from './simple-edit';
import { Stream } from './stream';
export interface Values {
    admin?: Admin;
    allowedLicenses?: Array<string>;
    availableMds?: Array<AvailableMds>;
    availableRepositories?: Array<string>;
    banner?: Banner;
    branding?: boolean;
    collections?: Collections;
    customCSS?: string;
    customLicenses?: Array<License>;
    customOptions?: Array<ContextMenuEntry>;
    defaultPassword?: string;
    defaultUsername?: string;
    editProfile?: boolean;
    editProfileUrl?: string;
    extension?: string;
    frontpage?: ConfigFrontpage;
    guest?: Guest;
    helpMenuOptions?: Array<HelpMenuOptions>;
    helpUrl?: string;
    hideMainMenu?: Array<string>;
    icons?: Array<FontIcon>;
    images?: Array<Image>;
    imprintUrl?: string;
    itemsPerRequest?: number;
    licenseAgreement?: LicenseAgreement;
    licenseDialogOnUpload?: boolean;
    loginAllowLocal?: boolean;
    loginDefaultLocation?: string;
    loginProviderTargetUrl?: string;
    loginProvidersUrl?: string;
    loginUrl?: string;
    logout?: LogoutInfo;
    mainnav?: Mainnav;
    menuEntries?: Array<MenuEntry>;
    nodeReport?: boolean;
    privacy?: ConfigPrivacy;
    privacyInformationUrl?: string;
    publish?: ConfigPublish;
    publishingNotice?: boolean;
    rating?: ConfigRating;
    recoverPasswordUrl?: string;
    register?: Register;
    remote?: ConfigRemote;
    rendering?: Rendering;
    searchGroupResults?: boolean;
    searchSidenavMode?: string;
    searchViewType?: number;
    services?: Services;
    sessionExpiredDialog?: SessionExpiredDialog;
    simpleEdit?: SimpleEdit;
    siteTitle?: string;
    stream?: Stream;
    supportedLanguages?: Array<string>;
    upload?: ConfigUpload;
    userAffiliation?: boolean;
    userDisplayName?: string;
    userMenuOverrides?: Array<ContextMenuEntry>;
    userSecondaryDisplayName?: string;
    whatsNewUrl?: string;
    workflow?: ConfigWorkflow;
    workspaceColumns?: Array<string>;
    workspaceSharedToMeDefaultAll?: boolean;
    workspaceViewType?: number;
}