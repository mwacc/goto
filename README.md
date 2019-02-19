# goto
The project aims to show ElasticSearch's power for search and particularly geo-related search when the result must be ranked based on a proximity to requestor. For instance, there are several cities names a London around the world: in Canada, in Australia, and obviously in UK.

**I've been asked to add compiled dependencies to git, so don't be freak out seeing them :)**

The project contains several modules:
* _goto-web-ui_ is the main component that provides interface and search logic
* _es-gateway_ keeps auxiliary classes for dealing with geo data
* _crawler-*_ is horizontaly scalable crawler to collect data from external services; used only to collect data
* _common_ shared models

(c) 2015
