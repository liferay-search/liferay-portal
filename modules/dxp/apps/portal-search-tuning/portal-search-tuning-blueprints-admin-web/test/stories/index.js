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
import {SELECTED_FRAGMENTS} from '../js/mocks/data';

const {addDecorator, storiesOf} = StorybookReact;
const {action} = StorybookAddonActions;
const {withKnobs} = StorybookAddonKnobs;

const CONTEXT = {
	defaultLocale: 'en_US',
	locale: 'en_US',
	namespace:
		'_com_liferay_portal_search_tuning_gsearch_configuration_web_internal_portlet_SearchConfigurationAdminPortlet_',
	spritemap: STORYBOOK_CONSTANTS.SPRITEMAP_PATH,
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

const withFragmentsClass = (storyFn) => (
	<ErrorBoundary>
		<div className="edit-fragment-root">{storyFn()}</div>
	</ErrorBoundary>
);

storiesOf('Pages|FragmentForm', module)
	.addDecorator(withFragmentsClass)
	.add('default', () => (
		<FragmentForm
			context={CONTEXT}
			props={{
				initialConfigurationString: JSON.stringify({
					fragmentTemplate: SELECTED_FRAGMENTS[0].inputJSON,
					uiConfiguration: SELECTED_FRAGMENTS[0].configJSON,
				}),
				initialDescription: {},
				initialTitle: {
					'en-US': 'Test Title',
				},
				redirectURL: '',
				submitFormURL: '',
			}}
		/>
	));

storiesOf('Pages|BlueprintForm', module).add('default', () => (
	<BlueprintForm
		context={CONTEXT}
		props={{
			blueprintId: '1',
			blueprintType: 0,
			entityJSON: {
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
			},
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

storiesOf('Components|Sidebar', module)
	.addDecorator(withBlueprintsClass)
	.add('Sidebar', () => (
		<Sidebar
			addFragment={action('addFragment')}
			queryFragments={SELECTED_FRAGMENTS}
		/>
	));

storiesOf('Components|Builder', module)
	.addDecorator(withBlueprintsClass)
	.addDecorator(withContainer)
	.add('Builder', () => (
		<QueryBuilder
			deleteFragment={action('buildFragment')}
			selectedFragments={SELECTED_FRAGMENTS}
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
			description={SELECTED_FRAGMENTS[0].inputJSON.description}
			disabled={false}
			icon={SELECTED_FRAGMENTS[0].inputJSON.icon}
			jsonString={SELECTED_FRAGMENTS[0].inputJSON.jsonString}
			title={SELECTED_FRAGMENTS[0].inputJSON.title}
			updateJSON={() => {}}
		/>
	));

storiesOf('Components|ConfigFragment', module)
	.addDecorator(withBlueprintsClass)
	.addDecorator(withBuilderClass)
	.addDecorator(withContainer)
	.add('ConfigFragment', () => (
		<ConfigFragment
			configJSON={SELECTED_FRAGMENTS[0].configJSON}
			configValues={SELECTED_FRAGMENTS[0].configValues}
			deleteFragment={action('deleteFragment')}
			inputJSON={SELECTED_FRAGMENTS[0].inputJSON}
			updateFragment={action('updateFragment')}
		/>
	));
