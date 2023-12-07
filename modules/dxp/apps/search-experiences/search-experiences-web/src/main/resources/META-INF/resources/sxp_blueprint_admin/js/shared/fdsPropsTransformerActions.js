/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fetch} from 'frontend-js-web';

import {DEFAULT_ERROR} from '../utils/errorMessages';
import {openErrorToast, openSuccessToast} from '../utils/toasts';

export function copy(url, parameters, loadData) {
	fetch(url, parameters)
		.then((response) => {
			return response.json().then((data) => ({
				ok: response.ok,
				responseContent: data,
			}));
		})
		.then(({ok, responseContent}) => {
			if (!ok) {
				openErrorToast({
					message: responseContent.title || DEFAULT_ERROR,
				});
			}
			else {
				loadData();

				openSuccessToast();
			}
		})
		.catch(() => {
			openErrorToast();
		});
}

export function download(url, parameters, title) {
	fetch(url, parameters)
		.then((response) => {
			if (!response.ok) {
				throw DEFAULT_ERROR;
			}

			return response.blob();
		})
		.then((responseBlob) => {
			const downloadElement = document.createElement('a');

			downloadElement.download = title + '.json';
			downloadElement.href = URL.createObjectURL(responseBlob);

			document.body.appendChild(downloadElement);

			downloadElement.click();

			openSuccessToast();
		})
		.catch(() => {
			openErrorToast();
		});
}
