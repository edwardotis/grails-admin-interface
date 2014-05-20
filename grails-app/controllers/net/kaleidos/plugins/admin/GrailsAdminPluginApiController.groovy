package net.kaleidos.plugins.admin

import grails.converters.JSON
import grails.validation.ValidationException

class GrailsAdminPluginApiController {
    def objectDefinitionSource
    def adminConfigHolder
    def grailsAdminPluginGenericService
    def grailsAdminPluginBuilderService
    private static int ITEMS_BY_PAGE = 20

    def listDomains() {
        render adminConfigHolder.domainClasses as JSON
    }

    def getAdminAction(String slug, Long id) {
        def config = adminConfigHolder.getDomainConfigBySlug(slug)
        if (!config) {
            response.status = 404
            render(["error":"Domain no configured"] as JSON)
            return
        }

        def result
        def renderedResult
        if (id) {
            result = grailsAdminPluginGenericService.retrieveDomain(config.domainClass.clazz, id)
            if (!result) {
                response.status = 404
                render(["error":"Entity not found"] as JSON)
                return
            }
            renderedResult = grailsAdminPluginBuilderService.renderObjectAsJson(result)
        } else {
            def page = params.page

            if (!page) {
                page = 1
            }

            result = grailsAdminPluginGenericService.list(config.domainClass.clazz, (page -1) * ITEMS_BY_PAGE, ITEMS_BY_PAGE, params.sort,  params.sort_order)
            renderedResult = grailsAdminPluginBuilderService.renderListAsJson(result)
        }

        render renderedResult
    }

    def putAdminAction(String slug) {
        def config = adminConfigHolder.getDomainConfigBySlug(slug)
        if (!config) {
            response.status = 404
            render(["error":"Domain no configured"] as JSON)
            return
        }

        def result = [:]
        try {
            result = grailsAdminPluginGenericService.saveDomain(config.domainClass.clazz, request.JSON)
        } catch (ValidationException e) {
            response.status = 500
            render e.getErrors() as JSON
            return
        }

        render grailsAdminPluginBuilderService.renderObjectAsJson(result)
    }

    def postAdminAction(String slug, Long id) {
        def config = adminConfigHolder.getDomainConfigBySlug(slug)
        if (!config) {
            response.status = 404
            render(["error":"Domain no configured"] as JSON)
            return
        }

        def result = [:]
        try {
            result = grailsAdminPluginGenericService.updateDomain(config.domainClass.clazz, id, request.JSON)
        } catch (ValidationException e) {
            response.status = 500
            render e.getErrors() as JSON
            return
        } catch (RuntimeException e) {
            response.status = 500
            result = [error: e.message]
            render result as JSON
            return
        }

        render grailsAdminPluginBuilderService.renderObjectAsJson(result)
    }

    def deleteAdminAction(String slug, Long id) {
        def config = adminConfigHolder.getDomainConfigBySlug(slug)
        if (!config) {
            response.status = 404
            render(["error":"Domain no configured"] as JSON)
            return
        }

        try {
            grailsAdminPluginGenericService.deleteDomain(config.domainClass.clazz, id)
        } catch (RuntimeException e) {
            response.status = 500
            def result = [error: e.message]
            render result as JSON
            return
        }
        response.status = 204
        render ""
    }

    def deleteRelatedAdminAction(String slug, Long id, String propertyName, Long id2) {
        def config = adminConfigHolder.getDomainConfigBySlug(slug)
        if (!config) {
            response.status = 404
            render(["error":"Domain no configured"] as JSON)
            return
        }


        try {
            grailsAdminPluginGenericService.deleteRelatedDomain(config.domainClass.clazz, id, propertyName, id2)
        } catch (RuntimeException e) {
            response.status = 500
            def result = [error: e.message]
            render result as JSON
            return
        }
        response.status = 204
        render ""
    }


    def putRelatedAdminAction(String slug, Long id, String propertyName, Long id2) {
        def config = adminConfigHolder.getDomainConfigBySlug(slug)
        if (!config) {
            response.status = 404
            render(["error":"Domain no configured"] as JSON)
            return
        }

        try {
            grailsAdminPluginGenericService.putRelatedDomain(config.domainClass.clazz, id, propertyName, id2)
        } catch (RuntimeException e) {
            response.status = 500
            def result = [error: e.message]
            render result as JSON
            return
        }
        response.status = 204
        render ""
    }

}
