/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export default function normalizeDateTime(value: string) {
	if (!value) {
		return value;
	}

	// The picker emits "--:--" when the time portion isn't filled. Default
	// to midnight so a date-only entry still serializes cleanly.

	const normalized = value.replace('--:--', '00:00');

	const date = new Date(normalized.replace(' ', 'T'));

	return Number.isNaN(date.getTime()) ? '' : normalized;
}
