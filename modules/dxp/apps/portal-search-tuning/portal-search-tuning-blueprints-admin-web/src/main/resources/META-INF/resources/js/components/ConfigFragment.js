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

import ThemeContext from '../ThemeContext';
import PreviewModal from '../shared/PreviewModal';
import {replaceConfigValues, validateConfigJSON} from '../utils/utils';
import CodeMirrorEditor from './CodeMirrorEditor';

function ConfigFragment({
	collapseAll,
	configJSON,
	configValues,
	deleteFragment,
	entityJSON,
	inputJSON,
	queryConfig,
	updateFragment = () => {},
}) {
	const {locale} = useContext(ThemeContext);
	const [collapse, setCollapse] = useState(false);
	const [active, setActive] = useState(false);

	useEffect(() => {
		setCollapse(collapseAll);
	}, [collapseAll]);

	const _handleChange = debounce((key, item) => {
		const newConfigValues = {...configValues, [key]: item};

		updateFragment(
			newConfigValues,
			replaceConfigValues(configJSON, inputJSON, newConfigValues)
		);
	}, 20);

	const _handleMutipleSelect = (key, className) => {
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
		} else {
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
		!!configJSON &&
		!!configJSON.configurationValues &&
		configJSON.configurationValues.length > 0;

	function _renderInput(config) {
		switch (config.type) {
			case 'date':
				return (
					<div className="date-picker-input">
						<ClayDatePicker
							dateFormat="MM/dd/yyyy"
							onValueChange={(value) => {
								_handleChange(config.key, moment(value).unix());
							}}
							placeholder="MM/DD/YYYY"
							readOnly
							value={
								configValues[`${config.key}`]
									? moment
											.unix(configValues[`${config.key}`])
											.format('MM/DD/YYYY')
									: ''
							}
							years={{
								end: 2024,
								start: 1997,
							}}
						/>

						{configValues[config.key] && (
							<ClayInput.GroupItem shrink>
								<ClayButton
									aria-label={Liferay.Language.get('delete')}
									displayType="unstyled"
									monospaced
									onClick={() =>
										_handleChange(config.key, '')
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
								insetAfter={configValues[config.key].length > 0}
								readOnly
								type={'text'}
								value={
									configValues[config.key].length > 0
										? configValues[config.key]
												.map((item) => item.name)
												.join(', ')
										: ''
								}
							/>
							{configValues[config.key].length > 0 && (
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
										_handleMutipleSelect(
											config.key,
											config.className
										);
									}
								}}
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
							_handleChange(config.key, event.target.value);
						}}
						value={configValues[`${config.key}`]}
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
			case 'slider':
				return (
					<Slider
						keyword={config.key}
						name={config.name}
						onChange={_handleChange}
						value={configValues[`${config.key}`]}
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
									_handleChange(
										config.key,
										parseInt(event.target.value, 10)
									);
								}}
								type={'number'}
								value={configValues[config.key]}
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
									_handleChange(
										config.key,
										event.target.value
									)
								}
								type={'text'}
								value={configValues[config.key]}
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

					{(queryConfig || deleteFragment) && (
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
										className="component-action"
										displayType="unstyled"
									>
										<ClayIcon symbol="ellipsis-v" />
									</ClayButton>
								</ClayList.ItemField>
							}
						>
							<ClayDropDown.ItemList>
								{deleteFragment && (
									<ClayDropDown.Item onClick={deleteFragment}>
										{Liferay.Language.get('delete')}
									</ClayDropDown.Item>
								)}

								{queryConfig && (
									<PreviewModal
										body={
											<CodeMirrorEditor
												readOnly
												value={JSON.stringify(
													queryConfig,
													null,
													'\t'
												)}
											/>
										}
										title={Liferay.Language.get(
											'query-configuration'
										)}
									>
										<ClayDropDown.Item>
											{Liferay.Language.get(
												'query-configuration'
											)}
										</ClayDropDown.Item>
									</PreviewModal>
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
				<>
					{(!_hasConfigurationValues ||
						!validateConfigJSON(configJSON)) && (
						<ClayAlert
							displayType="danger"
							title={Liferay.Language.get('info')}
						>
							{Liferay.Language.get(
								'an-error-is-preventing-one-or-more-fields-from-displaying'
							)}
						</ClayAlert>
					)}

					{_hasConfigurationValues && (
						<ClayList className="configuration-form-list">
							<ClayList.Header>
								{Liferay.Language.get('clauses')}
							</ClayList.Header>

							{configJSON.configurationValues.map((config) => (
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
					<label>Impact</label>
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

ConfigFragment.propTypes = {
	collapseAll: PropTypes.bool,
	configJSON: PropTypes.object,
	configValues: PropTypes.object,
	deleteFragment: PropTypes.func,
	entityJSON: PropTypes.object,
	inputJSON: PropTypes.object,
	updateFragment: PropTypes.func,
};

export default React.memo(ConfigFragment);
