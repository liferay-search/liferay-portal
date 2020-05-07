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

package com.liferay.portal.search.tuning.gsearch.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Petteri Karttunen
 */
public class ResultAttributes {

	public void addAttribute(String key, Object value) {
		_attributes.put(key, value);
	}

	public Map<String, Object> getAttributes() {
		return _attributes;
	}

	public int getDescriptionMaxLength() {
		return _descriptionMaxLength;
	}

	public List<String> getResultFields() {
		return _resultFields;
	}

	public boolean isIncludeRawDocument() {
		return _includeRawDocument;
	}

	public boolean isIncludeThumbnail() {
		return _includeThumbnail;
	}

	public boolean isIncludeUserPortrait() {
		return _includeUserPortrait;
	}

	public void setDescriptionMaxLength(int descriptionMaxLength) {
		_descriptionMaxLength = descriptionMaxLength;
	}

	public void setIncludeRawDocument(boolean includeRawDocument) {
		_includeRawDocument = includeRawDocument;
	}

	public void setIncludeThumbnail(boolean includeThumbnail) {
		_includeThumbnail = includeThumbnail;
	}

	public void setIncludeUserPortrait(boolean includeUserPortrait) {
		_includeUserPortrait = includeUserPortrait;
	}

	public void setResultFields(List<String> resultFields) {
		_resultFields = resultFields;
	}

	private final Map<String, Object> _attributes = new HashMap<>();
	private int _descriptionMaxLength = 700;
	private boolean _includeRawDocument;
	private boolean _includeThumbnail;
	private boolean _includeUserPortrait;
	private List<String> _resultFields;

}