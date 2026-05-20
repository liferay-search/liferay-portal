/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.delivery.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.delivery.client.dto.v1_0.ContentSetProvider;
import com.liferay.headless.delivery.client.pagination.Page;
import com.liferay.headless.delivery.client.pagination.Pagination;
import com.liferay.info.collection.provider.CollectionQuery;
import com.liferay.info.collection.provider.InfoCollectionProvider;
import com.liferay.info.collection.provider.SingleFormVariationInfoCollectionProvider;
import com.liferay.info.pagination.InfoPage;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Luis Ortiz
 */
@RunWith(Arquillian.class)
public class ContentSetProviderResourceTest
	extends BaseContentSetProviderResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		Bundle bundle = FrameworkUtil.getBundle(
			ContentSetProviderResourceTest.class);

		_bundleContext = bundle.getBundleContext();

		_serviceRegistrations = new ArrayList<>();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		for (ServiceRegistration<InfoCollectionProvider<?>>
				serviceRegistration : _serviceRegistrations) {

			serviceRegistration.unregister();
		}

		super.tearDown();
	}

	@Ignore(
		"Base test assumes the irrelevant-filter count equals the no-filter " +
			"count; that holds for per-site entity endpoints but not for a " +
				"global OSGi registry where filtering by itemType reduces the " +
					"count to zero while a null filter returns every registered " +
						"provider."
	)
	@Override
	@Test
	public void testGetAssetLibraryContentSetProvidersPage() throws Exception {
	}

	@Ignore(
		"Base test assumes the irrelevant-filter count equals the no-filter " +
			"count; that holds for per-site entity endpoints but not for a " +
				"global OSGi registry where filtering by itemType reduces the " +
					"count to zero while a null filter returns every registered " +
						"provider."
	)
	@Override
	@Test
	public void testGetSiteContentSetProvidersPage() throws Exception {
	}

	@Test
	public void testGetSiteContentSetProvidersPageExcludesUnavailableProviders()
		throws Exception {

		String availableKey = "available-" + RandomTestUtil.randomString();
		String unavailableKey = "unavailable-" + RandomTestUtil.randomString();

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				availableKey, "Available Provider", Object.class.getName(),
				null, true));
		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				unavailableKey, "Unavailable Provider", Object.class.getName(),
				null, false));

		Page<ContentSetProvider> page =
			contentSetProviderResource.getSiteContentSetProvidersPage(
				testGroup.getGroupId(), null, null, Pagination.of(1, 500));

		List<ContentSetProvider> contentSetProviders =
			(List<ContentSetProvider>)page.getItems();

		Assert.assertTrue(_containsKey(contentSetProviders, availableKey));
		Assert.assertFalse(_containsKey(contentSetProviders, unavailableKey));
	}

	@Test
	public void testGetSiteContentSetProvidersPageOrderedByLabel()
		throws Exception {

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				"zzz-last", "ZZZ Last Provider", Object.class.getName(), null,
				true));
		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				"aaa-first", "AAA First Provider", Object.class.getName(), null,
				true));

		Page<ContentSetProvider> page =
			contentSetProviderResource.getSiteContentSetProvidersPage(
				testGroup.getGroupId(), null, null, Pagination.of(1, 500));

		List<ContentSetProvider> contentSetProviders =
			(List<ContentSetProvider>)page.getItems();

		int firstIndex = _indexOfKey(contentSetProviders, "aaa-first");
		int lastIndex = _indexOfKey(contentSetProviders, "zzz-last");

		Assert.assertTrue(firstIndex >= 0);
		Assert.assertTrue(lastIndex >= 0);
		Assert.assertTrue(firstIndex < lastIndex);
	}

	@Test
	public void testGetSiteContentSetProvidersPageWithItemSubtype()
		throws Exception {

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				"with-subtype", "With Subtype", Object.class.getName(),
				"my-form-variation", true));

		Page<ContentSetProvider> page =
			contentSetProviderResource.getSiteContentSetProvidersPage(
				testGroup.getGroupId(), null, null, Pagination.of(1, 500));

		ContentSetProvider contentSetProvider = _findByKey(
			(List<ContentSetProvider>)page.getItems(), "with-subtype");

		Assert.assertNotNull(contentSetProvider);
		Assert.assertEquals(
			"my-form-variation", contentSetProvider.getItemSubtype());
	}

	@Test
	public void testGetSiteContentSetProvidersPageWithItemType()
		throws Exception {

		String itemType = "test-itemtype-" + RandomTestUtil.randomString();

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				"matching", "Matching Item Type Provider", itemType, null,
				true));

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				"non-matching", "Non Matching Item Type Provider",
				Object.class.getName(), null, true));

		Page<ContentSetProvider> page =
			contentSetProviderResource.getSiteContentSetProvidersPage(
				testGroup.getGroupId(), itemType, null, Pagination.of(1, 500));

		List<ContentSetProvider> contentSetProviders =
			(List<ContentSetProvider>)page.getItems();

		Assert.assertTrue(_containsKey(contentSetProviders, "matching"));
		Assert.assertFalse(_containsKey(contentSetProviders, "non-matching"));
	}

	@Test
	public void testGetSiteContentSetProvidersPageWithKeywords()
		throws Exception {

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				"alpha", "Alpha Provider", Object.class.getName(), null, true));
		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				"beta", "Beta Provider", Object.class.getName(), null, true));

		Page<ContentSetProvider> page =
			contentSetProviderResource.getSiteContentSetProvidersPage(
				testGroup.getGroupId(), null, "alpha", Pagination.of(1, 500));

		List<ContentSetProvider> contentSetProviders =
			(List<ContentSetProvider>)page.getItems();

		Assert.assertTrue(_containsKey(contentSetProviders, "alpha"));
		Assert.assertFalse(_containsKey(contentSetProviders, "beta"));
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"itemSubtype", "itemType", "key", "title"};
	}

	@Override
	protected ContentSetProvider
			testGetAssetLibraryContentSetProvidersPage_addContentSetProvider(
				Long assetLibraryId, ContentSetProvider contentSetProvider)
		throws Exception {

		return _registerContentSetProvider(contentSetProvider);
	}

	@Override
	protected ContentSetProvider
			testGetSiteContentSetProvidersPage_addContentSetProvider(
				Long siteId, ContentSetProvider contentSetProvider)
		throws Exception {

		return _registerContentSetProvider(contentSetProvider);
	}

	private boolean _containsKey(
		List<ContentSetProvider> contentSetProviders, String key) {

		if (_findByKey(contentSetProviders, key) != null) {
			return true;
		}

		return false;
	}

	private ContentSetProvider _findByKey(
		List<ContentSetProvider> contentSetProviders, String key) {

		for (ContentSetProvider contentSetProvider : contentSetProviders) {
			if (key.equals(contentSetProvider.getKey())) {
				return contentSetProvider;
			}
		}

		return null;
	}

	private int _indexOfKey(
		List<ContentSetProvider> contentSetProviders, String key) {

		for (int i = 0; i < contentSetProviders.size(); i++) {
			ContentSetProvider contentSetProvider = contentSetProviders.get(i);

			if (key.equals(contentSetProvider.getKey())) {
				return i;
			}
		}

		return -1;
	}

	private ContentSetProvider _registerContentSetProvider(
		ContentSetProvider contentSetProvider) {

		_registerInfoCollectionProvider(
			new TestInfoCollectionProvider(
				contentSetProvider.getKey(), contentSetProvider.getTitle(),
				contentSetProvider.getItemType(),
				contentSetProvider.getItemSubtype(), true));

		return contentSetProvider;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void _registerInfoCollectionProvider(
		InfoCollectionProvider<?> infoCollectionProvider) {

		// The registry indexes services either by the item.class.name service
		// property or, when absent, by the generic type parameter via
		// GenericUtil.getGenericClassName. Set the property explicitly so the
		// provider is findable under whatever itemType the test declares,
		// independent of the implementing class's generic signature.

		Dictionary<String, Object> properties = new Hashtable<>();

		properties.put(
			"item.class.name",
			infoCollectionProvider.getCollectionItemClassName());

		ServiceRegistration<InfoCollectionProvider<?>> serviceRegistration =
			_bundleContext.registerService(
				(Class<InfoCollectionProvider<?>>)
					(Class)InfoCollectionProvider.class,
				infoCollectionProvider, properties);

		_serviceRegistrations.add(serviceRegistration);
	}

	private BundleContext _bundleContext;
	private List<ServiceRegistration<InfoCollectionProvider<?>>>
		_serviceRegistrations;

	private static class TestInfoCollectionProvider
		implements SingleFormVariationInfoCollectionProvider<Object> {

		public TestInfoCollectionProvider(
			String key, String title, String itemType, String itemSubtype,
			boolean available) {

			_key = key;
			_title = title;
			_itemType = itemType;
			_itemSubtype = itemSubtype;
			_available = available;
		}

		@Override
		public InfoPage<Object> getCollectionInfoPage(
			CollectionQuery collectionQuery) {

			return InfoPage.of(Collections.emptyList());
		}

		@Override
		public String getCollectionItemClassName() {
			return _itemType;
		}

		@Override
		public String getFormVariationKey() {
			return _itemSubtype;
		}

		@Override
		public String getKey() {
			return _key;
		}

		@Override
		public String getLabel(Locale locale) {
			return _title;
		}

		@Override
		public boolean isAvailable() {
			return _available;
		}

		private final boolean _available;
		private final String _itemSubtype;
		private final String _itemType;
		private final String _key;
		private final String _title;

	}

}