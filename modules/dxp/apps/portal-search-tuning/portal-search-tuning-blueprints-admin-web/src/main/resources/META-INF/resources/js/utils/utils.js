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

import {openToast} from 'frontend-js-web';

export const openErrorToast = (config) => {
	openToast({
		message: Liferay.Language.get('an-unexpected-error-occurred'),
		title: Liferay.Language.get('error'),
		type: 'danger',
		...config,
	});
};

export const getConfigValues = (configJSON) => {
	if (!!configJSON && configJSON.configurationValues) {
		return configJSON.configurationValues.reduce((acc, curr) => {
			return {
				...acc,
				[`${curr.key}`]: {
					value: !curr.defaultValue
						? curr.typeOptions
							? curr.typeOptions[0].value
							: ''
						: curr.defaultValue,
				},
			};
		}, {});
	}

	return {};
};

export const replaceConfigValues = (
	configJSON,
	inputJSON,
	configValues = getConfigValues(configJSON)
) => {
	if (!!configJSON && configJSON.configurationValues) {
		let flattenJSON = JSON.stringify(inputJSON);

		configJSON.configurationValues.map((config) => {
			if (configValues[config.key].value) {
				if (
					config.type === 'slider' ||
					config.type === 'number' ||
					config.type === 'date'
				) {
					flattenJSON = replaceStr(
						flattenJSON,
						`"$\{${config.key}}"`,
						configValues[config.key].value
					);
				}
				flattenJSON = replaceStr(
					flattenJSON,
					`\${${config.key}}`,
					configValues[config.key].value
				);
			}
		});

		return JSON.parse(flattenJSON);
	}

	return inputJSON;
};

export const replaceStr = (str, search, replace) => {
	return str.split(search).join(replace);
};

export const convertSelectedFragment = ({configJSON, inputJSON}, id = 0) => {
	return {
		configJSON,
		configValues: getConfigValues(configJSON),
		id,
		inputJSON,
		queryConfig: replaceConfigValues(configJSON, inputJSON),
	};
};
