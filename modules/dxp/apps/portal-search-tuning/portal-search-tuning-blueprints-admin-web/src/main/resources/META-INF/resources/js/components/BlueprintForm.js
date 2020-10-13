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

import {fetch, navigate} from 'frontend-js-web';
import {PropTypes} from 'prop-types';
import React, {useCallback, useContext, useRef, useState} from 'react';

import ThemeContext from '../ThemeContext';
import {DEFAULT_FRAGMENT} from '../utils/data';
import {convertSelectedFragment, openErrorToast} from '../utils/utils';
import Builder from './Builder';
import PageToolbar from './PageToolbar';
import Sidebar from './Sidebar';

function BlueprintForm({
	blueprintId,
	blueprintType,
	entityJSON,
	initialDescription = {},
	initialSelectedFragments,
	initialTitle = {},
	redirectURL = '',
	submitFormURL = '',
}) {
	const {namespace} = useContext(ThemeContext);

	const [showSidebar] = useState(true);

	const form = useRef();

	const fragmentIdCounter = useRef(1); // 0 reserved for default fragment

	const [isSubmitting, setIsSubmitting] = useState(false);

	const [selectedFragments, setSelectedFragments] = useState(
		blueprintId !== '0'
			? initialSelectedFragments.map((configString) => ({
					...JSON.parse(configString),
					id: fragmentIdCounter.current++,
			  }))
			: [convertSelectedFragment(DEFAULT_FRAGMENT)]
	);

	const onAddFragment = useCallback((fragment) => {
		setSelectedFragments((selectedFragments) => [
			convertSelectedFragment(fragment, fragmentIdCounter.current++),
			...selectedFragments,
		]);
	}, []);

	const deleteFragment = useCallback((id) => {
		setSelectedFragments((selectedFragments) =>
			selectedFragments.filter((item) => item.id !== id)
		);
	}, []);

	const handleSubmit = useCallback(
		(event) => {
			event.preventDefault();

			setIsSubmitting(true);

			const formData = new FormData(form.current);

			try {
				formData.append(
					`${namespace}queryConfiguration`,
					JSON.stringify(
						selectedFragments.map((item) => item.queryConfig)
					)
				);

				formData.append(
					`${namespace}selectedFragments`,
					JSON.stringify(
						selectedFragments.map((item) => ({
							configJSON: item.configJSON,
							configValues: item.configValues,
							inputJSON: item.inputJSON,
							queryConfig: item.queryConfig,
						}))
					)
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
		},
		[
			blueprintId,
			blueprintType,
			selectedFragments,
			namespace,
			redirectURL,
			submitFormURL,
		]
	);

	const updateFragment = useCallback((index, configs) => {
		setSelectedFragments((selectedFragments) => [
			...selectedFragments.slice(0, index),
			configs,
			...selectedFragments.slice(index + 1),
		]);
	}, []);

	return (
		<form ref={form}>
			<PageToolbar
				initialDescription={initialDescription}
				initialTitle={initialTitle}
				isSubmitting={isSubmitting}
				onCancel={redirectURL}
				onSubmit={handleSubmit}
			/>

			{showSidebar && <Sidebar onAddFragment={onAddFragment} />}

			<div className={`${showSidebar ? 'shifted' : ''}`}>
				<Builder
					deleteFragment={deleteFragment}
					entityJSON={entityJSON}
					selectedFragments={selectedFragments}
					updateFragment={updateFragment}
				/>
			</div>
		</form>
	);
}

BlueprintForm.propTypes = {
	blueprintId: PropTypes.string,
	blueprintType: PropTypes.number,
	entityJSON: PropTypes.object,
	initialDescription: PropTypes.object,
	initialSelectedFragments: PropTypes.arrayOf(PropTypes.string),
	initialTitle: PropTypes.object,
	redirectURL: PropTypes.string,
	submitFormURL: PropTypes.string,
};

export default React.memo(BlueprintForm);