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

import ClayAlert from '@clayui/alert';
import ClayButton from '@clayui/button';
import ClayDatePicker from '@clayui/date-picker';
import ClayDropDown from '@clayui/drop-down';
import {ClayInput, ClaySelect} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import ClaySlider from '@clayui/slider';
import ClaySticker from '@clayui/sticker';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {debounce, openSelectionModal} from 'frontend-js-web';
import moment from 'moment';
import {PropTypes} from 'prop-types';
import React, {useContext, useEffect, useState} from 'react';

import {INPUT_TYPES} from '../utils/inputTypes';
import {
	replaceUIConfigurationValues,
	validateUIConfigurationJSON,
} from '../utils/utils';
import CodeMirrorEditor from './CodeMirrorEditor';
import PreviewModal from './PreviewModal';
import ThemeContext from './ThemeContext';

function Slider({keyword, name, onChange, value}) {
	const [active, setActive] = useState(false);

	return (
		<>
			<ClayInput.Group small>
				<ClayInput.GroupItem className="arrowless-input">
					<ClayInput
						aria-label={name}
						insetAfter
						onChange={(event) => {
							onChange(keyword, parseInt(event.target.value, 10));
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
					<ClaySlider
						id={keyword}
						max={100}
						min={-100}
						onValueChange={(value) => {
							onChange(keyword, value);
						}}
						value={value}
					/>
				</div>
			)}
		</>
	);
}

function ConfigFragment({
	collapseAll,
	uiConfigurationJSON,
	uiConfigurationValues,
	deleteFragment,
	entityJSON,
	fragmentTemplateJSON,
	fragmentOutput,
	updateFragment = () => {},
}) {
	const {availableLanguages, locale} = useContext(ThemeContext);
	const [collapse, setCollapse] = useState(false);
	const [active, setActive] = useState(false);

	useEffect(() => {
		setCollapse(collapseAll);
	}, [collapseAll]);

	const _handleChange = debounce((key, item) => {
		const newUIConfigurationValues = {
			...uiConfigurationValues,
			[key]: item,
		};

		updateFragment(
			newUIConfigurationValues,
			replaceUIConfigurationValues(
				uiConfigurationJSON,
				fragmentTemplateJSON,
				newUIConfigurationValues
			)
		);
	}, 20);

	const _handleMultipleSelect = (key, className) => {
		if (entityJSON[`${className}`].multiple) {
			openSelectionModal({
				buttonAddLabel: Liferay.Language.get('select'),
				multiple: true,
				onSelect: (selectedItems) => {
					if (selectedItems) {
						_handleChange(key, selectedItems);
					}
				},
				selectEventName: 'selectEntity',
				title: entityJSON[`${className}`].title,
				url: entityJSON[`${className}`].url,
			});
		}
		else {
			openSelectionModal({
				buttonAddLabel: Liferay.Language.get('select'),
				onSelect: (event) => {
					_handleChange(key, [
						{
							id: event.entityid,
							name: event.entityname,
						},
					]);
				},
				selectEventName: 'selectEntity',
				title: entityJSON[`${className}`].title,
				url: entityJSON[`${className}`].url,
			});
		}
	};

	const _hasConfigurationValues =
		!!uiConfigurationJSON && uiConfigurationJSON.length > 0;

	function _renderInput(config) {
		switch (config.type) {
			case INPUT_TYPES.DATE:
				return (
					<div className="date-picker-input">
						<ClayDatePicker
							dateFormat="MM/dd/yyyy"
							onValueChange={(value) => {
								_handleChange(config.key, moment(value).unix());
							}}
							placeholder="MM/DD/YYYY"
							readOnly
							sizing="sm"
							value={
								uiConfigurationValues[`${config.key}`]
									? moment
											.unix(
												uiConfigurationValues[
													`${config.key}`
												]
											)
											.format('MM/DD/YYYY')
									: ''
							}
							years={{
								end: 2024,
								start: 1997,
							}}
						/>

						{!!uiConfigurationValues[config.key] && (
							<ClayInput.GroupItem shrink>
								<ClayButton
									aria-label={Liferay.Language.get('delete')}
									displayType="unstyled"
									monospaced
									onClick={() =>
										_handleChange(config.key, '')
									}
									small
								>
									<ClayIcon symbol="times-circle" />
								</ClayButton>
							</ClayInput.GroupItem>
						)}
					</div>
				);
			case INPUT_TYPES.ENTITY:
				return (
					<ClayInput.Group small>
						<ClayInput.GroupItem>
							<ClayInput
								aria-label={config.name}
								id={config.key}
								insetAfter={
									uiConfigurationValues[config.key].length > 0
								}
								readOnly
								type="text"
								value={
									uiConfigurationValues[config.key].length > 0
										? uiConfigurationValues[config.key]
												.map((item) => item.name)
												.join(', ')
										: ''
								}
							/>
							{uiConfigurationValues[config.key].length > 0 && (
								<ClayInput.GroupInsetItem after>
									<ClayButton
										aria-label={Liferay.Language.get(
											'delete'
										)}
										className="component-action"
										displayType="unstyled"
										onClick={() =>
											_handleChange(config.key, [])
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
								disabled={!entityJSON}
								displayType="secondary"
								onClick={() => {
									if (entityJSON) {
										_handleMultipleSelect(
											config.key,
											config.className
										);
									}
								}}
								small
							>
								{Liferay.Language.get('select')}
							</ClayButton>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				);
			case INPUT_TYPES.FIELD_SELECT:
				return (
					<ClayInput.Group small>
						<ClayInput.GroupItem>
							<ClaySelect
								aria-label={config.name}
								className="form-control-sm"
								id={`${config.key}_field`}
								onChange={(event) =>
									_handleChange(config.key, {
										...uiConfigurationValues[config.key],
										field: event.target.value,
									})
								}
								value={uiConfigurationValues[config.key].field}
							>
								{config.typeOptions.map((option) => (
									<ClaySelect.Option
										key={option.field}
										label={option.label}
										value={option.field}
									/>
								))}
							</ClaySelect>
						</ClayInput.GroupItem>

						<ClayInput.GroupItem>
							<ClaySelect
								aria-label={config.name}
								className="form-control-sm"
								id={`${config.key}_locale`}
								onChange={(event) =>
									_handleChange(config.key, {
										...uiConfigurationValues[config.key],
										locale: event.target.value,
									})
								}
								value={uiConfigurationValues[config.key].locale}
							>
								<ClaySelect.Option
									key="users-language"
									label={Liferay.Language.get(
										'users-language'
									)}
									value={'_${context.language_id}'}
								/>

								<ClaySelect.Option
									key="no-localization"
									label={Liferay.Language.get(
										'no-localization'
									)}
									value=""
								/>

								{Object.keys(availableLanguages).map(
									(locale) => (
										<ClaySelect.Option
											key={locale}
											label={availableLanguages[locale]}
											value={`_${locale}`}
										/>
									)
								)}
							</ClaySelect>
						</ClayInput.GroupItem>
					</ClayInput.Group>
				);
			case INPUT_TYPES.SINGLE_SELECT:
				return (
					<ClaySelect
						aria-label={name}
						className="form-control-sm"
						id={config.key}
						onChange={(event) => {
							const value =
								typeof config.typeOptions[0].value ==
									'boolean' ||
								typeof config.typeOptions[0].value == 'number'
									? JSON.parse(event.target.value)
									: event.target.value;

							_handleChange(config.key, value);
						}}
						value={uiConfigurationValues[`${config.key}`]}
					>
						{config.typeOptions &&
							config.typeOptions.map((item) => (
								<ClaySelect.Option
									key={item.value}
									label={item.label}
									value={item.value}
								/>
							))}
					</ClaySelect>
				);
			case INPUT_TYPES.SLIDER:
				return (
					<Slider
						keyword={config.key}
						name={config.name}
						onChange={_handleChange}
						value={uiConfigurationValues[`${config.key}`]}
					/>
				);
			case INPUT_TYPES.NUMBER:
				return (
					<ClayInput.Group small>
						<ClayInput.GroupItem
							className={`${config.unit && 'arrowless-input'}`}
							prepend
						>
							<ClayInput
								aria-label={config.name}
								onChange={(event) => {
									_handleChange(
										config.key,
										event.target.value !== ''
											? JSON.parse(event.target.value)
											: event.target.value
									);
								}}
								type={'number'}
								value={uiConfigurationValues[config.key]}
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
					<ClayInput.Group small>
						<ClayInput.GroupItem prepend>
							<ClayInput
								aria-label={config.name}
								id={config.key}
								onChange={(event) =>
									_handleChange(
										config.key,
										event.target.value
									)
								}
								type={'text'}
								value={uiConfigurationValues[config.key]}
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
					<ClayList.ItemField expand>
						{fragmentTemplateJSON.title && (
							<ClayList.ItemTitle>
								{fragmentTemplateJSON.title[locale] ||
									fragmentTemplateJSON.title}
							</ClayList.ItemTitle>
						)}

						{fragmentTemplateJSON.description && (
							<ClayList.ItemText subtext={true}>
								{fragmentTemplateJSON.description[locale] ||
									fragmentTemplateJSON.description}
							</ClayList.ItemText>
						)}
					</ClayList.ItemField>

					{(fragmentOutput || deleteFragment) && (
						<ClayDropDown
							active={active}
							alignmentPosition={3}
							onActiveChange={setActive}
							trigger={
								<ClayList.ItemField>
									<ClayButton
										aria-label={Liferay.Language.get(
											'dropdown'
										)}
										borderless
										displayType="secondary"
										monospaced
										small
									>
										<ClayIcon symbol="ellipsis-v" />
									</ClayButton>
								</ClayList.ItemField>
							}
						>
							<ClayDropDown.ItemList>
								{fragmentOutput && (
									<PreviewModal
										body={
											<CodeMirrorEditor
												readOnly
												value={JSON.stringify(
													fragmentOutput,
													null,
													'\t'
												)}
											/>
										}
										title={Liferay.Language.get(
											'query-configuration-json'
										)}
									>
										<ClayDropDown.Item>
											{Liferay.Language.get(
												'query-configuration-json'
											)}
										</ClayDropDown.Item>
									</PreviewModal>
								)}

								{deleteFragment && (
									<ClayDropDown.Item onClick={deleteFragment}>
										{Liferay.Language.get('remove')}
									</ClayDropDown.Item>
								)}
							</ClayDropDown.ItemList>
						</ClayDropDown>
					)}

					<ClayList.ItemField>
						<ClayButton
							aria-label={
								!collapse
									? Liferay.Language.get('collapse')
									: Liferay.Language.get('expand')
							}
							borderless
							displayType="secondary"
							monospaced
							onClick={() => {
								setCollapse(!collapse);
							}}
							small
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
				<>
					{(!_hasConfigurationValues ||
						!validateUIConfigurationJSON(uiConfigurationJSON)) && (
						<ClayAlert
							displayType="danger"
							title={Liferay.Language.get('error')}
						>
							{Liferay.Language.get(
								'an-error-is-preventing-one-or-more-fields-from-displaying'
							)}
						</ClayAlert>
					)}

					{_hasConfigurationValues && (
						<ClayList className="configuration-form-list">
							{uiConfigurationJSON.map((config) => (
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
															title={
																config.helpText
															}
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
				</>
			)}
		</div>
	);
}

ConfigFragment.propTypes = {
	collapseAll: PropTypes.bool,
	deleteFragment: PropTypes.func,
	entityJSON: PropTypes.object,
	fragmentOutput: PropTypes.object,
	fragmentTemplateJSON: PropTypes.object,
	uiConfigurationJSON: PropTypes.arrayOf(PropTypes.object),
	uiConfigurationValues: PropTypes.object,
	updateFragment: PropTypes.func,
};

export default React.memo(ConfigFragment);
