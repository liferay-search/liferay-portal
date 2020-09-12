package com.liferay.portal.search.elasticsearch7.internal.proxy;

import com.liferay.portal.kernel.messaging.proxy.BaseMultiDestinationProxyBean;
import com.liferay.portal.kernel.messaging.proxy.ProxyRequest;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.IndexWriter;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineHelperUtil;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;

/**
 * @author André de Oliveira
 * @author Bruno Farache
 * @author Tina Tian
 */
@Component(service = SearchIndexWriterProxyBean.class)
public class SearchIndexWriterProxyBean
	extends BaseMultiDestinationProxyBean implements IndexWriter {

	@Override
	public void addDocument(SearchContext searchContext, Document document) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void clearQuerySuggestionDictionaryIndexes(
		SearchContext searchContext) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void clearSpellCheckerDictionaryIndexes(
		SearchContext searchContext) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void commit(SearchContext searchContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteDocument(SearchContext searchContext, String uid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteDocuments(
		SearchContext searchContext, Collection<String> uids) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteEntityDocuments(
		SearchContext searchContext, String className) {

		throw new UnsupportedOperationException();
	}

	@Override
	public String getDestinationName(ProxyRequest proxyRequest) {
		Object[] arguments = proxyRequest.getArguments();

		SearchContext searchContext = (SearchContext)arguments[0];

		return SearchEngineHelperUtil.getSearchWriterDestinationName(
			searchContext.getSearchEngineId());
	}

	@Override
	public void indexKeyword(
		SearchContext searchContext, float weight, String keywordType) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void indexQuerySuggestionDictionaries(SearchContext searchContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void indexQuerySuggestionDictionary(SearchContext searchContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void indexSpellCheckerDictionaries(SearchContext searchContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void indexSpellCheckerDictionary(SearchContext searchContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void partiallyUpdateDocument(
		SearchContext searchContext, Document document) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void partiallyUpdateDocuments(
		SearchContext searchContext, Collection<Document> documents) {
	}

	@Override
	public void updateDocument(SearchContext searchContext, Document document) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateDocuments(
		SearchContext searchContext, Collection<Document> documents) {

		throw new UnsupportedOperationException();
	}

}