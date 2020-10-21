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

import {getUnixTime, isValid} from 'date-fns';
import {openToast} from 'frontend-js-web';

export const openErrorToast = (config) => {
	openToast({
		message: Liferay.Language.get('an-unexpected-error-occurred'),
		title: Liferay.Language.get('error'),
		type: 'danger',
		...config,
	});
};

export const getDefaultValue = (item) => {
	switch (item.type) {
		case 'single-select':
			return isNotNull(item.defaultValue)
				? item.defaultValue
				: item.typeOptions && item.typeOptions[0].value
				? item.typeOptions[0].value
				: '';
		case 'date':
			return isNotNull(item.defaultValue)
				? typeof item.defaultValue == 'number'
					? item.defaultValue
					: isValid(new Date(item.defaultValue))
					? getUnixTime(new Date(item.defaultValue))
					: ''
				: '';
		default:
			return isNotNull(item.defaultValue) ? item.defaultValue : '';
	}
};

export const getConfigValues = (configJSON) => {
	if (!!configJSON && configJSON.configurationValues) {
		return configJSON.configurationValues.reduce((acc, curr) => {
			return {
				...acc,
				[`${curr.key}`]: {
					value: getDefaultValue(curr),
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
			const configValue = configValues[config.key].value;

			if (isNotNull(configValue)) {
				if (
					typeof configValue === 'number' ||
					typeof configValue === 'boolean' ||
					config.type === 'number'
				) {
					flattenJSON = replaceStr(
						flattenJSON,
						`"$\{${config.key}}"`,
						configValue
					);
				}
				flattenJSON = replaceStr(
					flattenJSON,
					`\${${config.key}}`,
					configValue
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

export const validateConfigJSON = (configJSON) => {
	return configJSON.configurationValues.every((item) => {
		if (
			isNotNull(item.type) &&
			isNotNull(item.key) &&
			isNotNull(item.name)
		) {
			switch (item.type) {
				case 'single-select':
					return (
						!!item.typeOptions &&
						item.typeOptions.length > 0 &&
						item.typeOptions.every(
							(option) =>
								isNotNull(option.label) &&
								isNotNull(option.value)
						)
					);
				case 'entity':
					return (
						item.className && entityKeys.includes(item.className)
					);
				default:
					return true;
			}
		} else {
			return false;
		}
	});
};

const entityKeys = [
	'com.liferay.asset.kernel.model.AssetTag',
	'com.liferay.portal.kernel.model.Group',
	'com.liferay.portal.kernel.model.Organization',
	'com.liferay.portal.kernel.model.Role',
	'com.liferay.portal.kernel.model.Team',
	'com.liferay.portal.kernel.model.User',
	'com.liferay.portal.kernel.model.UserGroup',
];

const isNotNull = (item) =>
	item !== 'null' && item !== '' && item !== undefined;
