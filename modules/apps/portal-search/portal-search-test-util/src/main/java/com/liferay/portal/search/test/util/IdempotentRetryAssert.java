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

package com.liferay.portal.search.test.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author André de Oliveira
 */
public class IdempotentRetryAssert {

	public static <T> T retryAssert(
			long timeout, TimeUnit timeoutTimeUnit, Callable<T> callable)
		throws Exception {

		return SearchRetryFixture.builder(
		).timeout(
			(int)timeout, timeoutTimeUnit
		).build(
		).assertSearch(
			() -> _call(callable)
		);
	}

	public static <T> T retryAssert(
			long timeout, TimeUnit timeoutTimeUnit, long pause,
			TimeUnit pauseTimeUnit, Callable<T> callable)
		throws Exception {

		return SearchRetryFixture.builder(
		).pause(
			(int)pause, pauseTimeUnit
		).timeout(
			(int)timeout, timeoutTimeUnit
		).build(
		).assertSearch(
			() -> _call(callable)
		);
	}

	public static <T> T retryAssert(
			long timeout, TimeUnit timeoutTimeUnit, Runnable runnable)
		throws Exception {

		SearchRetryFixture.builder(
		).timeout(
			(int)timeout, timeoutTimeUnit
		).build(
		).assertSearch(
			runnable
		);

		return null;
	}

	private static <T> T _call(Callable<T> callable) {
		try {
			return callable.call();
		}
		catch (RuntimeException runtimeException) {
			throw runtimeException;
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

}