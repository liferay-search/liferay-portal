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

import {ClayIconSpriteContext} from '@clayui/icon';
import {
	STORYBOOK_CONSTANTS,
	StorybookAddonActions,
	StorybookAddonKnobs,
	StorybookReact,
} from '@liferay/npm-scripts/src/storybook';
import React from 'react';

import '../../src/main/resources/META-INF/resources/css/main.scss';

import ClayLayout from '@clayui/layout';

import BlueprintForm from '../../src/main/resources/META-INF/resources/js/edit_blueprint';
import Sidebar from '../../src/main/resources/META-INF/resources/js/edit_blueprint/Sidebar';
import QueryBuilder from '../../src/main/resources/META-INF/resources/js/edit_blueprint/tabs/QueryBuilder';
import FragmentForm from '../../src/main/resources/META-INF/resources/js/edit_fragment';
import ConfigFragment from '../../src/main/resources/META-INF/resources/js/shared/ConfigFragment';
import ErrorBoundary from '../../src/main/resources/META-INF/resources/js/shared/ErrorBoundary';
import JSONFragment from '../../src/main/resources/META-INF/resources/js/shared/JSONFragment';
import PageToolbar from '../../src/main/resources/META-INF/resources/js/shared/PageToolbar';
import SearchInput from '../../src/main/resources/META-INF/resources/js/shared/SearchInput';
import {
	DEFAULT_EDIT_FRAGMENT,
	DEFAULT_FRAGMENT,
} from '../../src/main/resources/META-INF/resources/js/utils/data';
import {convertToSelectedFragment} from '../../src/main/resources/META-INF/resources/js/utils/utils';
import AddBlueprintModal from '../../src/main/resources/META-INF/resources/js/view_blueprints/AddBlueprintModal';
import {SELECTED_FRAGMENTS} from '../js/mocks/data';

const {addDecorator, storiesOf} = StorybookReact;
const {action} = StorybookAddonActions;
const {withKnobs} = StorybookAddonKnobs;

const CONTEXT = {
	availableLanguages: {
		ar_SA: 'Arabic (Saudi Arabia)',
		ca_ES: 'Catalan (Spain)',
		en_US: 'English (United States)',
		nl_NL: 'Dutch (Netherlands)',
		zh_CN: 'Chinese (China)',
	},
	contextPath: '/o/portal-search-tuning-blueprints-admin-web',
	defaultLocale: 'en_US',
	locale: 'en_US',
	namespace:
		'_com_liferay_portal_search_tuning_gsearch_configuration_web_internal_portlet_SearchConfigurationAdminPortlet_',
	spritemap: STORYBOOK_CONSTANTS.SPRITEMAP_PATH,
};

const ENTITY_JSON = {
	'com.liferay.asset.kernel.model.AssetTag': {
		multiple: false,
		title: 'Select Tag',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.Group': {
		multiple: false,
		title: 'Select Site',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.Organization': {
		multiple: true,
		title: 'Select Organization',
		url: 'http:/…',
	},
	'com.liferay.portal.kernel.model.Role': {
		multiple: false,
		title: 'Select Role',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.Team': {
		multiple: false,
		title: 'Select Team',
		url: 'http:…',
	},
	'com.liferay.portal.kernel.model.User': {
		multiple: true,
		title: 'Select User',
		url: 'http:/…',
	},
	'com.liferay.portal.kernel.model.UserGroup': {
		multiple: false,
		title: 'Select User Group',
		url: 'http:…',
	},
};

addDecorator(withKnobs);

addDecorator((storyFn) => {
	return (
		<div className="portlet-blueprints-admin">
			<ClayIconSpriteContext.Provider value={CONTEXT.spritemap}>
				{storyFn()}
			</ClayIconSpriteContext.Provider>
		</div>
	);
});

const withBlueprintsClass = (storyFn) => (
	<ErrorBoundary>
		<div className="edit-blueprint-root">{storyFn()}</div>
	</ErrorBoundary>
);

const withBuilderClass = (storyFn) => (
	<div className="builder">{storyFn()}</div>
);

const withContainer = (storyFn) => (
	<ClayLayout.ContainerFluid size="md" view>
		{storyFn()}
	</ClayLayout.ContainerFluid>
);

storiesOf('Pages|BlueprintForm', module)
	.add('default', () => (
		<BlueprintForm
			context={CONTEXT}
			props={{
				blueprintId: '1',
				blueprintType: 0,
				entityJSON: ENTITY_JSON,
				initialDescription: {},
				initialSelectedFragmentsString: JSON.stringify({
					query_configuration: SELECTED_FRAGMENTS,
				}),
				initialTitle: {
					'en-US': 'Test Title',
				},
				redirectURL: '',
				submitFormURL: '',
			}}
		/>
	))
	.add('empty', () => (
		<BlueprintForm
			context={CONTEXT}
			props={{
				blueprintId: '1',
				blueprintType: 0,
				entityJSON: ENTITY_JSON,
				initialDescription: {},
				initialSelectedFragmentsString: JSON.stringify({
					query_configuration: [],
				}),
				initialTitle: {
					'en-US': 'Test Title',
				},
				redirectURL: '',
				submitFormURL: '',
			}}
		/>
	))
	.add('default fragment', () => (
		<BlueprintForm
			context={CONTEXT}
			props={{
				blueprintId: '1',
				blueprintType: 0,
				entityJSON: ENTITY_JSON,
				initialDescription: {},
				initialSelectedFragmentsString: JSON.stringify({
					query_configuration: [
						convertToSelectedFragment(DEFAULT_FRAGMENT, 1),
					],
				}),
				initialTitle: {
					'en-US': 'Test Title',
				},
				redirectURL: '',
				submitFormURL: '',
			}}
		/>
	));

storiesOf('Pages|FragmentForm', module).add('default', () => (
	<FragmentForm
		context={CONTEXT}
		props={{
			initialConfigurationString: JSON.stringify(DEFAULT_EDIT_FRAGMENT),
			initialDescription: {'en-US': 'Description'},
			initialTitle: {
				'en-US': 'Test Title',
			},
			predefinedVariables: [
				{
					categoryName: 'User',
					parameterDefinitions: [
						{
							className:
								'com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter',
							description: "User's ID",
							variable: '${user.user_id}',
						},
						{
							className:
								'com.liferay.portal.search.tuning.blueprints.engine.parameter.StringParameter',
							description: "User's First Name",
							variable: '${user.user_first_name}',
						},
					],
				},
				{
					categoryName: 'Context',
					parameterDefinitions: [
						{
							description: 'Company ID',
							variable: '${context.company_id}',
						},
						{
							description: 'Scope Group ID',
							variable: '${context.scope_group_id}',
						},
					],
				},
			],
			redirectURL: '',
			submitFormURL: '',
		}}
	/>
));

storiesOf('Components|AddBlueprintModal', module)
	.addDecorator(withBlueprintsClass)
	.add('AddBlueprintModal', () => (
		<AddBlueprintModal
			contextPath="/o/portal-search-tuning-blueprints-admin-web/"
			dialogTitle="New Search Blueprint"
			initialVisible
		/>
	));

storiesOf('Components|ConfigFragment', module)
	.addDecorator(withBlueprintsClass)
	.addDecorator(withBuilderClass)
	.addDecorator(withContainer)
	.add('ConfigFragment', () => (
		<ConfigFragment
			deleteFragment={action('deleteFragment')}
			fragmentTemplateJSON={SELECTED_FRAGMENTS[0].fragmentTemplateJSON}
			uiConfigurationJSON={SELECTED_FRAGMENTS[0].uiConfigurationJSON}
			uiConfigurationValues={SELECTED_FRAGMENTS[0].uiConfigurationValues}
			updateFragment={action('updateFragment')}
		/>
	));

storiesOf('Components|Fragment', module)
	.addDecorator(withBlueprintsClass)
	.addDecorator(withBuilderClass)
	.addDecorator(withContainer)
	.add('JSONFragment', () => (
		<JSONFragment
			deleteFragment={action('deleteFragment')}
			description={SELECTED_FRAGMENTS[0].fragmentTemplateJSON.description}
			disabled={false}
			fragmentTemplateJSON={SELECTED_FRAGMENTS[0].fragmentTemplateJSON}
			icon={SELECTED_FRAGMENTS[0].fragmentTemplateJSON.icon}
			title={SELECTED_FRAGMENTS[0].fragmentTemplateJSON.title}
			updateJSON={() => {}}
		/>
	));

storiesOf('Components|PageToolbar', module)
	.addDecorator(withBlueprintsClass)
	.add('PageToolbar', () => (
		<PageToolbar
			initialTitle={{
				'en-US': 'Test Title',
			}}
			onCancel=""
			onPublish={action('onPublish')}
			tab={'query-builder'}
			tabs={{
				'query-builder': 'query-builder',
			}}
		/>
	));

storiesOf('Components|QueryBuilder', module)
	.addDecorator(withBlueprintsClass)
	.addDecorator(withContainer)
	.add('QueryBuilder', () => (
		<QueryBuilder
			deleteFragment={action('buildFragment')}
			selectedFragments={SELECTED_FRAGMENTS}
			updateFragment={action('updateFragment')}
		/>
	));

storiesOf('Components|SearchInput', module)
	.addDecorator(withBlueprintsClass)
	.add('SearchInput', () => (
		<SearchInput
			onChange={action('onChange')}
			onSubmit={action('onSubmit')}
		/>
	));

storiesOf('Components|Sidebar', module)
	.addDecorator(withBlueprintsClass)
	.add('Sidebar', () => (
		<Sidebar
			addFragment={action('addFragment')}
			queryFragments={SELECTED_FRAGMENTS}
		/>
	));
