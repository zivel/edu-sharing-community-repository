import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { CardDialogRef } from '../../card-dialog/card-dialog-ref';
import { DialogButton } from '../../../../core-module/ui/dialog-button';
import { NotificationConfig, NotificationV1Service } from 'ngx-edu-sharing-api';
import { FormControl, FormGroup } from '@angular/forms';
import { Toast } from '../../../../core-ui-module/toast';

enum NotificationEvents {
    addToCollectionEvent = 'addToCollectionEvent',
    commentEvent = 'commentEvent',
    inviteEvent = 'inviteEvent',
    nodeIssueEvent = 'nodeIssueEvent',
    ratingEvent = 'ratingEvent',
    workflowEvent = 'workflowEvent',
    metadataSuggestionEvent = 'metadataSuggestionEvent',
}

@Component({
    selector: 'es-notification-dialog',
    templateUrl: './notification-dialog.component.html',
    styleUrls: ['./notification-dialog.component.scss'],
})
export class NotificationDialogComponent implements OnInit, OnDestroy {
    readonly destroyed$ = new Subject<void>();
    private config: NotificationConfig;

    notificationForm = new FormGroup({
        configMode: new FormControl(),
        defaultInterval: new FormControl(),
        intervals: new FormGroup({}),
    });

    constructor(
        private dialogRef: CardDialogRef<void, void>,
        private toast: Toast,
        private notificationService: NotificationV1Service,
    ) {}

    getGroups() {
        const result = [];
        for (let v in NotificationEvents) {
            // skip unused events
            if (
                ![
                    NotificationEvents.nodeIssueEvent.valueOf(),
                    NotificationEvents.metadataSuggestionEvent.valueOf(),
                ].includes(v)
            ) {
                result.push(v);
            }
        }
        return result;
    }

    async ngOnInit() {
        for (let v in NotificationEvents) {
            (this.notificationForm.get('intervals') as FormGroup).addControl(v, new FormControl());
        }
        this.dialogRef.patchConfig({
            buttons: [
                new DialogButton('CANCEL', { color: 'standard' }, () => this.dialogRef.close()),
                new DialogButton('SAVE', { color: 'primary' }, () => this.save()),
            ],
        });
        this.dialogRef.patchState({
            isLoading: true,
        });
        this.config = await this.notificationService.getConfig2().toPromise();
        this.notificationForm.setValue(this.config);
        this.dialogRef.patchState({
            isLoading: false,
        });
    }

    ngOnDestroy() {
        this.destroyed$.next();
        this.destroyed$.complete();
    }

    private async save() {
        this.dialogRef.patchState({
            isLoading: true,
        });

        await this.notificationService
            .setConfig1({
                body: this.notificationForm.value,
            })
            .toPromise();
        this.dialogRef.close();
        this.toast.toast('NOTIFICATION.SAVED');
    }
}