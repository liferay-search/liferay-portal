/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

/**
 * Temporary data. This data should eventually be fetched from the server.
 */

export const QUERY_FRAGMENTS = [
  {
    "clauses": [
      {
        "occur": "must",
        "query": {
          "default_operator": "or",
          "boost": 2,
          "fields": [
            {
              "field": "title_${context.language_id}",
              "boost": "3"
            },
            {
              "field": "content_${context.language_id}",
              "boost": "3"
            }
          ]
        },
        "context": "query",
        "type": "simple_query_string"
      }
    ],
    "title": {
      "en_US": "Search title and content"
    },
    "description": {
      "en_US": "Match any keyword"
    },
    "conditions": [],
    "icon": "vocabulary",
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "should",
        "query": {
          "query": {
            "function_score": {
              "gauss": {
                "modified": {
                  "offset": "5d",
                  "origin": "${time.current_date|dateFormat=yyyyMMddHHmmss}",
                  "scale": "30d",
                  "decay": 0.4
                }
              },
              "boost": 100
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "description": {
      "en_US": "Boost contents modified within a time frame"
    },
    "title": {
      "en_US": "Freshness"
    },
    "conditions": [],
    "icon": "time",
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "must",
        "query": {
          "query": {
            "bool": {
              "must": [
                {
                  "term": {
                    "entryClassName": "com.liferay.journal.model.JournalArticle"
                  }
                }
              ]
            }
          }
        },
        "context": "pre_filter",
        "type": "wrapper"
      }
    ],
    "description": {
      "en_US": "Limit search to Web Content"
    },
    "title": {
      "en_US": "Filter Web Content"
    },
    "conditions": [],
    "icon": "filter",
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "must",
        "query": {
          "query": {
            "bool": {
              "must": [
                {
                  "term": {
                    "status": 0
                  }
                }
              ]
            }
          }
        },
        "context": "pre_filter",
        "type": "wrapper"
      }
    ],
    "description": {
      "en_US": "Limit search to published content"
    },
    "title": {
      "en_US": "Filter Published Content"
    },
    "conditions": [],
    "icon": "filter",
    "enabled": true
  }
];
