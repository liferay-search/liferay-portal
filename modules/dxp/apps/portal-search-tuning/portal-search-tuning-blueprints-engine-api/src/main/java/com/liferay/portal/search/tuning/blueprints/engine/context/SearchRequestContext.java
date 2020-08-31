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

package com.liferay.portal.search.tuning.blueprints.engine.context;

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.search.searcher.SearchRequestBuilder;
import com.liferay.portal.search.tuning.blueprints.engine.message.Message;
import com.liferay.portal.search.tuning.blueprints.engine.parameter.SearchParameterData;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * @author Petteri Karttunen
 */
public interface SearchRequestContext {

	public void addMessage(Message message);

	public Map<String, Object> getAttributes();

	public JSONObject getBlueprint();

	public Long getBlueprintId();

	public Long getCompanyId();

	public int getFrom();

	public Optional<String> getInitialKeywords();

	public String getKeywords();

	public Locale getLocale();

	public List<Message> getMessages();

	public String getRawKeywords();

	public SearchParameterData getSearchParameterData();

	public SearchRequestBuilder getSearchRequestBuilder();

	public Long getUserId();

	public boolean hasErrors();

}