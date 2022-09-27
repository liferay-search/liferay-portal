/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.web.internal.facet.display.context.builder;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.search.facet.Facet;
import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;
import com.liferay.portal.kernel.theme.PortletDisplay;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.search.web.internal.facet.display.context.UserSearchFacetDisplayContext;
import com.liferay.portal.search.web.internal.facet.display.context.UserSearchFacetTermDisplayContext;
import com.liferay.portal.search.web.internal.user.facet.configuration.UserFacetPortletInstanceConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.portlet.RenderRequest;

/**
 * @author André de Oliveira
 */
public class UserSearchFacetDisplayContextBuilder {

	public UserSearchFacetDisplayContextBuilder(RenderRequest renderRequest)
		throws ConfigurationException {

		_themeDisplay = (ThemeDisplay)renderRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		PortletDisplay portletDisplay = _themeDisplay.getPortletDisplay();

		_userFacetPortletInstanceConfiguration =
			portletDisplay.getPortletInstanceConfiguration(
				UserFacetPortletInstanceConfiguration.class);
	}

	public UserSearchFacetDisplayContext build() {
		boolean nothingSelected = isNothingSelected();

		List<TermCollector> termCollectors = getTermCollectors();

		boolean renderNothing = false;

		if (nothingSelected && termCollectors.isEmpty()) {
			renderNothing = true;
		}

		UserSearchFacetDisplayContext userSearchFacetDisplayContext =
			new UserSearchFacetDisplayContext();

		userSearchFacetDisplayContext.setDisplayStyleGroupId(
			getDisplayStyleGroupId());
		userSearchFacetDisplayContext.setNothingSelected(nothingSelected);
		userSearchFacetDisplayContext.setPaginationStartParameterName(
			_paginationStartParameterName);
		userSearchFacetDisplayContext.setParamName(_paramName);
		userSearchFacetDisplayContext.setParamValue(_getFirstParamValue());
		userSearchFacetDisplayContext.setParamValues(_paramValues);
		userSearchFacetDisplayContext.setRenderNothing(renderNothing);
		userSearchFacetDisplayContext.setTermDisplayContexts(
			buildTermDisplayContexts(termCollectors));
		userSearchFacetDisplayContext.setUserFacetPortletInstanceConfiguration(
			_userFacetPortletInstanceConfiguration);

		return userSearchFacetDisplayContext;
	}

	public void setFacet(Facet facet) {
		_facet = facet;
	}

	public void setFrequenciesVisible(boolean frequenciesVisible) {
		_frequenciesVisible = frequenciesVisible;
	}

	public void setFrequencyThreshold(int frequencyThreshold) {
		_frequencyThreshold = frequencyThreshold;
	}

	public void setMaxTerms(int maxTerms) {
		_maxTerms = maxTerms;
	}

	public void setOrder(String order) {
		_order = order;
	}

	public void setPaginationStartParameterName(
		String paginationStartParameterName) {

		_paginationStartParameterName = paginationStartParameterName;
	}

	public void setParamName(String paramName) {
		_paramName = paramName;
	}

	public void setParamValue(String paramValue) {
		paramValue = StringUtil.trim(Objects.requireNonNull(paramValue));

		if (paramValue.isEmpty()) {
			return;
		}

		_paramValues = Collections.singletonList(paramValue);
	}

	public void setParamValues(List<String> paramValues) {
		_paramValues = paramValues;
	}

	protected UserSearchFacetTermDisplayContext buildTermDisplayContext(
		TermCollector termCollector) {

		String term = GetterUtil.getString(termCollector.getTerm());

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			new UserSearchFacetTermDisplayContext();

		userSearchFacetTermDisplayContext.setFrequency(
			termCollector.getFrequency());
		userSearchFacetTermDisplayContext.setFrequencyVisible(
			_frequenciesVisible);
		userSearchFacetTermDisplayContext.setSelected(isSelected(term));
		userSearchFacetTermDisplayContext.setUserName(term);

		return userSearchFacetTermDisplayContext;
	}

	protected List<UserSearchFacetTermDisplayContext> buildTermDisplayContexts(
		List<TermCollector> termCollectors) {

		if (termCollectors.isEmpty()) {
			return getEmptyTermDisplayContexts();
		}

		List<UserSearchFacetTermDisplayContext>
			userSearchFacetTermDisplayContexts = new ArrayList<>(
				termCollectors.size());

		for (int i = 0; i < termCollectors.size(); i++) {
			TermCollector termCollector = termCollectors.get(i);

			if (((_maxTerms > 0) && (i >= _maxTerms)) ||
				((_frequencyThreshold > 0) &&
				 (_frequencyThreshold > termCollector.getFrequency()))) {

				break;
			}

			userSearchFacetTermDisplayContexts.add(
				buildTermDisplayContext(termCollector));
		}

		if (_order.equals("key:asc")) {
			userSearchFacetTermDisplayContexts.sort(_ASC_TERM_COMPARATOR);
		}
		else if (_order.equals("key:desc")) {
			userSearchFacetTermDisplayContexts.sort(_DESC_TERM_COMPARATOR);
		}
		else if (_order.equals("count:asc")) {
			userSearchFacetTermDisplayContexts.sort(_ASC_FREQUENCY_COMPARATOR);
		}
		else if (_order.equals("count:desc")) {
			userSearchFacetTermDisplayContexts.sort(_DESC_FREQUENCY_COMPARATOR);
		}

		return userSearchFacetTermDisplayContexts;
	}

	protected long getDisplayStyleGroupId() {
		long displayStyleGroupId =
			_userFacetPortletInstanceConfiguration.displayStyleGroupId();

		if (displayStyleGroupId <= 0) {
			displayStyleGroupId = _themeDisplay.getScopeGroupId();
		}

		return displayStyleGroupId;
	}

	protected List<UserSearchFacetTermDisplayContext>
		getEmptyTermDisplayContexts() {

		if (_paramValues.isEmpty()) {
			return Collections.emptyList();
		}

		UserSearchFacetTermDisplayContext userSearchFacetTermDisplayContext =
			new UserSearchFacetTermDisplayContext();

		userSearchFacetTermDisplayContext.setFrequency(0);
		userSearchFacetTermDisplayContext.setFrequencyVisible(
			_frequenciesVisible);
		userSearchFacetTermDisplayContext.setSelected(true);
		userSearchFacetTermDisplayContext.setUserName(_paramValues.get(0));

		return Collections.singletonList(userSearchFacetTermDisplayContext);
	}

	protected List<TermCollector> getTermCollectors() {
		if (_facet == null) {
			return Collections.emptyList();
		}

		FacetCollector facetCollector = _facet.getFacetCollector();

		if (facetCollector != null) {
			return facetCollector.getTermCollectors();
		}

		return Collections.<TermCollector>emptyList();
	}

	protected boolean isNothingSelected() {
		if (_paramValues.isEmpty()) {
			return true;
		}

		return false;
	}

	protected boolean isSelected(String value) {
		if (_paramValues.contains(value)) {
			return true;
		}

		return false;
	}

	private static int _compareDisplayNames(
		String displayName1, String displayName2) {

		return displayName1.compareTo(displayName2);
	}

	private String _getFirstParamValue() {
		if (_paramValues.isEmpty()) {
			return StringPool.BLANK;
		}

		return _paramValues.get(0);
	}

	private static final Comparator<UserSearchFacetTermDisplayContext>
		_ASC_FREQUENCY_COMPARATOR =
			new Comparator<UserSearchFacetTermDisplayContext>() {

				@Override
				public int compare(
					UserSearchFacetTermDisplayContext displayContext1,
					UserSearchFacetTermDisplayContext displayContext2) {

					int result =
						displayContext1.getFrequency() -
							displayContext2.getFrequency();

					if (result == 0) {
						return _compareDisplayNames(
							displayContext1.getUserName(),
							displayContext2.getUserName());
					}

					return result;
				}

			};

	private static final Comparator<UserSearchFacetTermDisplayContext>
		_ASC_TERM_COMPARATOR =
			new Comparator<UserSearchFacetTermDisplayContext>() {

				@Override
				public int compare(
					UserSearchFacetTermDisplayContext displayContext1,
					UserSearchFacetTermDisplayContext displayContext2) {

					int result = _compareDisplayNames(
						displayContext1.getUserName(),
						displayContext2.getUserName());

					if (result == 0) {
						return displayContext2.getFrequency() -
							displayContext1.getFrequency();
					}

					return result;
				}

			};

	private static final Comparator<UserSearchFacetTermDisplayContext>
		_DESC_FREQUENCY_COMPARATOR =
			new Comparator<UserSearchFacetTermDisplayContext>() {

				@Override
				public int compare(
					UserSearchFacetTermDisplayContext displayContext1,
					UserSearchFacetTermDisplayContext displayContext2) {

					int result =
						displayContext2.getFrequency() -
							displayContext1.getFrequency();

					if (result == 0) {
						return _compareDisplayNames(
							displayContext1.getUserName(),
							displayContext2.getUserName());
					}

					return result;
				}

			};

	private static final Comparator<UserSearchFacetTermDisplayContext>
		_DESC_TERM_COMPARATOR =
			new Comparator<UserSearchFacetTermDisplayContext>() {

				@Override
				public int compare(
					UserSearchFacetTermDisplayContext displayContext1,
					UserSearchFacetTermDisplayContext displayContext2) {

					int result = _compareDisplayNames(
						displayContext2.getUserName(),
						displayContext1.getUserName());

					if (result == 0) {
						return displayContext2.getFrequency() -
							displayContext1.getFrequency();
					}

					return result;
				}

			};

	private Facet _facet;
	private boolean _frequenciesVisible;
	private int _frequencyThreshold;
	private int _maxTerms;
	private String _order;
	private String _paginationStartParameterName;
	private String _paramName;
	private List<String> _paramValues = Collections.emptyList();
	private final ThemeDisplay _themeDisplay;
	private final UserFacetPortletInstanceConfiguration
		_userFacetPortletInstanceConfiguration;

}