define([
    'backbone',
    'text!find/templates/app/page/search/results/results-number-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, template, i18n) {

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function(options) {
            this.documentsCollection = options.documentsCollection;

            this.listenTo(this.documentsCollection, 'reset update sync', this.updateCounts);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$currentNumber = this.$('.current-results-number');
            this.$totalNumber = this.$('.total-results-number');
            this.$firstNumber = this.$('.first-result-number');

            this.updateCounts();
        },

        updateCounts: function(){
            if (this.$currentNumber) {
                this.$currentNumber.text(this.documentsCollection.length);
                this.$totalNumber.text(this.documentsCollection.totalResults || 0);
                this.$firstNumber.text(this.documentsCollection.length ? 1 : 0);
            }
        },

        getText: function() {
            return $.trim(this.$el.text().replace(/\s+/g, ' '));
        }
    });

});