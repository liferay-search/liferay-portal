/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
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