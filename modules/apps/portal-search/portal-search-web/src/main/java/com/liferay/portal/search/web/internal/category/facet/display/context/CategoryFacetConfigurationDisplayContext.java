/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.search.web.internal.category.facet.display.context;

import com.liferay.depot.group.provider.SiteConnectedGroupGroupProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.GroupService;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Akhash Ramprakash
 */
public class CategoryFacetConfigurationDisplayContext {

	public CategoryFacetConfigurationDisplayContext(
		GroupLocalService groupLocalService, GroupService groupService,
		JSONFactory jsonFactory, Locale locale,
		SiteConnectedGroupGroupProvider siteConnectedGroupGroupProvider) {

		_groupLocalService = groupLocalService;
		_groupService = groupService;
		_jsonFactory = jsonFactory;
		_locale = locale;
		_siteConnectedGroupGroupProvider = siteConnectedGroupGroupProvider;
	}

	public JSONArray getGroupsJSONArray() throws PortalException {
		JSONArray jsonArray = _jsonFactory.createJSONArray();

		for (Group group : _getSiteAndConnectedDepotGroups()) {
			jsonArray.put(_toJSONObject(group));
		}

		return jsonArray;
	}

	private Collection<Group> _getSiteAndConnectedDepotGroups()
		throws PortalException {

		Map<Long, Group> groupsMap = new LinkedHashMap<>();

		List<Group> siteGroups = ListUtil.filter(
			_groupService.getUserSitesGroups(), Group::isSite);

		for (Group siteGroup : siteGroups) {
			groupsMap.put(siteGroup.getGroupId(), siteGroup);
		}

		long[] groupIds =
			_siteConnectedGroupGroupProvider.
				getCurrentAndAncestorSiteAndDepotGroupIds(
					ListUtil.toLongArray(siteGroups, Group::getGroupId));

		List<Group> depotGroups = new ArrayList<>();

		for (long groupId : groupIds) {
			Group group = _groupLocalService.fetchGroup(groupId);

			if ((group != null) && group.isDepot() &&
				!groupsMap.containsKey(group.getGroupId())) {

				depotGroups.add(group);
				groupsMap.put(group.getGroupId(), group);
			}
		}

		Map<Group, String> depotGroupNames = new HashMap<>();

		for (Group depotGroup : depotGroups) {
			depotGroupNames.put(
				depotGroup, depotGroup.getDescriptiveName(_locale));
		}

		depotGroups.sort(Comparator.comparing(depotGroupNames::get));

		List<Group> groups = new ArrayList<>(siteGroups);

		groups.addAll(depotGroups);

		return groups;
	}

	private JSONObject _toJSONObject(Group group) throws PortalException {
		JSONObject jsonObject = _jsonFactory.createJSONObject();

		if (group.isDepot()) {
			jsonObject.put("assetLibraryKey", group.getGroupKey());
		}

		return jsonObject.put(
			"externalReferenceCode", group.getExternalReferenceCode()
		).put(
			"groupId", group.getGroupId()
		).put(
			"name", group.getDescriptiveName(_locale)
		);
	}

	private final GroupLocalService _groupLocalService;
	private final GroupService _groupService;
	private final JSONFactory _jsonFactory;
	private final Locale _locale;
	private final SiteConnectedGroupGroupProvider
		_siteConnectedGroupGroupProvider;

}