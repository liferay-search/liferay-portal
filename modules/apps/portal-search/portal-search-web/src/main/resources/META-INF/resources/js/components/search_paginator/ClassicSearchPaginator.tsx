/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {ClayPaginationWithBasicItems} from '@clayui/pagination';
import React from 'react';

import SearchPaginatorBar, {
	ISearchPaginatorProps,
	createHrefConstructor,
	toAriaLabels,
} from './SearchPaginatorBar';

const ELLIPSIS_BUFFER = 1;

/**
 * A search paginator that shows every page the total accounts for.
 *
 * This is the shape the `liferay-ui:search-paginator` markup had: the first and
 * last pages are links, and the pages between them sit behind an ellipsis
 * dropdown. It needs an exact total, since both the last page link and the
 * ellipsis contents are derived from the page count.
 *
 * Clay's pieces are composed by hand rather than taken through
 * `ClayPaginationBarWithBasicItems`, which renders the same markup but forwards
 * neither `aria-label` nor `ariaLabels` to the pagination inside it, and formats
 * the result counts as bare JavaScript numbers. Composing keeps every label
 * translated and every count formatted for the reader's locale, the way the JSP
 * did.
 */
const ClassicSearchPaginator = (props: ISearchPaginatorProps) => {
	const {
		activeDelta,
		activePage,
		curParam,
		labels,
		paginationURL,
		totalItems,
		urlAnchor,
	} = props;

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
		<SearchPaginatorBar {...props}>
			<ClayPaginationWithBasicItems
				active={activePage}
				aria-label={labels.pagination}
				ariaLabels={toAriaLabels(labels)}
				ellipsisBuffer={ELLIPSIS_BUFFER}
				ellipsisProps={{
					'aria-label': labels.intermediatePages,
					'title': labels.intermediatePages,
				}}
				hrefConstructor={createHrefConstructor({
					curParam,
					paginationURL,
					urlAnchor,
				})}
				onActiveChange={ignoreActiveChange}
				totalPages={Math.ceil(totalItems / activeDelta)}
			/>
		</SearchPaginatorBar>
	);
};

export default ClassicSearchPaginator;
