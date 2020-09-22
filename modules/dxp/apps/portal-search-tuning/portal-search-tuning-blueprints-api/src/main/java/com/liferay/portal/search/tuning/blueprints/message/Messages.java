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

package com.liferay.portal.search.tuning.blueprints.message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Petteri Karttunen
 */
public class Messages {

	public void addMessage(Message message) {
		_messages.add(message);
	}

	public List<Message> getMessages() {
		return _messages;
	}

	public List<Message> getMessagesBySeverity(Severity severity) {
		return _messages;
	}

	public boolean hasErrors() {
		Stream<Message> stream = _messages.stream();

		return stream.anyMatch(
			m -> m.getSeverity(
			).equals(
				Severity.ERROR
			));
	}

	private final List<Message> _messages = new ArrayList<>();

}