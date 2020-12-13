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
import ClayButton, {ClayButtonWithIcon} from '@clayui/button';
import {ClayInput} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import ClayModal, {useModal} from '@clayui/modal';
import {useIsMounted} from 'frontend-js-react-web';
import {fetch, navigate} from 'frontend-js-web';
import React, {useRef, useState} from 'react';

const VALID_EXTENSIONS = '.json';

/**
 * A slightly modified version of frontend-js-web module's SimpleInputModal
 * React component to include a description field.
 */
const ImportBlueprintModal = ({
	alert,
	closeModal,
	dialogTitle,
	formSubmitURL,
	initialVisible,
	namespace,
	onFormSuccess,
	submitButtonLabel = Liferay.Language.get('import'),
}) => {
	const [importFile, setImportFile] = useState();
	const isMounted = useIsMounted();
	const [errorMessage, setErrorMessage] = useState();
	const [loadingResponse, setLoadingResponse] = useState(false);
	const [visible, setVisible] = useState(initialVisible);

	const inputFileRef = useRef();

	const handleFormError = (responseContent) => {
		setErrorMessage(responseContent.error || '');
	};

	const _handleSubmit = (event) => {
		event.preventDefault();

		const formData = new FormData(
			document.querySelector(`#${namespace}form`)
		);

		fetch(formSubmitURL, {
			body: formData,
			method: 'POST',
		})
			.then((response) => response.json())
			.then((responseContent) => {
				if (isMounted()) {
					if (responseContent.error) {
						setLoadingResponse(false);

						handleFormError(responseContent);
					}
					else {
						setVisible(false);

						closeModal();

						if (responseContent.redirectURL) {
							navigate(responseContent.redirectURL);
						}
						else {
							if (onFormSuccess) {
								onFormSuccess({
									...responseContent,
									redirectURL:
										responseContent.redirectURL || '',
								});
							}
						}
					}
				}
			})
			.catch((response) => {
				handleFormError(response);
			});

		setLoadingResponse(true);
	};

	const {observer, onClose} = useModal({
		onClose: () => {
			setVisible(false);

			closeModal();
		},
	});

	return (
		visible && (
			<ClayModal observer={observer} size="md">
				<ClayModal.Header>{dialogTitle}</ClayModal.Header>

				<form id={`${namespace}form`} onSubmit={_handleSubmit}>
					<ClayModal.Body>
						{alert && alert.message && alert.title && (
							<ClayAlert
								displayType={alert.style}
								title={alert.title}
							>
								{alert.message}
							</ClayAlert>
						)}

						<div
							className={`form-group ${
								errorMessage ? 'has-error' : ''
							}`}
						>
							{errorMessage && (
								<div className="form-feedback-item">
									<ClayIcon
										className="inline-item inline-item-before"
										symbol="exclamation-full"
									/>

									{errorMessage}
								</div>
							)}
						</div>

						<ClayInput
							accept={VALID_EXTENSIONS}
							className="d-none"
							name="file"
							onChange={(e) => {
								setImportFile(e.target.files[0]);
							}}
							ref={inputFileRef}
							type="file"
						/>

						{!importFile && (
							<ClayButton
								displayType="secondary"
								onClick={() => {
									inputFileRef.current.click();
								}}
							>
								{Liferay.Language.get('select-file')}
							</ClayButton>
						)}

						{importFile && (
							<>
								<strong>{importFile.name}</strong>

								<ClayButtonWithIcon
									displayType="unstyled"
									onClick={() => {
										setImportFile(null);
									}}
									symbol="times-circle"
									title={Liferay.Language.get('delete')}
								/>
							</>
						)}
					</ClayModal.Body>

					<ClayModal.Footer
						last={
							<ClayButton.Group spaced>
								<ClayButton
									disabled={loadingResponse}
									displayType="secondary"
									onClick={onClose}
								>
									{Liferay.Language.get('cancel')}
								</ClayButton>

								<ClayButton
									disabled={loadingResponse}
									displayType="primary"
									type="submit"
								>
									{loadingResponse && (
										<span className="inline-item inline-item-before">
											<span
												aria-hidden="true"
												className="loading-animation"
											></span>
										</span>
									)}

									{submitButtonLabel}
								</ClayButton>
							</ClayButton.Group>
						}
					/>
				</form>
			</ClayModal>
		)
	);
};

export default ImportBlueprintModal;
