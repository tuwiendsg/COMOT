define(function(require) {
	var http = require('plugins/http'), ko = require('knockout');

	var url = 'http://api.flickr.com/services/feeds/photos_public.gne';

	var qs = {
		tags : 'mount ranier',
		tagmode : 'any',
		format : 'json'
	};

	return {
		images : ko.observableArray([]),
		activate : function() {
			var that = this;
			if (this.images().length > 0) {
				return;
			}

			return http.jsonp(url, qs, 'jsoncallback').then(function(response) {
				that.images(response.items);
			});
		}
	};
});