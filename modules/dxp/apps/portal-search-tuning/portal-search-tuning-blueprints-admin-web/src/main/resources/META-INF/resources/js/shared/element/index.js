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
import ClayDropDown from '@clayui/drop-down';
import {ClayToggle} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import ClaySticker from '@clayui/sticker';
import {ClayTooltipProvider} from '@clayui/tooltip';
import getCN from 'classnames';
import {PropTypes} from 'prop-types';
import React, {useContext, useEffect, useState} from 'react';

import {INPUT_TYPES} from '../../utils/inputTypes';
import {validateUIConfigurationJSON} from '../../utils/utils';
import CodeMirrorEditor from '../CodeMirrorEditor';
import PreviewModal from '../PreviewModal';
import ThemeContext from '../ThemeContext';
import DateInput from './DateInput';
import EntityInput from './EntityInput';
import FieldInput from './FieldInput';
import FieldListInput from './FieldListInput';
import JSONInput from './JSONInput';
import MultiSelectInput from './MultiSelectInput';
import NumberInput from './NumberInput';
import SelectInput from './SelectInput';
import SliderInput from './SliderInput';
import TextInput from './TextInput';

function Element({
	collapseAll,
	uiConfigurationJSON,
	uiConfigurationValues,
	deleteFragment,
	entityJSON,
	elementTemplateJSON,
	fragmentOutput,
	id,
	initialUIConfigurationValues = {},
	updateFragment = () => {},
}) {
	const {locale} = useContext(ThemeContext);
	const [collapse, setCollapse] = useState(false);
	const [active, setActive] = useState(false);

	useEffect(() => {
		setCollapse(collapseAll);
	}, [collapseAll]);

	const _getInputId = (elementId, configKey) => {
		return `${elementId}_${configKey}`;
	};

	const _handleDelete = () => {
		deleteFragment(id);
	};

	const _handleChange = (key, value) => {
		updateFragment(id, {
			uiConfigurationValues: {
				...uiConfigurationValues,
				[key]: value,
			},
		});
	};

	const _handleToggle = () => {
		const enabled = !elementTemplateJSON.enabled;

		updateFragment(id, {
			elementTemplateJSON: {
				...elementTemplateJSON,
				enabled,
			},
			fragmentOutput: {
				...fragmentOutput,
				enabled,
			},
		});
	};

	const _hasConfigurationValues =
		!!uiConfigurationJSON && uiConfigurationJSON.length > 0;

	const _renderInput = (config) => {
		const disabled = !elementTemplateJSON.enabled;
		const inputId = _getInputId(id, config.key);

		switch (config.type) {
			case INPUT_TYPES.DATE:
				return (
					<DateInput
						configKey={config.key}
						disabled={disabled}
						onChange={_handleChange}
						value={uiConfigurationValues[config.key]}
					/>
				);
			case INPUT_TYPES.ENTITY:
				return (
					<EntityInput
						className={config.className}
						configKey={config.key}
						disabled={disabled}
						entityJSON={entityJSON}
						label={config.label}
						onChange={_handleChange}
						value={uiConfigurationValues[config.key]}
					/>
				);
			case INPUT_TYPES.FIELD_LIST:
				return (
					<FieldListInput
						boost={config.boost}
						configKey={config.key}
						disabled={disabled}
						onChange={_handleChange}
						typeOptions={config.typeOptions}
						value={uiConfigurationValues[config.key]}
					/>
				);
			case INPUT_TYPES.JSON:
				return (
					<JSONInput
						configKey={config.key}
						disabled={disabled}
						initialValue={uiConfigurationValues[config.key]}
						onChange={_handleChange}
					/>
				);
			case INPUT_TYPES.MULTISELECT:
				return (
					<MultiSelectInput
						configKey={config.key}
						disabled={disabled}
						onChange={_handleChange}
						value={uiConfigurationValues[config.key]}
					/>
				);
			case INPUT_TYPES.NUMBER:
				return (
					<NumberInput
						configKey={config.key}
						disabled={disabled}
						label={config.label}
						onChange={_handleChange}
						unit={config.unit}
						value={uiConfigurationValues[config.key]}
					/>
				);
			case INPUT_TYPES.FIELD:
				return (
					<FieldInput
						boost={config.boost}
						configKey={config.key}
						disabled={disabled}
						onChange={_handleChange}
						value={uiConfigurationValues[config.key]}
					/>
				);
			case INPUT_TYPES.SELECT:
				return (
					<SelectInput
						configKey={config.key}
						disabled={disabled}
						label={config.label}
						onChange={_handleChange}
						typeOptions={config.typeOptions}
						value={uiConfigurationValues[config.key]}
					/>
				);
			case INPUT_TYPES.SLIDER:
				return (
					<SliderInput
						configKey={config.key}
						disabled={disabled}
						id={inputId}
						initialValue={initialUIConfigurationValues[config.key]}
						label={config.label}
						onChange={_handleChange}
					/>
				);
			default:
				return (
					<TextInput
						configKey={config.key}
						disabled={disabled}
						id={inputId}
						initialValue={initialUIConfigurationValues[config.key]}
						label={config.label}
						onChange={_handleChange}
					/>
				);
		}
	};

	return (
		<div
			className={getCN('configuration-fragment-sheet', 'sheet', {
				disabled: !elementTemplateJSON.enabled,
			})}
		>
			<ClayList className="configuration-header-list">
				<ClayList.Item flex>
					<ClayList.ItemField>
						<ClaySticker size="md">
							<ClayIcon symbol={elementTemplateJSON.icon} />
						</ClaySticker>
					</ClayList.ItemField>

					<ClayList.ItemField expand>
						{elementTemplateJSON.title && (
							<ClayList.ItemTitle>
								{elementTemplateJSON.title[locale] ||
									(typeof elementTemplateJSON.title ==
										'string' &&
										elementTemplateJSON.title)}
							</ClayList.ItemTitle>
						)}

						{elementTemplateJSON.description && (
							<ClayList.ItemText subtext={true}>
								{elementTemplateJSON.description[locale] ||
									(typeof elementTemplateJSON.description ==
										'string' &&
										elementTemplateJSON.description)}
							</ClayList.ItemText>
						)}
					</ClayList.ItemField>

					<ClayToggle
						onToggle={_handleToggle}
						toggled={elementTemplateJSON.enabled}
					/>

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
											<div className="configuration-json-modal">
												<CodeMirrorEditor
													readOnly
													value={JSON.stringify(
														fragmentOutput,
														null,
														'\t'
													)}
												/>
											</div>
										}
										size="lg"
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
									<ClayDropDown.Item onClick={_handleDelete}>
										{Liferay.Language.get('remove')}
									</ClayDropDown.Item>
								)}
							</ClayDropDown.ItemList>
						</ClayDropDown>
					)}

					{_hasConfigurationValues && (
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
					)}
				</ClayList.Item>
			</ClayList>

			{!collapse && (
				<>
					{!validateUIConfigurationJSON(uiConfigurationJSON) && (
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
								<ClayList.Item
									className={config.type}
									flex
									key={config.key}
								>
									{config.type !== INPUT_TYPES.JSON && (
										<ClayList.ItemField className="list-item-label">
											<label
												htmlFor={_getInputId(
													id,
													config.key
												)}
											>
												{config.label}

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
									)}

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

Element.propTypes = {
	collapseAll: PropTypes.bool,
	deleteFragment: PropTypes.func,
	elementTemplateJSON: PropTypes.object,
	entityJSON: PropTypes.object,
	fragmentOutput: PropTypes.object,
	id: PropTypes.number,
	initialUIConfigurationValues: PropTypes.object,
	uiConfigurationJSON: PropTypes.arrayOf(PropTypes.object),
	uiConfigurationValues: PropTypes.object,
	updateFragment: PropTypes.func,
};

export default React.memo(Element);
