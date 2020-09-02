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
          "query": {
            "query_string": {
              "default_operator": "or",
              "query": "${keywords}",
              "fields": [
                "title_${context.language_id}^2",
                "title",
                "content_${context.language_id}",
                "content"
              ]
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "icon": "vocabulary",
    "description": {
      "en_US": "Search title and content"
    },
    "title": {
      "en_US": "Match Any Keyword"
    },
    "conditions": [],
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "should",
        "query": {
          "query": {
            "multi_match": {
              "query": "${keywords}",
              "boost": 4,
              "fields": [
                "title_${context.language_id}^2",
                "content_${context.language_id}"
              ],
              "operator": "and"
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "icon": "time",
    "description": {
      "en_US": "Boost content matching all the keywords in a single field"
    },
    "title": {
      "en_US": "Boost All Keywords Match"
    },
    "conditions": [],
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "should",
        "query": {
          "query": {
            "multi_match": {
              "query": "${keywords}",
              "boost": 5,
              "fields": [
                "title_${context.language_id}^2",
                "content_${context.language_id}"
              ],
              "type": "phrase"
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "icon": "time",
    "description": {
      "en_US": "Boost content having a phrase match"
    },
    "title": {
      "en_US": "Boost Phrase Match"
    },
    "conditions": [],
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "should",
        "query": {
          "query": {
            "multi_match": {
              "query": "${keywords}",
              "boost": 5,
              "fields": [
                "title_${context.language_id}^2",
                "content_${context.language_id}"
              ],
              "type": "phrase_prefix"
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "icon": "time",
    "description": {
      "en_US": "Boost content having a phrase match in the beginning of a field"
    },
    "title": {
      "en_US": "Boost Phrase Prefix Match"
    },
    "conditions": [],
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "should",
        "query": {
          "query": {
            "match": {
              "assetTagNames": {
                "query": "${keywords}",
                "boost": 3,
                "operator": "or"
              }
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "icon": "time",
    "description": {
      "en_US": "Boost content having a match in tags"
    },
    "title": {
      "en_US": "Boost Tags Matching Keywords"
    },
    "conditions": [],
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "should",
        "query": {
          "query": {
            "term": {
              "assetTagNames": {
                "query": "liferay",
                "boost": 10
              }
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "icon": "time",
    "description": {
      "en_US": "Boost if user belongs to a user segment"
    },
    "title": {
      "en_US": "Boost Content For a User Segment"
    },
    "conditions": [
      {
        "handler": "default",
        "handler_parameters": {
          "evaluation_type": "contains",
          "parameter_name": "${user.segment_entry_ids}",
          "match_value": [
            123456
          ]
        },
        "operator": "and"
      }
    ],
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
                "expando__keyword__custom_fields__location_geolocation": {
                  "origin": {
                    "lon": "${ipstack.longitude}",
                    "lat": "${ipstack.latitude}"
                  },
                  "scale": "1000km",
                  "decay": 0.3
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
    "icon": "time",
    "description": {
      "en_US": "Boost content close to my location"
    },
    "title": {
      "en_US": "Boost Proximity"
    },
    "conditions": [],
    "enabled": false
  },
  {
    "clauses": [
      {
        "occur": "should",
        "query": {
          "query": {
            "match": {
              "content_${context.language_id}": "restaurant",
              "boost": 20
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "description": {
      "en_US": "Boost if keywords match a given value"
    },
    "title": {
      "en_US": "Boost by Keyword Match"
    },
    "conditions": [
      {
        "handler": "default",
        "handler_parameters": {
          "evaluation_type": "any_word_in",
          "parameter_name": "${keywords}",
          "match_value": [
            "food"
          ]
        },
        "operator": "AND"
      }
    ],
    "icon": "time",
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
                  "offset": "3d",
                  "origin": "${time.current_date|dateFormat=yyyyMMddHHmmss}",
                  "scale": "30d",
                  "decay": 0.4
                }
              },
              "boost": 50
            }
          }
        },
        "context": "query",
        "type": "wrapper"
      }
    ],
    "icon": "time",
    "description": {
      "en_US": "Boost content modified within a time frame"
    },
    "title": {
      "en_US": "Boost Freshness"
    },
    "conditions": [],
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
    "enabled": false
  },
  {
    "clauses": [
      {
        "occur": "must",
        "query": {
          "query": {
            "bool": {
              "should": [
                {
                  "bool": {
                    "must": [
                      {
                        "term": {
                          "entryClassName": "com.liferay.journal.model.JournalArticle"
                        }
                      },
                      {
                        "term": {
                          "head": "true"
                        }
                      },
                      {
                        "range": {
                          "displayDate_sortable": {
                            "include_lower": true,
                            "include_upper": true,
                            "from": "-9223372036854775808",
                            "to": "${time.current_date|dateFormat=timestamp}"
                          }
                        }
                      },
                      {
                        "range": {
                          "expirationDate_sortable": {
                            "include_lower": true,
                            "include_upper": true,
                            "from": "${time.current_date|dateFormat=timestamp}",
                            "to": "9223372036854775807"
                          }
                        }
                      }
                    ]
                  }
                },
                {
                  "bool": {
                    "must": [
                      {
                        "term": {
                          "entryClassName": "com.liferay.blogs.kernel.model.BlogsEntry"
                        }
                      },
                      {
                        "range": {
                          "displayDate_sortable": {
                            "include_lower": true,
                            "include_upper": true,
                            "from": "-9223372036854775808",
                            "to": "${time.current_date|dateFormat=timestamp}"
                          }
                        }
                      },
                      {
                        "range": {
                          "expirationDate_sortable": {
                            "include_lower": true,
                            "include_upper": true,
                            "from": "${time.current_date|dateFormat=timestamp}",
                            "to": "9223372036854775807"
                          }
                        }
                      }
                    ]
                  }
                },
                {
                  "term": {
                    "entryClassName": "com.liferay.document.library.kernel.model.DLFileEntry"
                  }
                },
                {
                  "term": {
                    "entryClassName": "com.liferay.message.boards.kernel.model.MBMessage"
                  }
                },
                {
                  "term": {
                    "entryClassName": "com.liferay.portal.kernel.model.User"
                  }
                },
                {
                  "term": {
                    "entryClassName": "com.liferay.knowledge.base.model.KBArticle"
                  }
                },
                {
                  "term": {
                    "entryClassName": "com.liferay.wiki.model.WikiPage"
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
    "icon": "filter",
    "description": {
      "en_US": "Limit content types to be searched"
    },
    "title": {
      "en_US": "Filter Content Type"
    },
    "conditions": [],
    "enabled": true
  },
  {
    "clauses": [
      {
        "occur": "must",
        "query": {
          "query": {
            "bool": {
              "should": [
                {
                  "term": {
                    "stagingGroup": false
                  }
                },
                {
                  "term": {
                    "entryClassName": "com.liferay.portal.kernel.model.User"
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
    "icon": "filter",
    "description": {
      "en_US": "Exclude staged groups from search"
    },
    "title": {
      "en_US": "Filter Published Sites"
    },
    "conditions": [],
    "enabled": true
  },
  {
    "clauses": [
      {
        "query": {
          "query": {
            "bool": {
              "must": [
                {
                  "term": {
                    "scopeGroupId": "${context.scope_group_id}"
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
      "en_US": "Limit search to requested group"
    },
    "title": {
      "en_US": "Filter by Scope"
    },
    "conditions": [
      {
        "handler": "default",
        "handler_parameters": {
          "evaluation_type": "exists",
          "parameter_name": "${context.scope_group_id}"
        }
      }
    ],
    "icon": "filter",
    "enabled": true
  },
  {
    "clauses": [
      {
        "query": {
          "query": {
            "bool": {
              "must": [
                {
                  "range": {
                    "modified_sortable": {
                      "include_lower": true,
                      "include_upper": true,
                      "from": "${request.time|dateFormat=timestamp}",
                      "to": "${time.current_date|dateFormat=timestamp}"
                    }
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
      "en_US": "Limit search to requested time range (requires a parameter definition)"
    },
    "title": {
      "en_US": "Filter by Time Range"
    },
    "conditions": [
      {
        "handler": "default",
        "handler_parameters": {
          "evaluation_type": "exists",
          "parameter_name": "${request.time}"
        }
      }
    ],
    "icon": "filter",
    "enabled": true
  }
];
