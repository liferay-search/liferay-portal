package com.liferay.portal.search.tuning.gsearch.results;

import java.util.List;

public class ResultAttributes {


	public int getDescriptionMaxLength() {
		return _descriptionMaxLength;
	}

	public void setDescriptionMaxLength(int descriptionMaxLength) {
		_descriptionMaxLength = descriptionMaxLength;
	}

	public boolean isIncludeThumbnail() {
		return includeThumbnail;
	}

	public void setIncludeThumbnail(boolean includeThumbnail) {
		this.includeThumbnail = includeThumbnail;
	}

	public boolean isIncludeUserPortrait() {
		return includeUserPortrait;
	}

	public void setIncludeUserPortrait(boolean includeUserPortrait) {
		this.includeUserPortrait = includeUserPortrait;
	}

	public boolean isIncludeRawDocument() {
		return includeRawDocument;
	}

	public void setIncludeRawDocument(boolean includeRawDocument) {
		this.includeRawDocument = includeRawDocument;
	}

	private int _descriptionMaxLength = 700;
	private boolean includeRawDocument = false;
	private boolean includeThumbnail = false;
	private boolean includeUserPortrait = false;
	
	private List<String> resultFields;
}
