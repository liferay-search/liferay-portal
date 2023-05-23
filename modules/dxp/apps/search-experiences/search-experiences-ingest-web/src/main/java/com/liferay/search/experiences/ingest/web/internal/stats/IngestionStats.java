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

package com.liferay.search.experiences.ingest.web.internal.stats;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petteri Karttunen
 */
public class IngestionStats {

	public void addFailedItem() {
		_failedItemsCount++;
	}

	public void addIngestedTitle(String title) {
		_ingestedTitles.add(title);
	}

	public void addSkippedItem() {
		_skippedItemsCount++;
	}

	public int getFailedItemsCount() {
		return _failedItemsCount;
	}

	public int getIngestedItemsCount() {
		return _ingestedTitles.size();
	}

	public List<String> getIngestedTitles() {
		return _ingestedTitles;
	}

	public long getSecondsElapsed() {
		return _secondsElapsed;
	}

	public int getTotalProcessedItemsCount() {
		return _failedItemsCount + _ingestedTitles.size() + _skippedItemsCount;
	}

	public boolean hasIngestedTitle(String title) {
		return _ingestedTitles.contains(title);
	}

	public void setSecondsElapsed(long secondsElapsed) {
		_secondsElapsed = secondsElapsed;
	}

	private int _failedItemsCount;
	private final List<String> _ingestedTitles = new ArrayList<>();
	private long _secondsElapsed;
	private int _skippedItemsCount;

}