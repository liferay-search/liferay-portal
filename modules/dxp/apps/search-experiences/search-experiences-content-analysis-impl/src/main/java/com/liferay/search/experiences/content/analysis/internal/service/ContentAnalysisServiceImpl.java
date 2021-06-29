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

package com.liferay.search.experiences.content.analysis.internal.service;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.search.experiences.content.analysis.request.ContentAnalysisRequest;
import com.liferay.search.experiences.content.analysis.response.CategoryAnalysisResponse;
import com.liferay.search.experiences.content.analysis.response.ModerationAnalysisResponse;
import com.liferay.search.experiences.content.analysis.service.ContentAnalysisService;
import com.liferay.search.experiences.content.analysis.spi.analyzer.CategoryAnalyzer;
import com.liferay.search.experiences.content.analysis.spi.analyzer.ModerationAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Petteri Karttunen
 */
@Component(immediate = true, service = ContentAnalysisService.class)
public class ContentAnalysisServiceImpl implements ContentAnalysisService {

	@Override
	public Optional<List<CategoryAnalysisResponse>> analyzeCategories(
		ContentAnalysisRequest contentAnalysisRequest) {

		if (_isEmptyServiceTrackerMap(_categoryAnalyzerServiceTrackerMap)) {
			return Optional.empty();
		}

		List<CategoryAnalysisResponse> responses = new ArrayList<>();

		Set<String> keySet = _categoryAnalyzerServiceTrackerMap.keySet();

		keySet.forEach(
			key -> {
				CategoryAnalyzer categoryAnalyzer =
					_categoryAnalyzerServiceTrackerMap.getService(key);

				Optional<CategoryAnalysisResponse> optional =
					categoryAnalyzer.analyze(contentAnalysisRequest);

				if (!optional.isPresent()) {
					if (_log.isDebugEnabled()) {
						StringBundler sb = new StringBundler(3);

						sb.append("Category analyzer ");
						sb.append(categoryAnalyzer.getClass());
						sb.append(" gave an empty response");

						_log.debug(sb.toString());
					}
				}
				else {
					responses.add(optional.get());
				}
			});

		return Optional.of(responses);
	}

	@Override
	public Optional<List<ModerationAnalysisResponse>> analyzeModeration(
		ContentAnalysisRequest contentAnalysisRequest) {

		if (_isEmptyServiceTrackerMap(_moderationAnalyzerServiceTrackerMap)) {
			return Optional.empty();
		}

		List<ModerationAnalysisResponse> responses = new ArrayList<>();

		Set<String> keySet = _moderationAnalyzerServiceTrackerMap.keySet();

		keySet.forEach(
			key -> {
				ModerationAnalyzer moderationAnalyzer =
					_moderationAnalyzerServiceTrackerMap.getService(key);

				Optional<ModerationAnalysisResponse> optional =
					moderationAnalyzer.analyze(contentAnalysisRequest);

				if (!optional.isPresent()) {
					if (_log.isDebugEnabled()) {
						StringBundler sb = new StringBundler(3);

						sb.append("Category analyzer ");
						sb.append(moderationAnalyzer.getClass());
						sb.append(" gave an empty response");

						_log.debug(sb.toString());
					}
				}
				else {
					responses.add(optional.get());
				}
			});

		return Optional.of(responses);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_categoryAnalyzerServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, CategoryAnalyzer.class, "name");
		_moderationAnalyzerServiceTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, ModerationAnalyzer.class, "name");
	}

	@Deactivate
	protected void deactivate() {
		_categoryAnalyzerServiceTrackerMap.close();
		_moderationAnalyzerServiceTrackerMap.close();
	}

	private boolean _isEmptyServiceTrackerMap(
		ServiceTrackerMap<String, ?> serviceTrackerMap) {

		Set<String> keySet = serviceTrackerMap.keySet();

		return keySet.isEmpty();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ContentAnalysisServiceImpl.class);

	private ServiceTrackerMap<String, CategoryAnalyzer>
		_categoryAnalyzerServiceTrackerMap;
	private ServiceTrackerMap<String, ModerationAnalyzer>
		_moderationAnalyzerServiceTrackerMap;

}