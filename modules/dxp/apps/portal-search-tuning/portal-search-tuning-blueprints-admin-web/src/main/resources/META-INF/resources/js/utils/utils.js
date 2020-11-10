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
import moment from 'moment';

export const openErrorToast = (config) => {
	openToast({
		message: Liferay.Language.get('an-unexpected-error-occurred'),
		title: Liferay.Language.get('error'),
		type: 'danger',
		...config,
	});
};

/**
 * Function for retrieving a valid default value from one fragment
 * configuration entry.
 *
 * Examples:
 * getDefaultValue({
		defaultValue: 10,
		key: 'config.title.boost',
		name: 'Title Boost',
		type: 'slider',
	})
 * => 10
 * getDefaultValue({
		key: 'config.lfr.enabled',
		name: 'Enabled',
		type: 'single-select',
		typeOptions: [
			{
				label: 'True',
				value: true,
			},
			{
				label: 'False',
				value: false,
			},
		],
	})
 * => true
 *
 * @param {object} item Configuration with key, name, type, defaultValue
 * @return {(string|Array|number)}
 */
export const getDefaultValue = (item) => {
	const itemValue = item.defaultValue;

	switch (item.type) {
		case 'single-select':
			return isNotNull(itemValue)
				? itemValue
				: item.typeOptions && item.typeOptions[0].value
				? item.typeOptions[0].value
				: '';
		case 'date':
			return isNotNull(itemValue)
				? typeof itemValue == 'number'
					? itemValue
					: moment(itemValue).isValid()
					? moment(itemValue).unix()
					: ''
				: '';
		case 'entity':
			return isNotNull(itemValue) &&
				itemValue.length > 0 &&
				itemValue.every(
					(item) => isNotNull(item.id) && isNotNull(item.name)
				)
				? itemValue
				: [];
		case 'number':
			return isNotNull(itemValue) && typeof itemValue == 'number'
				? itemValue
				: '';
		case 'slider':
			return isNotNull(itemValue) && typeof itemValue == 'number'
				? itemValue
				: '';
		default:
			return isNotNull(itemValue) ? itemValue : '';
	}
};

/**
 * Function for getting all the default values from a UI configuration.
 *
 * Example:
 * getConfigValues({
		configurationValues: [
			{
				defaultValue: 10,
				key: 'config.title.boost',
				name: 'Title Boost',
				type: 'slider',
			},
			{
				defaultValue: 'en_US',
				key: 'context.language_id',
				name: 'Context Language',
				type: 'text',
			}
		],
	})
 * => {context.title.boost: 10, context.language_id: 'en_US'}
 *
 * @param {object} configJSON Object with UI configuration
 * @return {object}
 */
export const getConfigValues = (configJSON) => {
	if (!!configJSON && configJSON.configurationValues) {
		return configJSON.configurationValues.reduce((acc, curr) => {
			return {
				...acc,
				[`${curr.key}`]: getDefaultValue(curr),
			};
		}, {});
	}

	return {};
};

/**
 * Function for replacing the ${variable_name} with actual value.
 *
 * @param {object} configJSON Object with UI configuration
 * @param {object} inputJSON Actual fragment template for blueprint configuration
 * @return {object}
 */
export const replaceConfigValues = (
	configJSON,
	inputJSON,
	configValues = getConfigValues(configJSON)
) => {
	if (!!configJSON && configJSON.configurationValues) {
		let flattenJSON = JSON.stringify(inputJSON);

		configJSON.configurationValues.map((config) => {
			const configValue =
				config.type === 'entity'
					? JSON.stringify(
							configValues[config.key].map((item) => item.id)
					  )
					: configValues[config.key];

			if (
				typeof configValue === 'number' ||
				typeof configValue === 'boolean' ||
				config.type === 'entity'
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
		});

		return JSON.parse(flattenJSON);
	}

	return inputJSON;
};

/**
 * Function to replace all instances of a string.
 *
 * Example:
 * replaceStr('title_${context.language}','${context.language}','en_US')
 * => 'title_en_US'
 *
 * @param {String} str Original string
 * @param {String} search Snippet to look for
 * @param {String} replace Snippet to replace with
 * @return {String}
 */
export const replaceStr = (str, search, replace) => {
	return str.split(search).join(replace);
};

/**
 * Function to package the initial data into a state that the blueprints
 * form will use, by including the id, configuration values, and
 * fragment for submission.
 *
 * @param {object} {configJSON, inputJSON} Object with UI configuration
 * and fragment template
 * @param {number} id ID number of fragment
 * @return {object}
 */
export const convertToSelectedFragment = ({configJSON, inputJSON}, id = 0) => {
	return {
		configJSON,
		configValues: getConfigValues(configJSON),
		id,
		inputJSON,
		queryConfig: replaceConfigValues(configJSON, inputJSON),
	};
};

/**
 * Function to validate the UI configuration, used to identify whether
 * user is missing a required value.
 *
 * Examples:
 * validateConfigJSON({
		defaultValue: 10,
		name: 'Title Boost',
		type: 'slider'
	})
 * => false
 * validateConfigJSON({
		defaultValue: 3,
		key: 'context.timespan',
		name: 'Time Span',
		type: 'number',
		unit: 'days'
	}
 * => true
 *
 * @param {object} configJSON Object with UI configuration
 * @return {boolean}
 */
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
		}
		else {
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

/**
 * Function to validate the UI configuration, used to identify whether
 * a required value is missing
 *
 * Examples:
 * isNotNull([])
 * => true
 * isNotNull('')
 * => false
 * isNotNull(null)
 * => false
 *
 * @param {String|object|Array} item Item to check existence
 * @return {boolean}
 */
const isNotNull = (item) =>
	item !== null && item !== '' && typeof item !== 'undefined';
