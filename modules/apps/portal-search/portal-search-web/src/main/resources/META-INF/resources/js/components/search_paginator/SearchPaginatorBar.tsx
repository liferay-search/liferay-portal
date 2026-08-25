/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {Option, Picker} from '@clayui/core';
import ClayIcon from '@clayui/icon';
import {PaginationBar} from '@clayui/pagination-bar';
import {useId} from '@clayui/shared';
import {sub} from 'frontend-js-web';
import React from 'react';

export interface IAriaLabels {

	/**
	 * Label for a page link, taking the page number.
	 */
	link: string;

	next: string;
	previous: string;
}

export interface IDelta {

	/**
	 * URL that switches the results to this page size.
	 */
	href: string;

	label: number;
}

export interface ILabels {

	/**
	 * Wraps a total that is a floor rather than an exact figure, taking the
	 * formatted total.
	 */
	approximateTotalItems: string;

	/**
	 * Warns that choosing a page size navigates. Every option is a link, so the
	 * page really does reload, and a reader who cannot see that deserves to be
	 * told before choosing.
	 */
	changingPageSizeReloads: string;

	/**
	 * Names what a page size option counts, for readers who cannot see that the
	 * option sits in an items per page picker.
	 */
	entriesPerPage: string;

	intermediatePages: string;
	itemsPerPagePicker: string;
	nextPage: string;

	/**
	 * Label for a page link, taking the page number.
	 */
	page: string;

	pagination: string;

	/**
	 * Result summary, taking the first item, the last item and the total.
	 */
	paginationResults: string;

	/**
	 * The active page size, taking the size.
	 */
	perPageItems: string;

	previousPage: string;
}

/**
 * The props every search paginator takes, all of them supplied by
 * <code>SearchResultsPaginatorReactDataBuilder</code>.
 */
export interface ISearchPaginatorProps {
	activeDelta: number;
	activePage: number;

	/**
	 * Name of the parameter carrying the page number.
	 */
	curParam: string;

	deltas: Array<IDelta>;
	labels: ILabels;

	/**
	 * URL the page links are built from, already ending in a parameter
	 * separator and stripped of <code>curParam</code>.
	 */
	paginationURL: string;

	showDeltasDropDown?: boolean;
	totalItems: number;

	/**
	 * Whether <code>totalItems</code> is a floor rather than an exact figure,
	 * which is what the search returns once the results outgrow its accurate
	 * count limit.
	 */
	totalItemsApproximate?: boolean;

	urlAnchor?: string;
}

const Trigger = React.forwardRef<HTMLButtonElement>(
	({activeDelta, label, ...otherProps}: Record<string, any>, ref) => (
		<ClayButton
			{...otherProps}
			className="dropdown-toggle"
			displayType="unstyled"
			ref={ref}
		>
			{sub(label, [activeDelta])}

			<ClayIcon symbol="caret-double-l" />
		</ClayButton>
	)
);

Trigger.displayName = 'Trigger';

/**
 * Builds the link to a page. The server owns the URL, so this only appends the
 * page to what it already prepared.
 *
 * The page is optional only to match the signature Clay declares for
 * <code>hrefConstructor</code>. Every caller, Clay's included, passes one.
 */
export function createHrefConstructor({
	curParam,
	paginationURL,
	urlAnchor = '',
}: Pick<ISearchPaginatorProps, 'curParam' | 'paginationURL' | 'urlAnchor'>) {
	return (page?: number) =>
		`${paginationURL}${encodeURIComponent(curParam)}=${page}${urlAnchor}`;
}

export function toAriaLabels(labels: ILabels): IAriaLabels {
	return {
		link: labels.page,
		next: labels.nextPage,
		previous: labels.previousPage,
	};
}

interface IProps extends ISearchPaginatorProps {

	/**
	 * The pagination itself, which is all that separates one paginator from
	 * another.
	 */
	children: React.ReactNode;
}

/**
 * The frame both search paginators share: the items per page picker, the
 * result summary, and whichever pagination the caller puts inside it.
 *
 * Everything here is link based. The server owns the URLs, so each items per
 * page option is an ordinary link and nothing fetches data or holds state.
 */
const SearchPaginatorBar = ({
	activeDelta,
	activePage,
	children,
	deltas,
	labels,
	showDeltasDropDown = true,
	totalItems,
	totalItemsApproximate = false,
}: IProps) => {

	// Clay's own `useId` rather than React's, which only exists from React 18.

	const resultsId = useId();
	const reloadsId = useId();

	const numberFormat = new Intl.NumberFormat(
		Liferay.ThemeDisplay.getBCP47LanguageId()
	);

	// `totalItems` stays an exact number so the page count and the upper bound
	// below remain arithmetic. When the search only counted up to its accurate
	// count limit, that number is a floor rather than a true total, so it is
	// the rendered label that gains the "or more" marker, not the value.

	const totalItemsLabel = totalItemsApproximate
		? sub(labels.approximateTotalItems, [numberFormat.format(totalItems)])
		: numberFormat.format(totalItems);

	return (
		<PaginationBar>
			{showDeltasDropDown && (
				<div className="dropdown pagination-items-per-page">
					<Picker
						activeDelta={activeDelta}
						aria-describedby={`${resultsId} ${reloadsId}`}
						aria-label={labels.itemsPerPagePicker}
						as={Trigger}
						defaultSelectedKey={String(activeDelta)}
						items={deltas}
						label={labels.perPageItems}
					>
						{(item: IDelta) => (
							<Option
								href={item.href}
								key={item.label}
								textValue={`${item.label}\u00a0${labels.entriesPerPage}`}
							>
								{item.label}

								<span className="sr-only">
									{'\u00a0'}

									{labels.entriesPerPage}
								</span>
							</Option>
						)}
					</Picker>

					<span className="sr-only" id={reloadsId}>
						{labels.changingPageSizeReloads}
					</span>
				</div>
			)}

			<PaginationBar.Results id={resultsId}>
				{sub(labels.paginationResults, [
					numberFormat.format((activePage - 1) * activeDelta + 1),
					numberFormat.format(
						Math.min(activePage * activeDelta, totalItems)
					),
					totalItemsLabel,
				])}
			</PaginationBar.Results>

			{children}
		</PaginationBar>
	);
};

export default SearchPaginatorBar;
