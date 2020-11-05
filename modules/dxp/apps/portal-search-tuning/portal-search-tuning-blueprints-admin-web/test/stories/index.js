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

import BlueprintForm from '../../src/main/resources/META-INF/resources/js/components/BlueprintForm';
import ConfigFragment from '../../src/main/resources/META-INF/resources/js/components/ConfigFragment';
import Fragment from '../../src/main/resources/META-INF/resources/js/components/Fragment';
import FragmentForm from '../../src/main/resources/META-INF/resources/js/components/FragmentForm';
import PageToolbar from '../../src/main/resources/META-INF/resources/js/components/PageToolbar';
import Sidebar from '../../src/main/resources/META-INF/resources/js/components/Sidebar';
import ErrorBoundary from '../../src/main/resources/META-INF/resources/js/shared/ErrorBoundary';
import QueryBuilder from '../../src/main/resources/META-INF/resources/js/tabs/QueryBuilder';
import {AVAILABLE_LOCALES, SELECTED_FRAGMENTS} from '../js/mocks/data';

const {addDecorator, storiesOf} = StorybookReact;
const {action} = StorybookAddonActions;
const {withKnobs} = StorybookAddonKnobs;

addDecorator(withKnobs);

addDecorator((storyFn) => {
	const context = {
		namespace:
			'_com_liferay_portal_search_tuning_gsearch_configuration_web_internal_portlet_SearchConfigurationAdminPortlet_',
		spritemap: STORYBOOK_CONSTANTS.SPRITEMAP_PATH,
	};

	return (
		<ClayIconSpriteContext.Provider value={context.spritemap}>
			{storyFn()}
		</ClayIconSpriteContext.Provider>
	);
});

const withBlueprintsClass = (storyFn) => (
	<ErrorBoundary>
		<div className="blueprints-admin-root">{storyFn()}</div>
	</ErrorBoundary>
);

const withBuilderClass = (storyFn) => (
	<div className="builder">{storyFn()}</div>
);

const withContainer = (storyFn) => (
	<ClayLayout.ContainerFluid size="md">{storyFn()}</ClayLayout.ContainerFluid>
);

const withFragmentsClass = (storyFn) => (
	<ErrorBoundary>
		<div className="fragment-admin-root">{storyFn()}</div>
	</ErrorBoundary>
);

storiesOf('Pages|FragmentForm', module)
	.addDecorator(withFragmentsClass)
	.add('default', () => (
		<FragmentForm
			originalConfigJSON={SELECTED_FRAGMENTS[0].configJSON}
			originalInputJSON={SELECTED_FRAGMENTS[0].inputJSON}
			redirectURL=""
			submitFormURL=""
		/>
	));

storiesOf('Pages|BlueprintForm', module)
	.addDecorator(withBlueprintsClass)
	.add('default', () => (
		<BlueprintForm
			availableLocales={AVAILABLE_LOCALES}
			blueprintId="0"
			blueprintType={0}
			entityJSON={{
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
			}}
			initialTitle={{
				'en-US': 'Test Title',
			}}
			redirectURL=""
			submitFormURL=""
		/>
	));

storiesOf('Components|PageToolbar', module)
	.addDecorator(withBlueprintsClass)
	.add('PageToolbar', () => (
		<PageToolbar
			availableLocales={AVAILABLE_LOCALES}
			initialTitle={{
				'en-US': 'Test Title',
			}}
			onCancel=""
			onPublish={action('onPublish')}
			tab={'query-builder'}
			tabs={{
				'query-builder': Liferay.Language.get('query-builder'),
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
			updateFragments={action('updateFragments')}
		/>
	));

storiesOf('Components|Fragment', module)
	.addDecorator(withBlueprintsClass)
	.addDecorator(withBuilderClass)
	.addDecorator(withContainer)
	.add('Fragment', () => (
		<Fragment
			deleteFragment={action('deleteFragment')}
			description={SELECTED_FRAGMENTS[0].inputJSON.description}
			disabled={false}
			icon={SELECTED_FRAGMENTS[0].inputJSON.icon}
			jsonString={SELECTED_FRAGMENTS[0].inputJSON.jsonString}
			title={SELECTED_FRAGMENTS[0].inputJSON.title}
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
