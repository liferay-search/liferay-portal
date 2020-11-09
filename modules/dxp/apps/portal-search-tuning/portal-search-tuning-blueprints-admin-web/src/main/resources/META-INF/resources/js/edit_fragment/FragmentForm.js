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
import ClayLink from '@clayui/link';
import ClaySticker from '@clayui/sticker';
import ClayToolbar from '@clayui/toolbar';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {fetch, navigate} from 'frontend-js-web';
import {PropTypes} from 'prop-types';
import React, {useContext, useRef, useState} from 'react';

import CodeMirrorEditor from '../shared/CodeMirrorEditor';
import ConfigFragment from '../shared/ConfigFragment';
import PreviewModal from '../shared/PreviewModal';
import ThemeContext from '../shared/ThemeContext';
import {PREDEFINED_VARIABLES} from '../utils/data';
import {
	getConfigValues,
	openErrorToast,
	replaceConfigValues,
} from '../utils/utils';

export default function FragmentForm({
	originalConfigJSON,
	originalInputJSON,
	predefinedVariables = PREDEFINED_VARIABLES,
	redirectURL,
	submitFormURL,
}) {
	const {namespace} = useContext(ThemeContext);

	const form = useRef();
	const inputJSONRef = useRef();

	const [isSubmitting, setIsSubmitting] = useState(false);
	const [expand, setExpand] = useState(false);

	const [inputJSON, setInputJSON] = useState(
		JSON.stringify(originalInputJSON, null, '\t')
	);
	const [configJSON, setConfigJSON] = useState(
		JSON.stringify(originalConfigJSON, null, '\t')
	);

	function addInputJSONVariable(variable) {
		var doc = inputJSONRef.current.getDoc();
		var cursor = doc.getCursor();

		doc.replaceRange(variable, cursor);
	}

	function _renderPreviewBody() {
		let previewInputJSON = {};
		let previewConfigJSON = {};

		try {
			previewInputJSON = JSON.parse(inputJSON);
			previewConfigJSON = JSON.parse(configJSON);
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
			<div className="blueprints-admin-root">
				<ConfigFragment
					collapseAll={false}
					configJSON={previewConfigJSON}
					configValues={getConfigValues(previewConfigJSON)}
					inputJSON={previewInputJSON}
					queryConfig={replaceConfigValues(
						previewConfigJSON,
						previewInputJSON
					)}
				/>
			</div>
		);
	}

	const handleSubmit = (event) => {
		event.preventDefault();

		setIsSubmitting(true);

		const formData = new FormData(form.current);

		try {
			formData.append(`${namespace}configuration`, {
				configuration: inputJSON.replaceAll(/\n|\t/g, ''),
				fragmentConfiguration: configJSON.replaceAll(/\n|\t/g, ''),
			});
		}
		catch {
			openErrorToast({
				message: Liferay.Language.get('the-json-is-invalid'),
			});

			setIsSubmitting(false);

			return;
		}

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
			<ClayToolbar light>
				<ClayLayout.ContainerFluid>
					<ClayToolbar.Nav>
						<ClayToolbar.Item
							className="text-left"
							expand
						></ClayToolbar.Item>

						<ClayToolbar.Item>
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
						</ClayToolbar.Item>
						<ClayToolbar.Item>
							<ClayLink
								displayType="secondary"
								href={redirectURL}
								outline="secondary"
							>
								{Liferay.Language.get('cancel')}
							</ClayLink>
						</ClayToolbar.Item>

						<ClayToolbar.Item>
							<ClayButton
								disabled={isSubmitting}
								onClick={handleSubmit}
								small
								type="submit"
							>
								{Liferay.Language.get('save')}
							</ClayButton>
						</ClayToolbar.Item>
					</ClayToolbar.Nav>
				</ClayLayout.ContainerFluid>
			</ClayToolbar>

			<ClayLayout.Row>
				<ClayLayout.Col
					className="fragment-section"
					size={expand ? 7 : 6}
				>
					<div className="fragment-header">
						<ClayButton
							className={expand && 'active'}
							disabled={false}
							displayType="secondary"
							monospaced
							onClick={() => setExpand(!expand)}
							small
							type="submit"
						>
							<ClayIcon symbol="web-content"></ClayIcon>
						</ClayButton>

						<div className="expand-header">
							<div className="header-label">
								<label>
									{Liferay.Language.get('json')}
									<ClayTooltipProvider>
										<ClaySticker
											displayType="unstyled"
											size="sm"
										>
											<ClayIcon
												data-tooltip-align="top"
												symbol="info-circle"
												title={Liferay.Language.get(
													'json'
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
							<ClayLayout.Col className="json-section" size={4}>
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
											{predefinedVariables.map((item) => (
												<div key={item.categoryName}>
													<dt className="sidebar-dt">
														{item.categoryName}
													</dt>

													{item.variables.map(
														(entry) => (
															<dd
																className="sidebar-dd"
																key={
																	entry.variable
																}
															>
																<ClayButton
																	displayType="unstyled"
																	onClick={() =>
																		addInputJSONVariable(
																			`"$\{${entry.variable}}"`
																		)
																	}
																>
																	{entry.name}
																</ClayButton>
															</dd>
														)
													)}
												</div>
											))}
										</dl>
									</div>
								</div>
							</ClayLayout.Col>
						)}

						<ClayLayout.Col
							className="json-section"
							size={expand ? 8 : 12}
						>
							<CodeMirrorEditor
								onChange={(value) => setInputJSON(value)}
								ref={inputJSONRef}
								value={inputJSON}
							/>
						</ClayLayout.Col>
					</ClayLayout.Row>
				</ClayLayout.Col>

				<ClayLayout.Col
					className="fragment-section"
					size={expand ? 5 : 6}
				>
					<div className="fragment-header">
						<div className="expand-header">
							<div className="header-label">
								<label>
									{Liferay.Language.get('ui-configuration')}
									<ClayTooltipProvider>
										<ClaySticker
											displayType="unstyled"
											size="sm"
										>
											<ClayIcon
												data-tooltip-align="top"
												symbol="info-circle"
												title={Liferay.Language.get(
													'ui-configuration'
												)}
											/>
										</ClaySticker>
									</ClayTooltipProvider>
								</label>
							</div>
						</div>
					</div>

					<CodeMirrorEditor
						onChange={(value) => setConfigJSON(value)}
						value={configJSON}
					/>
				</ClayLayout.Col>
			</ClayLayout.Row>
		</>
	);
}

FragmentForm.propTypes = {
	originalConfigJSON: PropTypes.object,
	originalInputJSON: PropTypes.object,
	predefinedVariables: PropTypes.object,
	redirectURL: PropTypes.string,
	submitFormURL: PropTypes.string,
};
