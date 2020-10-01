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
import ClayDatePicker from '@clayui/date-picker';
import {ClayInput, ClaySelect} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import ClaySlider from '@clayui/slider';
import ClaySticker from '@clayui/sticker';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {format, fromUnixTime, getUnixTime} from 'date-fns';
import {debounce, openSelectionModal} from 'frontend-js-web';
import {PropTypes} from 'prop-types';
import React, {useContext, useEffect, useState} from 'react';

import ThemeContext from '../ThemeContext';

const getDefaultValue = (item) => {
	return {
		value: !item.defaultValue
			? item.typeOptions
				? item.typeOptions[0].value
				: ''
			: item.defaultValue,
	};
};

const INPUT_JSON = {
	clauses: [
		{
			context: 'query',
			occur: 'must',
			query: {
				boost: 2,
				default_operator: '${config.operator}',
				fields: [
					{
						boost: '${config.title.boost}',
						field: 'title_${context.language_id}',
					},
					{
						boost: '${config.content.boost}',
						field: 'content_${context.language_id}',
					},
				],
			},
			type: 'simple_query_string',
		},
	],
	conditions: [],
	description: {
		en_US: 'A simple query string example',
	},
	enabled: '${config.lfr.enabled}',
	icon: 'time',
	title: {
		en_US: 'Match Any Keyword',
	},
	validate: [
		'${config.operator}',
		'${config.title.boost}',
		'${context.language_id}',
		'${context.timespan}',
		'${context.count}',
		'${context.date}',
		'${config.select.modal.role}',
		'${config.select.modal.user}',
		'${config.select.modal.organization}',
	],
};

const CONFIG_JSON = {
	configurationValues: [
		{
			defaultValue: 'or',
			helpText: 'This is the ...',
			key: 'config.operator',
			name: 'Query Clause Operator',
			type: 'single-select',
			typeOptions: [
				{
					label: 'OR',
					value: 'or',
				},
				{
					label: 'AND',
					value: 'and',
				},
			],
		},
		{
			defaultValue: 10,
			key: 'config.title.boost',
			name: 'Title Boost',
			type: 'slider',
		},
		{
			defaultValue: 'English',
			key: 'context.language_id',
			name: 'Context Language',
			type: 'text',
		},
		{
			defaultValue: 3,
			key: 'context.timespan',
			name: 'Time span',
			type: 'number',
			unit: 'days',
		},
		{
			defaultValue: 3,
			key: 'context.count',
			name: 'Number count',
			type: 'number',
		},
		{
			defaultValue: 1601751600, // Oct 3, 2020
			key: 'context.date',
			name: 'Relevant date',
			type: 'date',
		},
		{
			className: 'com.liferay.portal.kernel.model.Role',
			helpText: 'Select modal ...',
			key: 'config.select.modal.role',
			name: 'Select Modal',
			type: 'entity',
		},
		{
			className: 'com.liferay.portal.kernel.model.User',
			helpText: 'Select modal ...',
			key: 'config.select.modal.user',
			name: 'Select Modal',
			type: 'entity',
		},
		{
			className: 'com.liferay.portal.kernel.model.Organization',
			helpText: 'Select modal ...',
			key: 'config.select.modal.organization',
			name: 'Select Organization',
			type: 'entity',
		},
	],
};

export default function ConfigFragment({
	configurationJSON = CONFIG_JSON,
	entityJSON,
	inputJSON = INPUT_JSON,
	configValues = null,
}) {
	const {locale} = useContext(ThemeContext);
	const [collapse, setCollapse] = useState(false);
	const [, setOutputJSON] = useState(configurationJSON);
	const [inputValues, setInputValues] = useState(
		configValues
			? configValues
			: configurationJSON.configurationValues.reduce((acc, curr) => {
					return {
						...acc,
						[`${curr.key}`]: getDefaultValue(curr),
					};
			  }, {})
	);

	useEffect(() => {
		let flattenJSON = JSON.stringify(inputJSON);

		configurationJSON.configurationValues.map((config) => {
			if (inputValues[config.key].value) {
				if (
					config.type === 'slider' ||
					config.type === 'number' ||
					config.type === 'date'
				) {
					flattenJSON = flattenJSON.replaceAll(
						`"$\{${config.key}}"`,
						inputValues[config.key].value
					);
				}
				flattenJSON = flattenJSON.replaceAll(
					`\${${config.key}}`,
					inputValues[config.key].value
				);
			}
		});

		setOutputJSON(JSON.parse(flattenJSON));
	}, [configurationJSON, inputJSON, inputValues]);

	const _handleChange = debounce((key, item) => {
		setInputValues((inputValues) => {
			return {...inputValues, [key]: item};
		});
	}, 20);

	const _handleMutipleSelect = (key, className) => {
		if (entityJSON[`${className}`].multiple) {
			openSelectionModal({
				buttonAddLabel: Liferay.Language.get('select'),
				multiple: true,
				onSelect: (selectedItems) => {
					if (selectedItems) {
						_handleChange(key, {
							label: selectedItems.map((item) => item.name),
							value: selectedItems.map((item) => item.id),
						});
					}
				},
				selectEventName: 'selectEntity',
				title: entityJSON[`${className}`].title,
				url: entityJSON[`${className}`].url,
			});
		} else {
			openSelectionModal({
				buttonAddLabel: Liferay.Language.get('select'),
				onSelect: (event) => {
					_handleChange(key, {
						label: event.entityname,
						value: event.entityid,
					});
				},
				selectEventName: 'selectEntity',
				title: entityJSON[`${className}`].title,
				url: entityJSON[`${className}`].url,
			});
		}
	};

	function _renderInput(config) {
		switch (config.type) {
			case 'date':
				return (
					<div className="date-picker-input">
						<ClayDatePicker
							dateFormat="MM/dd/yyyy"
							onValueChange={(value) => {
								_handleChange(config.key, {
									value: getUnixTime(value),
								});
							}}
							placeholder="MM/DD/YYYY"
							readOnly
							value={
								inputValues[`${config.key}`].value
									? format(
											fromUnixTime(
												inputValues[`${config.key}`]
													.value
											),
											'MM/dd/yyyy'
									  )
									: ''
							}
							years={{
								end: 2024,
								start: 1997,
							}}
						/>

						{inputValues[config.key].value && (
							<ClayInput.GroupItem shrink>
								<ClayButton
									aria-label={Liferay.Language.get('delete')}
									displayType="unstyled"
									monospaced
									onClick={() =>
										_handleChange(config.key, {
											value: '',
										})
									}
								>
									<ClayIcon symbol="times-circle" />
								</ClayButton>
							</ClayInput.GroupItem>
						)}
					</div>
				);
			case 'entity':
				return (
					<ClayInput.Group>
						<ClayInput.GroupItem>
							<ClayInput
								aria-label={config.name}
								id={config.key}
								insetAfter={inputValues[config.key].label}
								readOnly
								type={'text'}
								value={
									inputValues[config.key].label &&
									entityJSON &&
									entityJSON[`${config.className}`].multiple
										? inputValues[config.key].label.join(
												', '
										  )
										: inputValues[config.key].label
								}
							/>
							{inputValues[config.key].label && (
								<ClayInput.GroupInsetItem after>
									<ClayButton
										aria-label={Liferay.Language.get(
											'delete'
										)}
										className="component-action"
										displayType="unstyled"
										onClick={() =>
											_handleChange(config.key, {
												label: '',
												value: '',
											})
										}
									>
										<ClayIcon symbol="times-circle" />
									</ClayButton>
								</ClayInput.GroupInsetItem>
							)}
						</ClayInput.GroupItem>
						<ClayInput.GroupItem shrink>
							<ClayButton
								aria-label={Liferay.Language.get('select')}
								displayType="secondary"
								onClick={() =>
									_handleMutipleSelect(
										config.key,
										config.className
									)
								}
							>
								{Liferay.Language.get('select')}
							</ClayButton>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				);
			case 'single-select':
				return (
					<ClaySelect
						aria-label={name}
						id={config.key}
						onChange={(event) => {
							_handleChange(config.key, {
								value: event.target.value,
							});
						}}
						value={inputValues[`${config.key}`].value}
					>
						{config.typeOptions.map((item) => (
							<ClaySelect.Option
								key={item.value}
								label={item.label}
								value={item.value}
							/>
						))}
					</ClaySelect>
				);
			case 'slider':
				return (
					<Slider
						keyword={config.key}
						name={config.name}
						onChange={_handleChange}
						value={inputValues[`${config.key}`].value}
					/>
				);
			case 'number':
				return (
					<ClayInput.Group>
						<ClayInput.GroupItem
							className={`${config.unit && 'arrowless-input'}`}
							prepend
						>
							<ClayInput
								aria-label={config.name}
								onChange={(event) => {
									_handleChange(config.key, {
										value: event.target.value,
									});
								}}
								type={'number'}
								value={inputValues[config.key].value}
							/>
						</ClayInput.GroupItem>
						{config.unit && (
							<ClayInput.GroupItem append shrink>
								<ClayInput.GroupText>
									{config.unit}
								</ClayInput.GroupText>
							</ClayInput.GroupItem>
						)}
					</ClayInput.Group>
				);
			default:
				return (
					<ClayInput.Group>
						<ClayInput.GroupItem prepend>
							<ClayInput
								aria-label={config.name}
								id={config.key}
								onChange={(event) =>
									_handleChange(config.key, {
										value: event.target.value,
									})
								}
								type={'text'}
								value={inputValues[config.key].value}
							/>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				);
		}
	}

	return (
		<div className="configuration-fragment-sheet sheet">
			<ClayList className="configuration-header-list">
				<ClayList.Item flex>
					<ClayList.ItemField>
						<ClaySticker className="icon" displayType="secondary">
							<ClayIcon symbol={inputJSON.icon} />
						</ClaySticker>
					</ClayList.ItemField>

					<ClayList.ItemField expand>
						<ClayList.ItemTitle>
							{inputJSON.title[locale]}
						</ClayList.ItemTitle>

						<ClayList.ItemText subtext={true}>
							{inputJSON.description[locale]}
						</ClayList.ItemText>
					</ClayList.ItemField>

					<ClayList.ItemField>
						<ClayButton
							aria-label={
								!collapse
									? Liferay.Language.get('collapse')
									: Liferay.Language.get('expand')
							}
							className="component-action"
							displayType="unstyled"
							onClick={() => {
								setCollapse(!collapse);
							}}
						>
							<ClayIcon
								symbol={
									!collapse ? 'angle-down' : 'angle-right'
								}
							/>
						</ClayButton>
					</ClayList.ItemField>
				</ClayList.Item>
			</ClayList>

			{!collapse && (
				<ClayList className="configuration-form-list">
					<ClayList.Header>
						{Liferay.Language.get('clauses')}
					</ClayList.Header>

					{configurationJSON.configurationValues.map((config) => (
						<ClayList.Item flex key={config.key}>
							<ClayList.ItemField className="list-item-label">
								<label htmlFor={config.key}>
									{config.name}
									{config.helpText && (
										<ClayTooltipProvider>
											<ClaySticker
												displayType="unstyled"
												size="sm"
											>
												<ClayIcon
													data-tooltip-align="top"
													symbol="info-circle"
													title={config.helpText}
												/>
											</ClaySticker>
										</ClayTooltipProvider>
									)}
								</label>
							</ClayList.ItemField>

							<ClayList.ItemField expand>
								{_renderInput(config)}
							</ClayList.ItemField>
						</ClayList.Item>
					))}
				</ClayList>
			)}
		</div>
	);
}

function Slider({keyword, name, onChange, value}) {
	const [active, setActive] = useState(false);

	return (
		<>
			<ClayInput.Group>
				<ClayInput.GroupItem className="arrowless-input">
					<ClayInput
						aria-label={name}
						insetAfter
						onChange={(event) => {
							onChange(keyword, {value: event.target.value});
						}}
						type={'number'}
						value={value}
					/>
					<ClayInput.GroupInsetItem after>
						<ClayButton
							aria-label={Liferay.Language.get('slider')}
							displayType="unstyled"
							onClick={() => {
								setActive(!active);
							}}
						>
							<ClayIcon symbol="control-panel" />
						</ClayButton>
					</ClayInput.GroupInsetItem>
				</ClayInput.GroupItem>
			</ClayInput.Group>
			{active && (
				<div className="slider-configuration">
					<label>Impact</label>
					<ClaySlider
						id={keyword}
						max={100}
						min={-100}
						onValueChange={(value) => {
							onChange(keyword, {value});
						}}
						value={value}
					/>
				</div>
			)}
		</>
	);
}

ConfigFragment.propTypes = {
	configurationJSON: PropTypes.object,
	inputJSON: PropTypes.object,
};
