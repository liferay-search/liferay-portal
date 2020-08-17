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

package com.liferay.portal.search.tuning.blueprints.constants;

import com.liferay.portal.kernel.security.permission.ActionKeys;

/**
 * @author Petteri Karttunen
 */
public class BlueprintsActionKeys {

	public static final String ADD_BLUEPRINT = "ADD_BLUEPRINT";

	public static final String APPLY_BLUEPRINT = "APPLY_BLUEPRINT";

	public static final String ADD_FRAGMENT = "ADD_FRAGMENT";

	public static final String ADD_TEMPLATE = "ADD_TEMPLATE";

	public static final String DELETE_BLUEPRINT = "DELETE_BLUEPRINT";

	public static final String DELETE_FRAGMENT = "DELETE_FRAGMENT";

	public static final String DELETE_TEMPLATE = "DELETE_TEMPLATE";

	public static final String UPDATE_BLUEPRINT = "UPDATE_BLUEPRINT";

	public static final String UPDATE_FRAGMENT = "UPDATE_FRAGMENT";

	public static final String UPDATE_TEMPLATE = "UPDATE_TEMPLATE";

	public static final String VIEW_BLUEPRINT = "VIEW_BLUEPRINT";

	public static final String VIEW_FRAGMENT = "VIEW_FRAGMENT";

	public static final String VIEW_TEMPLATE = "VIEW_TEMPLATE";

	public static String getActionKeyForBlueprintType(
		int type, String action) {

		if (type == BlueprintTypes.BLUEPRINT) {
			if (ActionKeys.VIEW.equals(action)) {
				return BlueprintsActionKeys.VIEW_BLUEPRINT;
			}
			else if (ActionKeys.ADD_ENTRY.equals(action)) {
				return BlueprintsActionKeys.ADD_BLUEPRINT;
			}
			else if (ActionKeys.UPDATE.equals(action)) {
				return BlueprintsActionKeys.UPDATE_BLUEPRINT;
			}
			else if (ActionKeys.DELETE.equals(action)) {
				return BlueprintsActionKeys.DELETE_BLUEPRINT;
			}
			else {
				return action;
			}
		}
		else if (type == BlueprintTypes.AGGREGATION_FRAGMENT  ||
				type == BlueprintTypes.PARAMETER_FRAGMENT ||
				type == BlueprintTypes.QUERY_FRAGMENT ||
				type == BlueprintTypes.SORT_FRAGMENT ||
				type == BlueprintTypes.SUGGESTER_FRAGMENT) {
			if (ActionKeys.VIEW.equals(action)) {
				return BlueprintsActionKeys.VIEW_FRAGMENT;
			}
			else if (ActionKeys.ADD_ENTRY.equals(action)) {
				return BlueprintsActionKeys.ADD_FRAGMENT;
			}
			else if (ActionKeys.UPDATE.equals(action)) {
				return BlueprintsActionKeys.UPDATE_FRAGMENT;
			}
			else if (ActionKeys.DELETE.equals(action)) {
				return BlueprintsActionKeys.DELETE_FRAGMENT;
			}
			else {
				return action;
			}
		}
		else if (type == BlueprintTypes.TEMPLATE) {
			if (ActionKeys.VIEW.equals(action)) {
				return BlueprintsActionKeys.VIEW_TEMPLATE;
			}
			else if (ActionKeys.ADD_ENTRY.equals(action)) {
				return BlueprintsActionKeys.ADD_TEMPLATE;
			}
			else if (ActionKeys.UPDATE.equals(action)) {
				return BlueprintsActionKeys.UPDATE_TEMPLATE;
			}
			else if (ActionKeys.DELETE.equals(action)) {
				return BlueprintsActionKeys.DELETE_TEMPLATE;
			}
			else {
				return action;
			}
		}

		throw new RuntimeException(
			"Unknown type " + type + " for action " + action + ".");
	}

}