/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {Option, Picker} from '@clayui/core';
import ClayIcon from '@clayui/icon';
import {ClayPaginationWithBasicItems, Pagination} from '@clayui/pagination';
import {PaginationBar} from '@clayui/pagination-bar';
import {useId} from '@clayui/shared';
import {sub} from 'frontend-js-web';
import React from 'react';

const ELLIPSIS_BUFFER = 2;

interface IAriaLabels {

	/**
	 * Label for a page link, taking the page number.
	 */
	link: string;

	next: string;
	previous: string;
}

interface IDelta {

	/**
	 * URL that switches the results to this page size.
	 */
	href: string;

	label: number;
}

interface ILabels {

	/**
	 * Wraps a total that is a floor rather than an exact figure, taking the
	 * formatted total.
	 */
	approximateTotalItems: string;

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

	/**
	 * A page size option, taking the size.
	 */
	selectPerPageItems: string;
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

interface ILimitedPaginationProps {
	activePage: number;
	ariaLabels: IAriaLabels;
	hrefConstructor: (page: number) => string;
	label: string;

	/**
	 * The last page the total accounts for. Because the total is a floor, pages
	 * may exist beyond it, so this only bounds which pages can be linked.
	 */
	lastKnownPage: number;

	visiblePageCount: number;
}

/**
 * Pagination restricted to a sliding window of pages.
 *
 * Used when the total is only known up to the accurate count limit, which
 * leaves the real last page unknown. Nothing here can link to an end page or
 * enumerate the pages behind an ellipsis, so it renders a window around the
 * active page and lets the next arrow be the only sign that there is more to
 * come. The window slides to keep the active page in the middle and clamps at
 * the start, so the first pages read `1 2 3 4 5` rather than a stub.
 */
const LimitedPagination = ({
	activePage,
	ariaLabels,
	hrefConstructor,
	label,
	lastKnownPage,
	visiblePageCount,
}: ILimitedPaginationProps) => {
	const firstVisiblePage = Math.max(
		1,
		activePage - Math.floor(visiblePageCount / 2)
	);

	const lastVisiblePage = Math.min(
		lastKnownPage,
		firstVisiblePage + visiblePageCount - 1
	);

	const visiblePages = [];

	for (let page = firstVisiblePage; page <= lastVisiblePage; page++) {
		visiblePages.push(page);
	}

	const hasPreviousPage = activePage > 1;

	return (
		<Pagination aria-label={label}>
			<Pagination.Item
				aria-label={
					hasPreviousPage
						? sub(ariaLabels.previous, [activePage - 1])
						: undefined
				}
				as={hasPreviousPage ? undefined : 'div'}
				disabled={!hasPreviousPage}
				href={
					hasPreviousPage
						? hrefConstructor(activePage - 1)
						: undefined
				}
			>
				<ClayIcon symbol="angle-left" />
			</Pagination.Item>

			{visiblePages.map((page) => (
				<Pagination.Item
					active={page === activePage}
					aria-label={sub(ariaLabels.link, [page])}
					href={hrefConstructor(page)}
					key={page}
				>
					{page}
				</Pagination.Item>
			))}

			{/*
			 * Paging forward is never disabled here. The total this window is
			 * built from is a floor, so there is no page at which we can say
			 * nothing follows; reaching the real end is something only the
			 * next response can reveal.
			 */}

			<Pagination.Item
				aria-label={sub(ariaLabels.next, [activePage + 1])}
				href={hrefConstructor(activePage + 1)}
			>
				<ClayIcon symbol="angle-right" />
			</Pagination.Item>
		</Pagination>
	);
};

interface IProps {
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
	 * count limit. An approximate total leaves the real last page unknown, so
	 * it is also what decides that the pages render as a window.
	 */
	totalItemsApproximate?: boolean;

	urlAnchor?: string;

	/**
	 * How many pages the window holds. Only consulted when the total is
	 * approximate, since an exact total renders every page it accounts for.
	 */
	visiblePageCount?: number;
}

/**
 * Link based paginator for the Search Results widget.
 *
 * This is a rewiring of the existing `liferay-ui:search-paginator` markup
 * rather than a new component: the server still owns the URLs, and every page
 * and every items per page option remains an ordinary link. Clay is handed an
 * `hrefConstructor`, so navigating a page stays a plain request exactly as it
 * was with the JSP. Nothing here fetches data or holds pagination state.
 *
 * The pieces are composed by hand rather than through
 * `ClayPaginationBarWithBasicItems`, which renders the same markup but forwards
 * neither `aria-label` nor `ariaLabels` to the pagination inside it, and
 * formats the result counts as bare JavaScript numbers. Composing keeps every
 * label translated and every count formatted for the reader's locale, the way
 * the JSP did.
 */
const SearchPaginator = ({
	activeDelta,
	activePage,
	curParam,
	deltas,
	labels,
	paginationURL,
	showDeltasDropDown = true,
	totalItems,
	totalItemsApproximate = false,
	urlAnchor = '',
	visiblePageCount = 5,
}: IProps) => {

	// Clay's own `useId` rather than React's, which only exists from React 18.
	// Clay uses it for the same reason, and it keeps the paginator renderable
	// wherever Clay itself is, including the Storybook server.

	const resultsId = useId();

	const numberFormat = new Intl.NumberFormat(
		Liferay.ThemeDisplay.getBCP47LanguageId()
	);

	// The page is optional only to match the signature Clay declares for
	// `hrefConstructor`. Every caller, Clay's included, passes one.

	const hrefConstructor = (page?: number) =>
		`${paginationURL}${encodeURIComponent(curParam)}=${page}${urlAnchor}`;

	// `totalItems` stays an exact number so the page count and the upper bound
	// below remain arithmetic. When the search only counted up to its accurate
	// count limit, that number is a floor rather than a true total, so it is
	// the rendered label that gains the "or more" marker, not the value.

	const totalItemsLabel = totalItemsApproximate
		? sub(labels.approximateTotalItems, [numberFormat.format(totalItems)])
		: numberFormat.format(totalItems);

	const lastKnownPage = Math.ceil(totalItems / activeDelta);

	// The active page is controlled by the server and never by Clay. Left
	// uncontrolled, clicking a page marks that item active while the click is
	// still being handled, and `Pagination.Item` renders no `href` for the
	// active item, so the anchor loses its target before the browser follows
	// it and the click does nothing at all. Holding `active` fixed keeps every
	// item a real link, which is the whole point of a link based paginator.
	// Clay warns when `active` arrives without a handler, and there is genuinely
	// nothing to handle: the next active page comes back with the next render.

	const ignoreActiveChange = () => {};

	const ariaLabels = {
		link: labels.page,
		next: labels.nextPage,
		previous: labels.previousPage,
	};

	return (
		<PaginationBar>
			{showDeltasDropDown && (
				<div className="dropdown pagination-items-per-page">
					<Picker
						activeDelta={activeDelta}
						aria-describedby={resultsId}
						aria-label={labels.itemsPerPagePicker}
						as={Trigger}
						defaultSelectedKey={String(activeDelta)}
						items={deltas}
						label={labels.perPageItems}
					>
						{(item: IDelta) => (
							<Option href={item.href} key={item.label}>
								{sub(labels.selectPerPageItems, [item.label])}
							</Option>
						)}
					</Picker>
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

			{totalItemsApproximate ? (
				<LimitedPagination
					activePage={activePage}
					ariaLabels={ariaLabels}
					hrefConstructor={hrefConstructor}
					label={labels.pagination}
					lastKnownPage={lastKnownPage}
					visiblePageCount={visiblePageCount}
				/>
			) : (
				<ClayPaginationWithBasicItems
					active={activePage}
					aria-label={labels.pagination}
					ariaLabels={ariaLabels}
					ellipsisBuffer={ELLIPSIS_BUFFER}
					ellipsisProps={{
						'aria-label': labels.intermediatePages,
						'title': labels.intermediatePages,
					}}
					hrefConstructor={hrefConstructor}
					onActiveChange={ignoreActiveChange}
					totalPages={lastKnownPage}
				/>
			)}
		</PaginationBar>
	);
};

export default SearchPaginator;
