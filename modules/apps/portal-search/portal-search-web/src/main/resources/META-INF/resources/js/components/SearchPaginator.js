/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {Option, Picker} from '@clayui/core';
import ClayIcon from '@clayui/icon';
import {ClayPaginationWithBasicItems} from '@clayui/pagination';
import {PaginationBar} from '@clayui/pagination-bar';
import {useId} from '@clayui/shared';
import {sub} from 'frontend-js-web';
import React from 'react';

const ELLIPSIS_BUFFER = 2;

const Trigger = React.forwardRef(({activeDelta, label, ...otherProps}, ref) => (
	<ClayButton
		{...otherProps}
		className="dropdown-toggle"
		displayType="unstyled"
		ref={ref}
	>
		{sub(label, [activeDelta])}

		<ClayIcon symbol="caret-double-l" />
	</ClayButton>
));

Trigger.displayName = 'Trigger';

/**
 * Link based paginator for the Search Results widget.
 *
 * This is a rewiring of the existing `liferay-ui:search-paginator` markup
 * rather than a new component: the server still owns the URLs, and every page
 * and every items per page option remains an ordinary link. Clay is handed an
 * `hrefConstructor`, so navigating a page stays a plain request exactly as it
 * was with the JSP. Nothing here fetches data or holds pagination state, which
 * is why both the active page and the active delta are seeded as uncontrolled.
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
}) => {

	// Clay's own `useId` rather than React's, which only exists from React 18.
	// Clay uses it for the same reason, and it keeps the paginator renderable
	// wherever Clay itself is, including the Storybook server.

	const resultsId = useId();

	const numberFormat = new Intl.NumberFormat(
		Liferay.ThemeDisplay.getBCP47LanguageId()
	);

	const hrefConstructor = (page) =>
		`${paginationURL}${encodeURIComponent(curParam)}=${page}${urlAnchor}`;

	// `totalItems` stays an exact number so the page count and the upper bound
	// below remain arithmetic. When the search only counted up to its accurate
	// count limit, that number is a floor rather than a true total, so it is
	// the rendered label that gains the "or more" marker, not the value.

	const totalItemsLabel = totalItemsApproximate
		? sub(labels.approximateTotalItems, [numberFormat.format(totalItems)])
		: numberFormat.format(totalItems);

	// The active page is controlled by the server and never by Clay. Left
	// uncontrolled, clicking a page marks that item active while the click is
	// still being handled, and `Pagination.Item` renders no `href` for the
	// active item, so the anchor loses its target before the browser follows
	// it and the click does nothing at all. Holding `active` fixed keeps every
	// item a real link, which is the whole point of a link based paginator.
	// Clay warns when `active` arrives without a handler, and there is genuinely
	// nothing to handle: the next active page comes back with the next render.

	const ignoreActiveChange = () => {};

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
						{(item) => (
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

			<ClayPaginationWithBasicItems
				active={activePage}
				aria-label={labels.pagination}
				ariaLabels={{
					link: labels.page,
					next: labels.nextPage,
					previous: labels.previousPage,
				}}
				ellipsisBuffer={ELLIPSIS_BUFFER}
				ellipsisProps={{
					'aria-label': labels.intermediatePages,
					'title': labels.intermediatePages,
				}}
				hrefConstructor={hrefConstructor}
				onActiveChange={ignoreActiveChange}
				totalPages={Math.ceil(totalItems / activeDelta)}
			/>
		</PaginationBar>
	);
};

export default SearchPaginator;
