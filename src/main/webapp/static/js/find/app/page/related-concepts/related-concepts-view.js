define([
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'text!find/templates/app/page/related-concepts/related-concepts-view.html',
    'text!find/templates/app/page/related-concepts/related-concept-list-item.html',
    'text!find/templates/app/page/top-results-popover-contents.html',
    'text!find/templates/app/page/loading-spinner.html'
], function(Backbone, i18n, DocumentsCollection, relatedConceptsView, relatedConceptTemplate, topResultsPopoverContents, loadingSpinnerTemplate) {

    return Backbone.View.extend({

        className: 'suggestions-content',

        relatedConceptsView: _.template(relatedConceptsView),
        relatedConceptTemplate: _.template(relatedConceptTemplate),
        topResultsPopoverContents: _.template(topResultsPopoverContents),
        loadingSpinnerTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n}),

        events: {
            'mouseover a': _.debounce(function(e) {
                this.$('.popover-content').append(this.loadingSpinnerTemplate);

                this.topResultsCollection.fetch({
                    reset: false,
                    data: {
                        text: $(e.currentTarget).html(),
                        max_results: 3,
                        summary: 'context',
                        index: this.queryModel.get('indexes')
                    }
                });
            }, 800),
            'click .query-text' : function(e) {
                var $target = $(e.target);
                var queryText = $target.attr('data-title');
                this.queryModel.set('queryText', queryText);
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.entityCollection = options.entityCollection;

            this.topResultsCollection = new DocumentsCollection([], {
                indexesCollection: options.indexesCollection
            });

            this.listenTo(this.entityCollection, 'reset', function() {
                this.$list.empty();

                if (this.entityCollection.isEmpty()) {
                    this.$list.addClass('hide');
                }
                else {
                    this.$list.removeClass('hide');

                    var clusters = this.entityCollection.groupBy('cluster');

                    _.each(clusters, function(entities) {
                        this.$list.append(this.relatedConceptTemplate({
                            entities: entities
                        }));

                        this.$('li a').popover({
                            html: true,
                            placement: 'bottom',
                            trigger: 'hover'
                        })
                    }, this);
                }
            });

            /*suggested links*/
            this.listenTo(this.entityCollection, 'request', function() {
                this.$processing.removeClass('hide');
                this.$list.addClass('hide');
                this.$error.addClass('hide');

                this.$notLoading.addClass('hide');
            });

            this.listenTo(this.entityCollection, 'error', function() {
                this.$error.removeClass('hide');
                this.$list.addClass('hide');
                this.$processing.addClass('hide');

                this.$error.text(i18n['search.error.relatedConcepts']);
            });

            /*top 3 results popover*/
            this.listenTo(this.topResultsCollection, 'add', function(model){
                this.$('.popover-content .loading-spinner').remove();

                this.$('.popover-content').append(this.topResultsPopoverContents({
                    title: model.get('title'),
                    summary: model.get('summary').trim().substring(0, 100) + "..."
                }));
            });
        },

        render: function() {
            this.$el.html(this.relatedConceptsView({i18n:i18n}));

            this.$list = this.$('.related-concepts-list');
            this.$error = this.$('.related-concepts-error');

            this.$notLoading = this.$('.not-loading');

            this.$processing = this.$('.processing');
            this.$processing.append(this.loadingSpinnerTemplate);
        }

    })

});
