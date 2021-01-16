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

import {CONFIG_PREFIX} from './constants';
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
 * Function to validate the UI configuration, used to identify whether
 * a required value is not null, undefined, or simply an empty string
 *
 * Examples:
 * isNotEmpty([])
 * => true
 * isNotEmpty('')
 * => false
 * isNotEmpty(null)
 * => false
 *
 * @param {String|object} item Item to check
 * @return {boolean}
 */
export const isNotEmpty = (item) =>
	item !== null && item !== '' && typeof item !== 'undefined';

/**
 * Function to validate the UI configuration, used to identify whether
 * every item in array is not null, undefined, or simply an empty string
 *
 * @param {Array} item Item to check
 * @return {boolean}
 */
const isNotAllEmpty = (item) => item.every(isNotEmpty);

/**
 * Function to validate the UI configuration, used to identify whether
 * a required value is not null or undefined
 *
 * Examples:
 * isNotNullOrUndefined([])
 * => true
 * isNotNullOrUndefined('')
 * => true
 * isNotNullOrUndefined(null)
 * => false
 *
 * @param {String|object} item Item to check
 * @return {boolean}
 */
const isNotNullOrUndefined = (item) =>
	item !== null && typeof item !== 'undefined';

/**
 * Function to replace all instances of a string.
 *
 * Example:
 * replaceStr('title_${config.language}', '${config.language}', 'en_US')
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
 * Function to return a new object with renamed keys.
 *
 * Example:
 * renameKeys({"en-US": "Hello", "zh-CN": "Ni Hao"}, (str) => str.replace('-', '_'))
 * => {en_US: "Hello", zh_CN: "Ni Hao"}
 *
 * @param {Object} obj Original object
 * @return {Object}
 */
export const renameKeys = (obj, func) => {
	const newObj = {};

	Object.keys(obj).map((key) => {
		newObj[`${func(key)}`] = obj[key];
	});

	return newObj;
};

/**
 * Function turn string into number, otherwise returns itself.
 *
 * Example:
 * toNumber('234')
 * => 234
 * toNumber(234)
 * => 234
 * toNumber('0234')
 * => '0234'
 *
 * @param {String} str String
 * @return {number}
 */
export const toNumber = (str) => {
	try {
		return JSON.parse(str);
	}
	catch {
		return str;
	}
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
			return isNotEmpty(itemValue)
				? itemValue
				: item.typeOptions && item.typeOptions[0].value
				? item.typeOptions[0].value
				: '';
		case INPUT_TYPES.DATE:
			return isNotEmpty(itemValue)
				? typeof itemValue == 'number'
					? itemValue
					: moment(itemValue).isValid()
					? moment(itemValue).unix()
					: ''
				: '';
		case INPUT_TYPES.ENTITY:
			return isNotEmpty(itemValue) &&
				itemValue.length > 0 &&
				itemValue.every(
					(item) => isNotEmpty(item.id) && isNotEmpty(item.name)
				)
				? itemValue
				: [];
		case INPUT_TYPES.MULTISELECT:
			return isNotEmpty(itemValue) ? itemValue : [];
		case INPUT_TYPES.NUMBER:
			return isNotEmpty(itemValue) && typeof itemValue == 'number'
				? itemValue
				: '';
		case INPUT_TYPES.SLIDER:
			return isNotEmpty(itemValue) && typeof itemValue == 'number'
				? itemValue
				: '';
		case INPUT_TYPES.FIELD:
			return isNotEmpty(itemValue) &&
				itemValue.every(
					(item) =>
						isNotNullOrUndefined(item.field) &&
						isNotNullOrUndefined(item.locale)
				)
				? itemValue
				: [];
		case INPUT_TYPES.SINGLE_FIELD:
			return isNotEmpty(itemValue) &&
				itemValue.every(
					(item) =>
						isNotNullOrUndefined(item.field) &&
						isNotNullOrUndefined(item.locale)
				)
				? itemValue
				: [
						{
							field: '',
							locale: '',
						},
				  ];
		case INPUT_TYPES.JSON:
			return isNotEmpty(itemValue) ? itemValue : {};
		default:
			return isNotEmpty(itemValue) ? itemValue : '';
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
			else if (
				config.type === INPUT_TYPES.FIELD ||
				config.type === INPUT_TYPES.SINGLE_FIELD
			) {
				const fields = uiConfigurationValues[config.key].map(
					(item) =>
						`${item.field}${
							item.locale == '' || item.locale.includes('$')
								? item.locale
								: '_' + item.locale
						}${
							item.boost && JSON.parse(item.boost) > 1
								? '^' + item.boost
								: ''
						}`
				);

				configValue =
					config.type === INPUT_TYPES.FIELD
						? JSON.stringify(fields)
						: fields[0];
			}
			else if (config.type === INPUT_TYPES.NUMBER) {
				const oldConfigValue = uiConfigurationValues[config.key];

				configValue = isNotEmpty(config.unitSuffix)
					? typeof oldConfigValue == 'string'
						? oldConfigValue.concat('', config.unitSuffix)
						: JSON.stringify(oldConfigValue).concat(
								'',
								config.unitSuffix
						  )
					: oldConfigValue;
			}
			else if (config.type === INPUT_TYPES.DATE) {
				configValue = uiConfigurationValues[config.key]
					? JSON.parse(
							moment
								.unix(uiConfigurationValues[config.key])
								.format(
									config.format
										? config.format
										: 'YYYYMMDDHHMMSS'
								)
					  )
					: '';
			}
			else if (config.type === INPUT_TYPES.JSON) {
				configValue = JSON.stringify(uiConfigurationValues[config.key]);
			}
			else if (config.type === INPUT_TYPES.MULTISELECT) {
				configValue = JSON.stringify(
					uiConfigurationValues[config.key].map((item) =>
						toNumber(item.value)
					)
				);
			}

			// Check if key starts with 'config.' prefix to support keys with
			// both the prefix or not

			const key =
				config.key &&
				config.key.substring(0, CONFIG_PREFIX.length + 1) ===
					`${CONFIG_PREFIX}.`
					? config.key
					: `${CONFIG_PREFIX}.${config.key}`;

			// Check whether to add quotes around output

			if (
				typeof configValue === 'number' ||
				typeof configValue === 'boolean' ||
				config.type === INPUT_TYPES.ENTITY ||
				config.type === INPUT_TYPES.FIELD ||
				config.type === INPUT_TYPES.JSON ||
				config.type === INPUT_TYPES.MULTISELECT
			) {
				flattenJSON = replaceStr(
					flattenJSON,
					`"$\{${key}}"`,
					configValue
				);
			}

			flattenJSON = replaceStr(flattenJSON, `\${${key}}`, configValue);
		});

		return JSON.parse(flattenJSON);
	}

	return fragmentTemplateJSON;
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

const ENTITY_KEYS = [
	'com.liferay.portal.kernel.model.Group',
	'com.liferay.portal.kernel.model.Organization',
	'com.liferay.portal.kernel.model.Role',
	'com.liferay.portal.kernel.model.Team',
	'com.liferay.portal.kernel.model.User',
	'com.liferay.portal.kernel.model.UserGroup',
];

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
		if (item.type === INPUT_TYPES.JSON) {
			return isNotEmpty(item.key);
		}
		else if (item.type === INPUT_TYPES.ENTITY) {
			return (
				isNotAllEmpty([item.key, item.name, item.className]) &&
				ENTITY_KEYS.includes(item.className)
			);
		}
		else if (item.type === INPUT_TYPES.SINGLE_SELECT) {
			return (
				isNotAllEmpty([item.key, item.name, item.typeOptions]) &&
				item.typeOptions.length > 0 &&
				item.typeOptions.every(
					(option) =>
						isNotNullOrUndefined(option.label) &&
						isNotNullOrUndefined(option.value)
				)
			);
		}
		else {
			return isNotAllEmpty([item.type, item.key, item.name]);
		}
	});
};
