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

package com.liferay.portal.search.elasticsearch7.internal.sidecar;

import java.util.Arrays;
import java.util.List;

/**
 * @author André de Oliveira
 */
public class Elasticsearch7100Distribution implements Distribution {

	@Override
	public Distributable getElasticsearchDistributable() {
		return new DistributableImpl(
			"https://artifacts.elastic.co/downloads/elasticsearch" +
				"/elasticsearch-oss-7.10.0-no-jdk-linux-x86_64.tar.gz",
			"51f2146e2aacdb9e9f4b16067224f88952bc5495aedc7d7b3936a7f2db336cc5" +
				"134013c1ee8a273cc4874d533f2a0db2387f8e0bc8508fa91fe21566d5da" +
					"0355");
	}

	@Override
	public List<Distributable> getPluginDistributables() {
		return Arrays.asList(
			new DistributableImpl(
				"https://artifacts.elastic.co/downloads/elasticsearch-plugins" +
					"/analysis-icu/analysis-icu-7.10.0.zip",
				"523c6f126ad780013066ee9f84abfb66c1695f776280eaa7f48f115f108f" +
					"0860d33c250640a6a673857fb3a1942c1426ebe379849e11c36c3f6c" +
						"a4285ba1a4a8"),
			new DistributableImpl(
				"https://artifacts.elastic.co/downloads/elasticsearch-plugins" +
					"/analysis-kuromoji/analysis-kuromoji-7.10.0.zip",
				"dd671723797cbd8cea1e5f7ff323163a3250ec0e8c691fb66354775aaeab" +
					"ad3b2a99bbc7f2098bbf145213238cce772f8ddc9ad673fc71d83aac" +
						"d1e5ccac0dbc"),
			new DistributableImpl(
				"https://artifacts.elastic.co/downloads/elasticsearch-plugins" +
					"/analysis-smartcn/analysis-smartcn-7.10.0.zip",
				"ecb2c5583e76deaa0bd2260aa568009c4bba953010004d1ad30759f54d27" +
					"ebd77d72d64609eef6b810ae83d378711980cd5edaf43e9f0aaab87f" +
						"ae46ccd23641"),
			new DistributableImpl(
				"https://artifacts.elastic.co/downloads/elasticsearch-plugins" +
					"/analysis-stempel/analysis-stempel-7.10.0.zip",
				"fb01d40d1f0607b13c72a0ea1b53790e0b49a602f3b44b325977a3ed9ae4" +
					"27b63892a10788757fa417571638d2abacfbd8b63f96e993932b430d" +
						"bf108e7f0154"));
	}

}