/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import ClayButton from '@clayui/button';
import {Align, ClayDropDownWithItems} from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import ClayPagination from '@clayui/pagination';
import React from 'react';

const ELLIPSIS_BUFFER = 2;

const PaginationEllipsis = ({
	disabledPages = [],
	hrefConstructor,
	items = [],
	onPageChange,
}) => {
	const pages = items.map((page) => ({
		disabled: disabledPages.includes(page),
		href: hrefConstructor ? hrefConstructor(page) : undefined,
		label: String(page),
		onClick: () => onPageChange && onPageChange(page),
	}));

	return (
		<ClayDropDownWithItems
			alignmentPosition={Align.TopCenter}
			className="page-item"
			containerElement="li"
			items={pages}
			trigger={
				<ClayButton className="page-link" displayType="unstyled">
					...
				</ClayButton>
			}
		/>
	);
};

const getBufferList = (start, end, config) => {
	const {EllipsisComponent, ellipsisProps, items} = config;

	const removedItems = items.slice(start, Math.max(end, start));

	if (removedItems.length > 1) {
		return [
			<EllipsisComponent
				items={removedItems}
				key="ellipsisComponent"
				{...ellipsisProps}
			/>,
		];
	}

	return removedItems;
};

export const getEllipsisItems = (config, ellipsisBuffer, activeIndex = 0) => {
	const {items} = config;

	const lastIndex = items.length - 1;

	const leftBufferEnd = activeIndex - ellipsisBuffer;

	const rightBufferStart = activeIndex + ellipsisBuffer + 1;

	const leftBuffer = getBufferList(1, leftBufferEnd, config);
	const rightBuffer = getBufferList(rightBufferStart, lastIndex, config);

	const newArray = [
		items[0],
		...leftBuffer,
		...items.slice(
			Math.max(activeIndex - ellipsisBuffer, 1),

			// Add 1 to account for active index

			Math.min(activeIndex + ellipsisBuffer + 1, lastIndex)
		),
		...rightBuffer,
	];

	if (items.length > 1) {
		newArray.push(items[lastIndex]);
	}

	return newArray;
};

function PaginationWithBasicItems({
	activePage,
	ariaLabels = {
		next: Liferay.Language.get('next'),
		previous: Liferay.Language.get('previous'),
	},
	disabledPages = [],
	ellipsisBuffer = ELLIPSIS_BUFFER,
	hrefConstructor,
	onPageChange,
	totalPages,
	...otherProps
}) {
	const previousPage = activePage - 1;
	const previousHref = hrefConstructor && hrefConstructor(previousPage);

	const nextPage = activePage + 1;
	const nextHref = hrefConstructor && hrefConstructor(nextPage);

	const pages = Array(totalPages)
		.fill(0)
		.map((item, index) => index + 1);

	return (
		<ClayPagination {...otherProps}>
			<ClayPagination.Item
				aria-label={ariaLabels.previous}
				data-testid="prevArrow"
				disabled={activePage === 1}
				href={previousHref}
				onClick={() => onPageChange && onPageChange(previousPage)}
			>
				<ClayIcon symbol="angle-left" />
			</ClayPagination.Item>

			{(ellipsisBuffer
				? getEllipsisItems(
						{
							EllipsisComponent: PaginationEllipsis,
							ellipsisProps: {
								disabledPages,
								hrefConstructor,
								onPageChange,
							},
							items: pages,
						},
						ellipsisBuffer,
						activePage - 1
				  )
				: pages
			).map((page, index) =>
				React.isValidElement(page) ? (
					React.cloneElement(page, {key: `ellipsis${index}`})
				) : (
					<ClayPagination.Item
						active={page === activePage}
						disabled={disabledPages.includes(page)}
						href={hrefConstructor && hrefConstructor(page)}
						key={page}
						onClick={() => onPageChange && onPageChange(page)}
					>
						{page}
					</ClayPagination.Item>
				)
			)}

			<ClayPagination.Item
				aria-label={ariaLabels.next}
				data-testid="nextArrow"
				disabled={activePage === totalPages}
				href={nextHref}
				onClick={() => onPageChange && onPageChange(nextPage)}
			>
				<ClayIcon symbol="angle-right" />
			</ClayPagination.Item>
		</ClayPagination>
	);
}

export default PaginationWithBasicItems;
