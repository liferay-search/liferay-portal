/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemListBuilder;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.engine.SearchEngineInformation;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.DocumentToMisspellingSetTranslator;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.MisspellingSet;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexName;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.index.name.MisspellingSetIndexNameBuilder;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.request.SearchMisspellingSetRequest;
import com.liferay.portal.search.tuning.blueprints.misspellings.web.internal.request.SearchMisspellingSetResponse;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionURL;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.RenderURL;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Filipe Oshiro
 */
public class MisspellingsDisplayBuilder {

	public MisspellingsDisplayBuilder(
		DocumentToMisspellingSetTranslator documentToMisspellingSetTranslator,
		HttpServletRequest httpServletRequest, Language language, Portal portal,
		Queries queries, RenderRequest renderRequest,
		RenderResponse renderResponse, SearchEngineAdapter searchEngineAdapter,
		SearchEngineInformation searchEngineInformation, Sorts sorts,
		MisspellingSetIndexNameBuilder misspellingSetIndexNameBuilder) {

		_documentToMisspellingSetTranslator = documentToMisspellingSetTranslator;
		_httpServletRequest = httpServletRequest;
		_language = language;
		_portal = portal;
		_queries = queries;
		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_searchEngineAdapter = searchEngineAdapter;
		_searchEngineInformation = searchEngineInformation;
		_sorts = sorts;
		_misspellingSetIndexNameBuilder = misspellingSetIndexNameBuilder;
	}

	public MisspellingsDisplayContext build() {
		MisspellingsDisplayContext misspellingsDisplayContext =
			new MisspellingsDisplayContext();

		if (Objects.equals(
				_searchEngineInformation.getVendorString(), "Solr")) {

			return misspellingsDisplayContext;
		}

		misspellingsDisplayContext.setCreationMenu(getCreationMenu());

		SearchContainer<MisspellingSetDisplayContext> searchContainer =
			buildSearchContainer();

		List<MisspellingSetDisplayContext> MisspellingSetDisplayContexts =
			searchContainer.getResults();

		misspellingsDisplayContext.setDisabledManagementBar(
			isDisabledManagementBar(MisspellingSetDisplayContexts));

		misspellingsDisplayContext.setDropdownItems(getDropdownItems());
		misspellingsDisplayContext.setItemsTotal(MisspellingSetDisplayContexts.size());
		misspellingsDisplayContext.setSearchContainer(searchContainer);

		return misspellingsDisplayContext;
	}

	protected RenderURL buildEditRenderURL(MisspellingSet misspellingSet) {
		RenderURL editRenderURL = _renderResponse.createRenderURL();

		editRenderURL.setParameter("mvcRenderCommandName", "editMisspellingSet");
		editRenderURL.setParameter(
			"redirect", _portal.getCurrentURL(_httpServletRequest));
		editRenderURL.setParameter("MisspellingSetId", misspellingSet.getMisspellingSetId());

		return editRenderURL;
	}

	protected SearchContainer<MisspellingSetDisplayContext> buildSearchContainer() {
		SearchContainer<MisspellingSetDisplayContext> searchContainer =
			new SearchContainer<>(
				_renderRequest, _getPortletURL(), null, "there-are-no-entries");

		searchContainer.setId("MisspellingSetsEntries");
		searchContainer.setRowChecker(
			new EmptyOnClickRowChecker(_renderResponse));

		SearchMisspellingSetRequest searchMisspellingSetRequest =
			new SearchMisspellingSetRequest(
				buildMisspellingSetIndexName(), _httpServletRequest, _queries,
				_sorts, searchContainer, _searchEngineAdapter);

		SearchMisspellingSetResponse searchMisspellingSetResponse =
			searchMisspellingSetRequest.search();

		searchContainer.setResults(
			buildMisspellingSetDisplayContexts(
				searchMisspellingSetResponse.getSearchHits()));

		searchContainer.setSearch(true);
		searchContainer.setTotal(searchMisspellingSetResponse.getTotalHits());

		return searchContainer;
	}

	protected MisspellingSetDisplayContext buildMisspellingSetDisplayContext(
		MisspellingSet misspellingSet) {

		MisspellingSetDisplayContext misspellingSetDisplayContext =
			new MisspellingSetDisplayContext();

		List<String> misspellings = misspellingSet.getMisspellings();

		RenderURL editRenderURL = buildEditRenderURL(misspellingSet);

		misspellingSetDisplayContext.setDropDownItems(
			buildMisspellingSetDropdownItemList(misspellingSet, editRenderURL));
		misspellingSetDisplayContext.setEditRenderURL(editRenderURL.toString());

		misspellingSetDisplayContext.setName(
				misspellingSet.getName());
		misspellingSetDisplayContext.setMisspellingSetId(misspellingSet.getMisspellingSetId());
		misspellingSetDisplayContext.setMisspellings(misspellings);

		return misspellingSetDisplayContext;
	}

	protected List<MisspellingSetDisplayContext> buildMisspellingSetDisplayContexts(
		SearchHits searchHits) {

		List<MisspellingSet> misspellingSets =
			_documentToMisspellingSetTranslator.translateAll(searchHits);

		Stream<MisspellingSet> stream = misspellingSets.stream();

		return stream.map(
			this::buildMisspellingSetDisplayContext
		).collect(
			Collectors.toList()
		);
	}

	protected List<DropdownItem> buildMisspellingSetDropdownItemList(
		MisspellingSet misspellingSet, RenderURL editRenderURL) {

		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.setHref(editRenderURL);
				dropdownItem.setLabel(
					_language.get(_httpServletRequest, "edit"));
				dropdownItem.setQuickAction(true);
			}
		).add(
			dropdownItem -> {
				dropdownItem.putData("action", "delete");

				ActionURL deleteURL = _renderResponse.createActionURL();

				deleteURL.setParameter(
					ActionRequest.ACTION_NAME, "deleteMisspellingSet");
				deleteURL.setParameter(Constants.CMD, Constants.DELETE);
				deleteURL.setParameter("rowIds", misspellingSet.getMisspellingSetId());
				deleteURL.setParameter(
					"redirect", _portal.getCurrentURL(_httpServletRequest));

				dropdownItem.putData("deleteURL", deleteURL.toString());

				dropdownItem.setIcon("times");
				dropdownItem.setLabel(
					_language.get(_httpServletRequest, "delete"));
				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	protected MisspellingSetIndexName buildMisspellingSetIndexName() {
		return _misspellingSetIndexNameBuilder.getMisspellingSetIndexName(
			_portal.getCompanyId(_renderRequest));
	}

	protected CreationMenu getCreationMenu() {
		return CreationMenuBuilder.addPrimaryDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(
					_renderResponse.createRenderURL(), "mvcRenderCommandName",
					"editMisspellingSet", "redirect",
					_portal.getCurrentURL(_httpServletRequest));
				dropdownItem.setLabel(
					_language.get(_httpServletRequest, "new-misspelling-set"));
			}
		).build();
	}

	protected List<DropdownItem> getDropdownItems() {
		return DropdownItemListBuilder.add(
			dropdownItem -> {
				dropdownItem.putData("action", "deleteMultipleMisspellingSets");
				dropdownItem.setIcon("times-circle");
				dropdownItem.setLabel(
					_language.get(_httpServletRequest, "delete"));
				dropdownItem.setQuickAction(true);
			}
		).build();
	}

	protected boolean isDisabledManagementBar(
		List<MisspellingSetDisplayContext> misspellingSetDisplayContexts) {

		if (misspellingSetDisplayContexts.isEmpty()) {
			return true;
		}

		return false;
	}

	private PortletURL _getPortletURL() {
		PortletURL portletURL = _renderResponse.createRenderURL();

		portletURL.setParameter("mvcPath", "/view.jsp");

		return portletURL;
	}

	private final DocumentToMisspellingSetTranslator
		_documentToMisspellingSetTranslator;
	private final HttpServletRequest _httpServletRequest;
	private final Language _language;
	private final Portal _portal;
	private final Queries _queries;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final SearchEngineAdapter _searchEngineAdapter;
	private final SearchEngineInformation _searchEngineInformation;
	private final Sorts _sorts;
	private final MisspellingSetIndexNameBuilder _misspellingSetIndexNameBuilder;

}