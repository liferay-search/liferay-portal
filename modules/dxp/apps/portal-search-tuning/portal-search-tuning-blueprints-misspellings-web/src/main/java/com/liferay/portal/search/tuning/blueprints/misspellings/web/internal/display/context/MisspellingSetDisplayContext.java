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

import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;

import java.util.Date;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class MisspellingSetDisplayContext {

	public Date getCreated() {
		return _created;
	}

	public List<DropdownItem> getDropdownItems() {
		return _dropDownItems;
	}

	public String getEditRenderURL() {
		return _editRenderURL;
	}

	public String getLanguageId() {
		return _languageId;
	}

	public List<String> getMisspellings() {
		return _misspellings;
	}

	public String getMisspellingSetId() {
		return _misspellingSetId;
	}

	public Date getModified() {
		return _modified;
	}

	public String getName() {
		return _name;
	}

	public String getPhrase() {
		return _phrase;
	}

	public String getUserName() {
		return _userName;
	}

	public void setCreated(Date created) {
		_created = created;
	}

	public void setDropDownItems(List<DropdownItem> dropDownItems) {
		_dropDownItems = dropDownItems;
	}

	public void setEditRenderURL(String editRenderURL) {
		_editRenderURL = editRenderURL;
	}

	public void setMisspellings(List<String> misspellings) {
		_misspellings = misspellings;
	}

	public void setMisspellingSetId(String misspellingSetId) {
		_misspellingSetId = misspellingSetId;
	}

	public void setModified(Date modified) {
		_modified = modified;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}

	private Date _created;
	private List<DropdownItem> _dropDownItems;
	private String _editRenderURL;
	private String _languageId;
	private List<String> _misspellings;
	private String _misspellingSetId;
	private Date _modified;
	private String _name;
	private String _phrase;
	private String _userName;

}