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
import ClayDropDown from '@clayui/drop-down';
import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import {PropTypes} from 'prop-types';
import React, {useContext, useEffect, useMemo, useState} from 'react';

import CodeMirrorEditor from './CodeMirrorEditor';
import ThemeContext from './ThemeContext';

function JSONFragment({
	collapseAll,
	deleteFragment,
	fragmentTemplateJSON,
	id,
	updateFragment,
}) {
	const {locale} = useContext(ThemeContext);

	const [active, setActive] = useState(false);
	const [collapse, setCollapse] = useState(collapseAll);

	useEffect(() => {
		setCollapse(collapseAll);
	}, [collapseAll]);

	function handleChange(value) {
		try {
			updateFragment(JSON.parse(value));
		}
		catch {}
	}

	return (
		<div className="configuration-fragment-sheet sheet">
			{useMemo(() => {
				return (
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
										{fragmentTemplateJSON.description[
											locale
										] || fragmentTemplateJSON.description}
									</ClayList.ItemText>
								)}
							</ClayList.ItemField>

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
									<ClayDropDown.Item
										onClick={() => deleteFragment(id)}
									>
										{Liferay.Language.get('remove')}
									</ClayDropDown.Item>
								</ClayDropDown.ItemList>
							</ClayDropDown>
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
											!collapse
												? 'angle-down'
												: 'angle-right'
										}
									/>
								</ClayButton>
							</ClayList.ItemField>
						</ClayList.Item>
					</ClayList>
				);
			}, [
				active,
				collapse,
				deleteFragment,
				id,
				fragmentTemplateJSON,
				locale,
			])}

			{!collapse && (
				<div className="json-configuration-editor">
					<label>{Liferay.Language.get('json')}</label>

					<CodeMirrorEditor
						onChange={handleChange}
						value={JSON.stringify(fragmentTemplateJSON, null, '\t')}
					/>
				</div>
			)}
		</div>
	);
}

JSONFragment.propTypes = {
	collapseAll: PropTypes.bool,
	deleteFragment: PropTypes.func,
	fragmentTemplateJSON: PropTypes.object,
	icon: PropTypes.string,
	id: PropTypes.number,
	updateFragment: PropTypes.func,
};

export default React.memo(JSONFragment);
