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
import ClayEmptyState from '@clayui/empty-state';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClaySticker from '@clayui/sticker';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {fetch, navigate} from 'frontend-js-web';
import {PropTypes} from 'prop-types';
import React, {useContext, useRef, useState} from 'react';

import CodeMirrorEditor from '../shared/CodeMirrorEditor';
import ConfigFragment from '../shared/ConfigFragment';
import ErrorBoundary from '../shared/ErrorBoundary';
import PageToolbar from '../shared/PageToolbar';
import PreviewModal from '../shared/PreviewModal';
import ThemeContext from '../shared/ThemeContext';
import {PREDEFINED_VARIABLES} from '../utils/data';
import {
	getUIConfigurationValues,
	openErrorToast,
	replaceUIConfigurationValues,
} from '../utils/utils';

function EditFragmentForm({
	blueprintId,
	blueprintType,
	initialConfigurationString,
	initialDescription,
	initialTitle,
	predefinedVariables = PREDEFINED_VARIABLES,
	redirectURL,
	submitFormURL,
}) {
	const {namespace} = useContext(ThemeContext);

	const initialConfiguration = JSON.parse(initialConfigurationString);

	const form = useRef();
	const fragmentTemplateJSONRef = useRef();

	const [isSubmitting, setIsSubmitting] = useState(false);
	const [expand, setExpand] = useState(false);

	const [fragmentTemplateJSON, setFragmentTemplateJSON] = useState(
		JSON.stringify(initialConfiguration.fragmentTemplateJSON, null, '\t')
	);
	const [uiConfigurationJSON, setUIConfigurationJSON] = useState(
		JSON.stringify(initialConfiguration.uiConfigurationJSON, null, '\t')
	);

	function addFragmentTemplateJSONVariable(variable) {
		var doc = fragmentTemplateJSONRef.current.getDoc();
		var cursor = doc.getCursor();

		doc.replaceRange(variable, cursor);
	}

	function _renderPreviewBody() {
		let previewFragmentTemplateJSON = {};
		let previewUIConfigurationJSON = {};

		try {
			previewFragmentTemplateJSON = JSON.parse(fragmentTemplateJSON);
			previewUIConfigurationJSON = JSON.parse(uiConfigurationJSON);
		}
		catch (e) {
			return (
				<ClayEmptyState
					description={Liferay.Language.get(
						'json-may-be-incorrect-and-we-were-unable-to-load-a-preview-of-the-configuration'
					)}
					imgSrc="/o/admin-theme/images/states/empty_state.gif"
					title={Liferay.Language.get('unable-to-load-preview')}
				/>
			);
		}

		return (
			<div className="portlet-blueprints-admin">
				<ErrorBoundary>
					<ConfigFragment
						collapseAll={false}
						fragmentOutput={replaceUIConfigurationValues(
							previewUIConfigurationJSON,
							previewFragmentTemplateJSON
						)}
						fragmentTemplateJSON={previewFragmentTemplateJSON}
						uiConfigurationJSON={previewUIConfigurationJSON}
						uiConfigurationValues={getUIConfigurationValues(
							previewUIConfigurationJSON
						)}
					/>
				</ErrorBoundary>
			</div>
		);
	}

	const handleSubmit = (event) => {
		event.preventDefault();

		setIsSubmitting(true);

		const formData = new FormData(form.current);

		try {
			formData.append(
				`${namespace}configuration`,
				JSON.stringify({
					fragmentTemplateJSON: JSON.parse(fragmentTemplateJSON),
					uiConfigurationJSON: JSON.parse(uiConfigurationJSON),
				})
			);
		}
		catch {
			openErrorToast({
				message: Liferay.Language.get('the-json-is-invalid'),
			});

			setIsSubmitting(false);

			return;
		}

		formData.append(`${namespace}type`, blueprintType);
		formData.append(`${namespace}blueprintId`, blueprintId);
		formData.append(`${namespace}redirect`, redirectURL);

		return fetch(submitFormURL, {
			body: formData,
			method: 'POST',
		})
			.then((response) => response.json())
			.then((responseContent) => {
				if (
					Object.prototype.hasOwnProperty.call(
						responseContent,
						'errors'
					)
				) {
					responseContent.errors.forEach((message) =>
						openErrorToast({message})
					);

					setIsSubmitting(false);
				}
				else {
					navigate(redirectURL);
				}
			})
			.catch(() => {
				openErrorToast();

				setIsSubmitting(false);
			});
	};

	return (
		<>
			<form ref={form}>
				<PageToolbar
					initialDescription={initialDescription}
					initialTitle={initialTitle}
					isSubmitting={isSubmitting}
					onCancel={redirectURL}
					onSubmit={handleSubmit}
					toolbarItem={
						<PreviewModal
							body={_renderPreviewBody()}
							size="lg"
							title={Liferay.Language.get(
								'preview-configuration'
							)}
						>
							<ClayButton
								borderless
								displayType="secondary"
								small
							>
								{Liferay.Language.get('preview')}
							</ClayButton>
						</PreviewModal>
					}
				/>
			</form>

			<div className="fragment-row">
				<ClayLayout.Row>
					<ClayLayout.Col size={8}>
						<div className="fragment-section">
							<div className="fragment-header">
								<ClayButton
									className={expand && 'active'}
									disabled={false}
									displayType="secondary"
									monospaced
									onClick={() => setExpand(!expand)}
									small
									title={Liferay.Language.get(
										'predefined-variables'
									)}
									type="submit"
								>
									<ClayIcon symbol="list-ul"></ClayIcon>
								</ClayButton>

								<div className="expand-header">
									<div className="header-label">
										<label>
											{Liferay.Language.get(
												'fragment-template-json'
											)}
											<ClayTooltipProvider>
												<ClaySticker
													className="cursor-pointer"
													displayType="unstyled"
													size="sm"
												>
													<ClayIcon
														data-tooltip-align="top"
														symbol="info-circle"
														title={Liferay.Language.get(
															'fragment-template-json-helptext'
														)}
													/>
												</ClaySticker>
											</ClayTooltipProvider>
										</label>
									</div>
								</div>
							</div>

							<ClayLayout.Row>
								{expand && (
									<ClayLayout.Col
										className="json-section"
										size={3}
									>
										<div className="sidebar sidebar-light">
											<div className="sidebar-header">
												<span className="text-truncate-inline">
													<span className="text-truncate">
														{Liferay.Language.get(
															'predefined-variables'
														)}
													</span>
												</span>
											</div>

											<div className="container-fluid sidebar-input">
												<ClayInput
													aria-label={Liferay.Language.get(
														'search'
													)}
													placeholder={Liferay.Language.get(
														'search'
													)}
													type="text"
												/>
											</div>

											<div className="container-fluid">
												<dl className="sidebar-dl">
													{predefinedVariables.map(
														(item) => (
															<SidebarPanel
																categoryName={
																	item.categoryName
																}
																handleClick={
																	addFragmentTemplateJSONVariable
																}
																item={item}
																key={
																	item.categoryName
																}
																variables={
																	item.variables
																}
															/>
														)
													)}
												</dl>
											</div>
										</div>
									</ClayLayout.Col>
								)}

								<ClayLayout.Col
									className="json-section"
									size={expand ? 9 : 12}
								>
									<CodeMirrorEditor
										onChange={(value) =>
											setFragmentTemplateJSON(value)
										}
										ref={fragmentTemplateJSONRef}
										value={fragmentTemplateJSON}
									/>
								</ClayLayout.Col>
							</ClayLayout.Row>
						</div>
					</ClayLayout.Col>

					<ClayLayout.Col size={4}>
						<div className="fragment-section">
							<div className="fragment-header">
								<div className="expand-header">
									<div className="header-label">
										<label>
											{Liferay.Language.get(
												'ui-configuration-json'
											)}
											<ClayTooltipProvider>
												<ClaySticker
													className="cursor-pointer"
													displayType="unstyled"
													size="sm"
												>
													<ClayIcon
														data-tooltip-align="top"
														symbol="info-circle"
														title={Liferay.Language.get(
															'ui-configuration-json-helptext'
														)}
													/>
												</ClaySticker>
											</ClayTooltipProvider>
										</label>
									</div>
								</div>
							</div>

							<CodeMirrorEditor
								onChange={(value) =>
									setUIConfigurationJSON(value)
								}
								value={uiConfigurationJSON}
							/>
						</div>
					</ClayLayout.Col>
				</ClayLayout.Row>
			</div>
		</>
	);
}

EditFragmentForm.propTypes = {
	initialConfigurationString: PropTypes.string,
	initialDescription: PropTypes.object,
	initialTitle: PropTypes.object,
	predefinedVariables: PropTypes.object,
	redirectURL: PropTypes.string,
	submitFormURL: PropTypes.string,
};

function SidebarPanel({categoryName, handleClick, variables}) {
	const [expand, setExpand] = useState(false);

	return (
		<div>
			<ClayButton
				className="panel-header sidebar-dt"
				displayType="unstyled"
				onClick={() => setExpand(!expand)}
			>
				<span>{categoryName}</span>

				<span className="sidebar-arrow">
					<ClayIcon symbol={expand ? 'angle-down' : 'angle-right'} />
				</span>
			</ClayButton>

			{expand &&
				variables.map((entry) => (
					<dd className="sidebar-dd" key={entry.variable}>
						<ClayButton
							displayType="unstyled"
							onClick={() =>
								handleClick(`"$\{${entry.variable}}"`)
							}
						>
							{entry.name}
						</ClayButton>
					</dd>
				))}
		</div>
	);
}

export default function ({context, props}) {
	return (
		<ThemeContext.Provider value={context}>
			<div className="edit-fragment-root">
				<ErrorBoundary>
					<EditFragmentForm {...props} />
				</ErrorBoundary>
			</div>
		</ThemeContext.Provider>
	);
}
