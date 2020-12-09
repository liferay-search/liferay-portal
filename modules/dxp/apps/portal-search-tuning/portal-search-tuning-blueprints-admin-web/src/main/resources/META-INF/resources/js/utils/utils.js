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

import {INPUT_TYPES} from './inputTypes';

export const openErrorToast = (config) => {
	openToast({
		message: Liferay.Language.get('an-unexpected-error-occurred'),
		title: Liferay.Language.get('error'),
		type: 'danger',
		...config,
	});
};

export const openSuccessToast = (config) => {
	openToast({
		message: Liferay.Language.get('your-request-completed-successfully'),
		title: Liferay.Language.get('success'),
		type: 'success',
		...config,
	});
};

/**
 * Function for retrieving a valid default value from one fragment
 * configuration entry.
 *
 * Examples:
 * getDefaultValue({
 *  	defaultValue: 10,
 *  	key: 'config.title.boost',
 *  	name: 'Title Boost',
 *  	type: 'slider',
 *  })
 * => 10
 *
 * getDefaultValue({
 *  	key: 'config.lfr.enabled',
 *  	name: 'Enabled',
 *  	type: 'single-select',
 *  	typeOptions: [
 *  		{
 *  			label: 'True',
 *  			value: true,
 *  		},
 *  		{
 *  			label: 'False',
 *  			value: false,
 *  		},
 *  	],
 *  })
 * => true
 *
 * @param {object} item Configuration with key, name, type, defaultValue
 * @return {(string|Array|number)}
 */
export const getDefaultValue = (item) => {
	const itemValue = item.defaultValue;

	switch (item.type) {
		case INPUT_TYPES.SINGLE_SELECT:
			return isNotNull(itemValue)
				? itemValue
				: item.typeOptions && item.typeOptions[0].value
				? item.typeOptions[0].value
				: '';
		case INPUT_TYPES.DATE:
			return isNotNull(itemValue)
				? typeof itemValue == 'number'
					? itemValue
					: moment(itemValue).isValid()
					? moment(itemValue).unix()
					: ''
				: '';
		case INPUT_TYPES.ENTITY:
			return isNotNull(itemValue) &&
				itemValue.length > 0 &&
				itemValue.every(
					(item) => isNotNull(item.id) && isNotNull(item.name)
				)
				? itemValue
				: [];
		case INPUT_TYPES.NUMBER:
			return isNotNull(itemValue) && typeof itemValue == 'number'
				? itemValue
				: '';
		case INPUT_TYPES.SLIDER:
			return isNotNull(itemValue) && typeof itemValue == 'number'
				? itemValue
				: '';
		case INPUT_TYPES.FIELD_SELECT:
			return isNotNull(itemValue) ? itemValue : [];
		case INPUT_TYPES.JSON:
			return isNotNull(itemValue) ? itemValue : {};
		default:
			return isNotNull(itemValue) ? itemValue : '';
	}
};

/**
 * Function for getting all the default values from a UI configuration.
 *
 * Example:
 * getUIConfigurationValues(
 *  	[
 *  		{
 *  			defaultValue: 10,
 *  			key: 'config.title.boost',
 *  			name: 'Title Boost',
 *  			type: 'slider',
 *  		},
 *  		{
 *  			defaultValue: 'en_US',
 *  			key: 'context.language_id',
 *  			name: 'Context Language',
 *  			type: 'text',
 *  		}
 *  	]
 * )
 * => {context.title.boost: 10, context.language_id: 'en_US'}
 *
 * @param {object} uiConfigurationJSON Object with UI configuration
 * @return {object}
 */
export const getUIConfigurationValues = (uiConfigurationJSON) => {
	if (uiConfigurationJSON) {
		return uiConfigurationJSON.reduce((acc, curr) => {
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
 * @param {object} uiConfigurationJSON Object with UI configuration
 * @param {object} fragmentTemplateJSON Actual fragment template for blueprint configuration
 * @return {object}
 */
export const replaceUIConfigurationValues = (
	uiConfigurationJSON,
	fragmentTemplateJSON,
	uiConfigurationValues = getUIConfigurationValues(uiConfigurationJSON)
) => {
	if (uiConfigurationJSON) {
		let flattenJSON = JSON.stringify(fragmentTemplateJSON);

		uiConfigurationJSON.map((config) => {
			let configValue = uiConfigurationValues[config.key];

			if (config.type === INPUT_TYPES.ENTITY) {
				configValue = JSON.stringify(
					uiConfigurationValues[config.key].map((item) => item.id)
				);
			}
			else if (config.type === INPUT_TYPES.FIELD_SELECT) {
				configValue = JSON.stringify(
					uiConfigurationValues[config.key].map(
						(item) =>
							`${item.field}${
								item.locale == '' || item.locale.includes('$')
									? item.locale
									: '_' + item.locale
							}${
								JSON.parse(item.boost) > 1
									? '^' + item.boost
									: ''
							}`
					)
				);
			}
			else if (config.type === INPUT_TYPES.JSON) {
				configValue = JSON.stringify(uiConfigurationValues[config.key]);
			}

			if (
				typeof configValue === 'number' ||
				typeof configValue === 'boolean' ||
				config.type === INPUT_TYPES.ENTITY ||
				config.type === INPUT_TYPES.FIELD_SELECT ||
				config.type === INPUT_TYPES.JSON
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

	return fragmentTemplateJSON;
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
 * @param {object} `{uiConfigurationJSON, fragmentTemplateJSON}` Object with UI configuration
 * and fragment template
 * @param {number} id ID number of fragment
 * @return {object}
 */
export const convertToSelectedFragment = (
	{fragmentTemplateJSON, uiConfigurationJSON},
	id = 0
) => {
	return {
		fragmentOutput: replaceUIConfigurationValues(
			uiConfigurationJSON,
			fragmentTemplateJSON
		),
		fragmentTemplateJSON,
		id,
		uiConfigurationJSON,
		uiConfigurationValues: getUIConfigurationValues(uiConfigurationJSON),
	};
};

/**
 * Function to validate the UI configuration, used to identify whether
 * user is missing a required value.
 *
 * Examples:
 * validateUIConfigurationJSON({
 *  	defaultValue: 10,
 *  	name: 'Title Boost',
 *  	type: 'slider'
 *  })
 * => false
 *
 * validateUIConfigurationJSON({
 *  	defaultValue: 3,
 *  	key: 'context.timespan',
 *  	name: 'Time Span',
 *  	type: 'number',
 *  	unit: 'days'
 *  }
 * => true
 *
 * @param {object} uiConfigurationJSON Object with UI configuration
 * @return {boolean}
 */
export const validateUIConfigurationJSON = (uiConfigurationJSON) => {
	return uiConfigurationJSON.every((item) => {
		if (
			isNotNull(item.type) &&
			isNotNull(item.key) &&
			isNotNull(item.name)
		) {
			switch (item.type) {
				case INPUT_TYPES.SINGLE_SELECT:
					return (
						!!item.typeOptions &&
						item.typeOptions.length > 0 &&
						item.typeOptions.every(
							(option) =>
								isNotNull(option.label) &&
								isNotNull(option.value)
						)
					);
				case INPUT_TYPES.ENTITY:
					return (
						item.className && ENTITY_KEYS.includes(item.className)
					);
				default:
					return true;
			}
		}
		else {
			return (
				isNotNull(item.type) &&
				item.type === INPUT_TYPES.JSON &&
				isNotNull(item.key)
			);
		}
	});
};

const ENTITY_KEYS = [
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
