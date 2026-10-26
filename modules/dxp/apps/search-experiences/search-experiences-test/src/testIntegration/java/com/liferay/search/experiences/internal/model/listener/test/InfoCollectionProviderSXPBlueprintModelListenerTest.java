/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.journal.model.JournalArticle;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.search.experiences.model.SXPBlueprint;
import com.liferay.search.experiences.service.SXPBlueprintLocalService;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jorge Avalos
 */
@RunWith(Arquillian.class)
public class InfoCollectionProviderSXPBlueprintModelListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_sxpBlueprint = _sxpBlueprintLocalService.addSXPBlueprint(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			JSONUtil.put(
				"generalConfiguration",
				JSONUtil.put(
					"collectionProvider", true
				).put(
					"collectionProviderType", JournalArticle.class.getName()
				).put(
					"searchableAssetTypes",
					JSONUtil.put(JournalArticle.class.getName())
				)
			).toString(),
			null, null, StringPool.BLANK,
			Collections.singletonMap(
				LocaleUtil.US, RandomTestUtil.randomString()),
			ServiceContextTestUtil.getServiceContext());

		_copiedSXPBlueprint = _sxpBlueprintLocalService.createSXPBlueprint(
			_sxpBlueprint.getSXPBlueprintId());

		_copiedSXPBlueprint.setExternalReferenceCode(
			_sxpBlueprint.getExternalReferenceCode());
		_copiedSXPBlueprint.setCompanyId(RandomTestUtil.randomLong());
		_copiedSXPBlueprint.setConfigurationJSON(
			_sxpBlueprint.getConfigurationJSON());
	}

	@Test
	public void testOnAfterCreate() throws Exception {
		_modelListener.onAfterCreate(_copiedSXPBlueprint);

		try {
			Assert.assertNotNull(
				_getInfoCollectionProvider(_copiedSXPBlueprint));
			Assert.assertNotNull(_getInfoCollectionProvider(_sxpBlueprint));
		}
		finally {
			_modelListener.onAfterRemove(_copiedSXPBlueprint);
		}
	}

	@Test
	public void testOnAfterRemove() throws Exception {
		_modelListener.onAfterCreate(_copiedSXPBlueprint);

		_modelListener.onAfterRemove(_copiedSXPBlueprint);

		Assert.assertNull(_getInfoCollectionProvider(_copiedSXPBlueprint));
		Assert.assertNotNull(_getInfoCollectionProvider(_sxpBlueprint));
	}

	private InfoCollectionProvider<?> _getInfoCollectionProvider(
		SXPBlueprint sxpBlueprint) {

		return _infoItemServiceRegistry.getInfoItemService(
			InfoCollectionProvider.class,
			StringBundler.concat(
				SXPBlueprint.class.getName(), StringPool.UNDERLINE,
				sxpBlueprint.getCompanyId(), StringPool.UNDERLINE,
				sxpBlueprint.getExternalReferenceCode()));
	}

	private SXPBlueprint _copiedSXPBlueprint;

	@Inject
	private InfoItemServiceRegistry _infoItemServiceRegistry;

	@Inject(
		filter = "component.name=com.liferay.search.experiences.internal.model.listener.InfoCollectionProviderSXPBlueprintModelListener"
	)
	private ModelListener<SXPBlueprint> _modelListener;

	@DeleteAfterTestRun
	private SXPBlueprint _sxpBlueprint;

	@Inject
	private SXPBlueprintLocalService _sxpBlueprintLocalService;

}