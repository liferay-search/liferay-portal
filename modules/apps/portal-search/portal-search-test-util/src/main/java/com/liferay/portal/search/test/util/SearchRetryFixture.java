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

import com.liferay.portal.kernel.util.GetterUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author André de Oliveira
 */
public class SearchRetryFixture {

	public static Builder builder() {
		return new Builder();
	}

	public void assertSearch(Runnable runnable) {
		assertSearch(
			() -> {
				runnable.run();

				return null;
			});
	}

	public <T> T assertSearch(Supplier<T> supplier) {
		if (_getAttempts() == 1) {
			return supplier.get();
		}

		TimeUnit timeoutTimeUnit = _getTimeoutTimeUnit();

		long deadline =
			System.currentTimeMillis() +
				timeoutTimeUnit.toMillis(_getTimeout());

		while (true) {
			try {
				return _call(supplier::get);
			}
			catch (AssertionError ae) {
				if (System.currentTimeMillis() > deadline) {
					throw ae;
				}
			}

			_sleep(_getPause(), _getPauseTimeUnit());
		}
	}

	public static class Builder {

		public Builder attempts(Integer attempts) {
			_searchRetryFixture._attempts = attempts;

			return this;
		}

		public SearchRetryFixture build() {
			return new SearchRetryFixture(_searchRetryFixture);
		}

		public Builder pause(Integer pause, TimeUnit pauseTimeUnit) {
			_searchRetryFixture._pause = pause;
			_searchRetryFixture._pauseTimeUnit = pauseTimeUnit;

			return this;
		}

		public Builder timeout(Integer timeout, TimeUnit timeoutTimeUnit) {
			_searchRetryFixture._timeout = timeout;
			_searchRetryFixture._timeoutTimeUnit = timeoutTimeUnit;

			return this;
		}

		private final SearchRetryFixture _searchRetryFixture =
			new SearchRetryFixture();

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

	private static void _sleep(long pause, TimeUnit pauseTimeUnit) {
		try {
			Thread.sleep(pauseTimeUnit.toMillis(pause));
		}
		catch (InterruptedException interruptedException) {
			throw new RuntimeException(interruptedException);
		}
	}

	private SearchRetryFixture() {
	}

	private SearchRetryFixture(SearchRetryFixture searchRetryFixture) {
		_attempts = searchRetryFixture._attempts;
		_pause = searchRetryFixture._pause;
		_pauseTimeUnit = searchRetryFixture._pauseTimeUnit;
		_timeout = searchRetryFixture._timeout;
		_timeoutTimeUnit = searchRetryFixture._timeoutTimeUnit;
	}

	private int _getAttempts() {
		return GetterUtil.getInteger(_attempts);
	}

	private int _getPause() {
		return GetterUtil.getInteger(_pause);
	}

	private TimeUnit _getPauseTimeUnit() {
		return (TimeUnit)GetterUtil.getObject(_pauseTimeUnit, TimeUnit.SECONDS);
	}

	private int _getTimeout() {
		return GetterUtil.getInteger(_timeout, 3);
	}

	private TimeUnit _getTimeoutTimeUnit() {
		return (TimeUnit)GetterUtil.getObject(
			_timeoutTimeUnit, TimeUnit.SECONDS);
	}

	private Integer _attempts;
	private Integer _pause;
	private TimeUnit _pauseTimeUnit;
	private Integer _timeout;
	private TimeUnit _timeoutTimeUnit;

}