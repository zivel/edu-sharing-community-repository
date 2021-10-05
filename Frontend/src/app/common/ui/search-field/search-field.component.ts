import { CdkConnectedOverlay, ConnectedPosition } from '@angular/cdk/overlay';
import {
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { FacetsDict, LabeledValue } from 'edu-sharing-api';
import { Subject } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { SearchFieldService } from './search-field.service';

@Component({
    selector: 'app-search-field',
    templateUrl: './search-field.component.html',
    styleUrls: ['./search-field.component.scss'],
})
export class SearchFieldComponent implements OnInit, OnDestroy {
    @Input() set searchString(s: string) {
        this.inputControl.setValue(s);
    }
    @Output() searchStringChange = new EventEmitter<string>();
    @Input() placeholder: string;
    /**
     * If enabled, shows filters as chips inside the search field and suggests additional filters in
     * an overlay as the user types into the search field.
     *
     * Relies on active filters values being provided to `SearchFieldService` via `setFilterValues`
     * and `filterValuesChange` being handled.
     */
    @Input() enableFiltersAndSuggestions: boolean;
    @Output() search = new EventEmitter<string>();
    @Output() clear = new EventEmitter<void>();

    @ViewChild('input') input: ElementRef;
    @ViewChild(CdkConnectedOverlay) private overlay: CdkConnectedOverlay;
    @ViewChild('suggestionChip', { read: ElementRef })
    private firstSuggestionChip: ElementRef<HTMLElement>;

    readonly inputControl = new FormControl('');
    readonly filters$ = this.searchField.filters$;
    readonly categories$ = this.searchField.categories$;
    readonly suggestions$ = this.searchField.suggestions$;
    showOverlay = false;
    hasSuggestions = false;
    readonly overlayPositions: ConnectedPosition[] = [
        {
            originX: 'center',
            originY: 'bottom',
            offsetX: 0,
            offsetY: 4,
            overlayX: 'center',
            overlayY: 'top',
        },
    ];

    private readonly destroyed$ = new Subject<void>();

    constructor(private searchField: SearchFieldService) {}

    ngOnInit(): void {
        this.searchField.setEnableFiltersAndSuggestions(this.enableFiltersAndSuggestions);
        this.searchField.updateSuggestions(this.inputControl.value);
        this.inputControl.valueChanges.subscribe((inputString) => {
            this.searchField.updateSuggestions(inputString);
            this.searchStringChange.emit(inputString);
        });
        this.suggestions$
            .pipe(
                takeUntil(this.destroyed$),
                map((suggestions) => this.getHasSuggestions(suggestions)),
            )
            .subscribe((hasSuggestions) => (this.hasSuggestions = hasSuggestions));
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
        this.destroyed$.complete();
    }

    onSubmit(): void {
        this.showOverlay = false;
        this.search.emit(this.inputControl.value);
    }

    onClear(): void {
        this.inputControl.setValue('');
        this.clear.emit();
    }

    inputHasFocus(): boolean {
        return document.activeElement === this.input?.nativeElement;
    }

    onAddFilter(property: string, filter: LabeledValue): void {
        this.inputControl.setValue('');
        this.searchField.addFilter(property, filter);
    }

    onRemoveFilter(property: string, filter: LabeledValue): void {
        this.searchField.removeFilter(property, filter);
    }

    onOutsideClick(event: MouseEvent): void {
        const clickTarget = event.target as HTMLElement;
        if (!(this.overlay.origin.elementRef.nativeElement as HTMLElement).contains(clickTarget)) {
            this.showOverlay = false;
        }
    }

    focusOverlayIfOpen(event: Event): void {
        if (this.firstSuggestionChip) {
            this.firstSuggestionChip.nativeElement.focus();
            event.stopPropagation();
            event.preventDefault();
        }
    }

    onDetach(): void {
        const focusWasOnOverlay = this.overlay.overlayRef.overlayElement.contains(
            document.activeElement,
        );
        if (focusWasOnOverlay) {
            this.input.nativeElement.focus();
        }
        // Update `showOverlay` if the user closed the overlay by hitting Esc, but leave it if it
        // was detached because we have no suggestions right now. In the latter case, we want to
        // show the overlay again as soon as suggestions become available.
        if (this.hasSuggestions) {
            this.showOverlay = false;
        }
    }

    onInputBlur(event: FocusEvent): void {
        if (!this.overlay.overlayRef?.overlayElement.contains(event.relatedTarget as HTMLElement)) {
            this.showOverlay = false;
        }
    }

    private getHasSuggestions(suggestions: FacetsDict): boolean {
        return (
            suggestions &&
            Object.values(suggestions).some((suggestion) => suggestion.values.length > 0)
        );
    }
}
